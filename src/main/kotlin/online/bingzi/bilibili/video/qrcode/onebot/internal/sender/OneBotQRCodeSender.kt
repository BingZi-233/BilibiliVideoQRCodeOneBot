package online.bingzi.bilibili.video.qrcode.onebot.internal.sender

import online.bingzi.bilibili.bilibilivideo.api.qrcode.metadata.DependencyResult
import online.bingzi.bilibili.bilibilivideo.api.qrcode.metadata.DependencyStatus
import online.bingzi.bilibili.bilibilivideo.api.qrcode.options.SendOptions
import online.bingzi.bilibili.bilibilivideo.api.qrcode.result.SendResult
import online.bingzi.bilibili.bilibilivideo.api.qrcode.sender.QRCodeSender
import online.bingzi.bilibili.video.qrcode.onebot.BilibiliVideoQRCodeOneBot
import online.bingzi.bilibili.video.qrcode.onebot.internal.sender.config.SenderConfig
import online.bingzi.bilibili.video.qrcode.onebot.internal.sender.helper.SenderHelper
import org.bukkit.entity.Player
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submitAsync
import taboolib.common.platform.function.warning
import java.util.function.Consumer

class OneBotQRCodeSender : QRCodeSender {
    
    override val id: String = SenderConfig.senderId
    override val name: String = SenderConfig.senderName
    
    private var initialized = false
    
    override fun isAvailable(): Boolean {
        return SenderConfig.senderEnabled && 
               initialized && 
               SenderHelper.isOneBotPluginAvailable() && 
               SenderHelper.isOneBotApiAvailable()
    }
    
    override fun checkDependencies(): DependencyResult {
        val dependencies = mutableMapOf<String, DependencyStatus>()
        val missing = mutableListOf<String>()
        val softMissing = mutableListOf<String>()
        
        // 检查 OneBot 插件
        if (SenderHelper.isOneBotPluginAvailable()) {
            dependencies["OneBot"] = DependencyStatus.PRESENT
        } else {
            dependencies["OneBot"] = DependencyStatus.MISSING
            missing.add("OneBot")
        }
        
        // 检查 OneBot API 连接状态
        if (SenderHelper.isOneBotApiAvailable()) {
            dependencies["OneBotAPI"] = DependencyStatus.PRESENT
        } else {
            dependencies["OneBotAPI"] = DependencyStatus.MISSING
            softMissing.add("OneBotAPI")  // API连接状态作为软依赖
        }
        
        // 检查绑定服务
        try {
            val bindingService = BilibiliVideoQRCodeOneBot.bindingService
            dependencies["BindingService"] = DependencyStatus.PRESENT
        } catch (e: Exception) {
            dependencies["BindingService"] = DependencyStatus.MISSING
            missing.add("BindingService")
        }
        
        return DependencyResult(
            satisfied = missing.isEmpty(),
            missingDependencies = missing,
            missingSoftDependencies = softMissing,
            details = dependencies
        )
    }
    
    override fun send(player: Player, content: String, options: SendOptions): SendResult {
        SenderHelper.debugLog("开始发送二维码给玩家: ${player.name}")
        SenderHelper.debugLogQRCodeContent(content)
        
        // 检查发送器是否可用
        if (!isAvailable()) {
            val reason = when {
                !SenderConfig.senderEnabled -> "发送器已被禁用"
                !initialized -> "发送器未初始化"
                !SenderHelper.isOneBotPluginAvailable() -> "OneBot插件不可用"
                !SenderHelper.isOneBotApiAvailable() -> "OneBot API未连接"
                else -> "发送器不可用"
            }
            
            SenderHelper.debugLog("发送器不可用: $reason")
            SenderHelper.notifyPlayerOneBotUnavailable(player)
            return SendResult.Failure(id, reason, canRetry = true)
        }
        
        // 清理和验证二维码内容
        val sanitizedContent = SenderHelper.sanitizeQRCodeContent(content)
        if (sanitizedContent.isBlank()) {
            val reason = "二维码内容为空"
            SenderHelper.debugLog(reason)
            SenderHelper.notifyPlayer(player, false)
            return SendResult.Failure(id, reason, canRetry = false)
        }
        
        // 检查玩家绑定状态
        val playerUuid = player.uniqueId.toString()
        val bindingService = BilibiliVideoQRCodeOneBot.bindingService
        
        // 使用同步方式获取绑定信息（注意：这里使用回调但在同步上下文中等待结果）
        var qqNumber: Long? = null
        var bindingCheckComplete = false
        
        bindingService.getQQByPlayer(playerUuid) { qq ->
            qqNumber = qq
            bindingCheckComplete = true
        }
        
        // 等待绑定检查完成（简单的忙等待，实际生产环境可能需要更优雅的处理）
        var waitCount = 0
        while (!bindingCheckComplete && waitCount < 100) {
            Thread.sleep(10)
            waitCount++
        }
        
        if (qqNumber == null) {
            SenderHelper.debugLog("玩家未绑定QQ: ${player.name}")
            SenderHelper.notifyPlayerNotBound(player)
            return SendResult.Failure(id, "玩家未绑定QQ账户", canRetry = false)
        }
        
        // 格式化消息
        val message = SenderHelper.formatQRCodeMessage(sanitizedContent)
        
        // 发送消息
        var sendResult: SendResult? = null
        var sendComplete = false
        
        SenderHelper.sendPrivateMessage(qqNumber!!, message) { success ->
            sendResult = if (success) {
                SenderHelper.debugLog("二维码发送成功给QQ: $qqNumber")
                SenderHelper.notifyPlayer(player, true)
                SendResult.Success(
                    senderId = id,
                    timestamp = System.currentTimeMillis(),
                    metadata = mapOf(
                        "qq" to qqNumber!!,
                        "player" to player.name,
                        "contentLength" to sanitizedContent.length
                    )
                )
            } else {
                SenderHelper.debugLog("二维码发送失败给QQ: $qqNumber")
                SenderHelper.notifyPlayer(player, false)
                SendResult.Failure(
                    senderId = id,
                    reason = "QQ消息发送失败",
                    canRetry = true
                )
            }
            sendComplete = true
        }
        
        // 等待发送完成
        waitCount = 0
        while (!sendComplete && waitCount < 500) { // 5秒超时
            Thread.sleep(10)
            waitCount++
        }
        
        return sendResult ?: SendResult.Failure(id, "发送超时", canRetry = true)
    }
    
    override fun sendAsync(player: Player, content: String, options: SendOptions, callback: Consumer<SendResult>) {
        submitAsync {
            try {
                val result = send(player, content, options)
                callback.accept(result)
            } catch (e: Exception) {
                SenderHelper.debugLog("异步发送异常: ${e.message}")
                callback.accept(SendResult.Failure(
                    senderId = id,
                    reason = SenderHelper.buildErrorMessage("异步发送异常", e),
                    exception = e,
                    canRetry = true
                ))
            }
        }
    }
    
    override fun initialize() {
        if (initialized) {
            SenderHelper.debugLog("发送器已经初始化")
            return
        }
        
        try {
            SenderHelper.debugLog("正在初始化OneBot二维码发送器...")
            
            // 检查依赖
            val dependencyResult = checkDependencies()
            if (!dependencyResult.satisfied) {
                warning("OneBot二维码发送器依赖检查失败: ${dependencyResult.missingDependencies}")
                if (dependencyResult.missingDependencies.isNotEmpty()) {
                    throw IllegalStateException("缺少必需依赖: ${dependencyResult.missingDependencies}")
                }
            }
            
            if (dependencyResult.missingSoftDependencies.isNotEmpty()) {
                info("OneBot二维码发送器软依赖缺失: ${dependencyResult.missingSoftDependencies}")
            }
            
            initialized = true
            info("OneBot二维码发送器初始化完成")
            
        } catch (e: Exception) {
            warning("OneBot二维码发送器初始化失败: ${e.message}")
            initialized = false
            throw e
        }
    }
    
    override fun shutdown() {
        if (!initialized) {
            return
        }
        
        try {
            SenderHelper.debugLog("正在关闭OneBot二维码发送器...")
            initialized = false
            info("OneBot二维码发送器已关闭")
        } catch (e: Exception) {
            warning("关闭OneBot二维码发送器时发生错误: ${e.message}")
        }
    }
}
package online.bingzi.bilibili.video.qrcode.onebot.internal.sender

import online.bingzi.bilibili.bilibilivideo.api.qrcode.registry.QRCodeSenderRegistry
import online.bingzi.bilibili.video.qrcode.onebot.internal.sender.config.SenderConfig
import online.bingzi.bilibili.video.qrcode.onebot.internal.sender.helper.SenderHelper
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info
import taboolib.common.platform.function.severe
import taboolib.common.platform.function.warning

object SenderManager {
    
    private var senderInstance: OneBotQRCodeSender? = null
    private var registered = false
    
    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        info("正在初始化二维码发送器管理器...")
        
        // 检查是否启用
        if (!SenderConfig.senderEnabled) {
            info("二维码发送器已被配置禁用，跳过注册")
            return
        }
        
        // 检查 OneBot 插件是否可用
        if (!SenderHelper.isOneBotPluginAvailable()) {
            info("OneBot插件不可用，跳过二维码发送器注册")
            info("如需使用二维码发送功能，请确保OneBot插件已正确安装并启用")
            return
        }
        
        try {
            // 创建发送器实例
            val sender = OneBotQRCodeSender()
            senderInstance = sender
            
            // 初始化发送器
            sender.initialize()
            
            // 注册到 BilibiliVideo
            val success = QRCodeSenderRegistry.register(sender)
            if (success) {
                registered = true
                info("OneBot二维码发送器注册成功 (ID: ${sender.id})")
                
                // 尝试激活发送器（如果当前没有活跃的发送器）
                if (QRCodeSenderRegistry.getActiveSender() == null) {
                    val activated = QRCodeSenderRegistry.activate(sender.id)
                    if (activated) {
                        info("OneBot二维码发送器已激活")
                    } else {
                        warning("OneBot二维码发送器激活失败")
                    }
                }
                
                // 记录依赖状态
                logDependencyStatus(sender)
                
            } else {
                warning("OneBot二维码发送器注册失败")
                sender.shutdown()
                senderInstance = null
            }
            
        } catch (e: Exception) {
            severe("初始化OneBot二维码发送器失败: ${e.message}")
            e.printStackTrace()
            senderInstance?.shutdown()
            senderInstance = null
        }
    }
    
    @Awake(LifeCycle.DISABLE)
    fun onDisable() {
        info("正在关闭二维码发送器管理器...")
        
        try {
            // 注销发送器
            if (registered && senderInstance != null) {
                val success = QRCodeSenderRegistry.unregister(senderInstance!!.id)
                if (success) {
                    info("OneBot二维码发送器注销成功")
                } else {
                    warning("OneBot二维码发送器注销失败")
                }
                registered = false
            }
            
            // 关闭发送器
            senderInstance?.shutdown()
            senderInstance = null
            
            info("二维码发送器管理器已关闭")
            
        } catch (e: Exception) {
            severe("关闭二维码发送器管理器失败: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 获取发送器实例
     * @return 发送器实例，如果未注册则返回null
     */
    fun getSenderInstance(): OneBotQRCodeSender? {
        return senderInstance
    }
    
    /**
     * 检查发送器是否已注册
     * @return true如果已注册
     */
    fun isRegistered(): Boolean {
        return registered && senderInstance != null
    }
    
    /**
     * 检查发送器是否可用
     * @return true如果可用
     */
    fun isAvailable(): Boolean {
        return senderInstance?.isAvailable() ?: false
    }
    
    /**
     * 获取发送器状态信息
     * @return 状态信息映射
     */
    fun getStatus(): Map<String, Any> {
        val status = mutableMapOf<String, Any>()
        
        status["enabled"] = SenderConfig.senderEnabled
        status["registered"] = registered
        status["instance_created"] = (senderInstance != null)
        status["available"] = isAvailable()
        status["onebot_plugin"] = SenderHelper.isOneBotPluginAvailable()
        status["onebot_api"] = SenderHelper.isOneBotApiAvailable()
        
        senderInstance?.let { sender ->
            status["sender_id"] = sender.id
            status["sender_name"] = sender.name
            
            val dependencyResult = sender.checkDependencies()
            status["dependencies_satisfied"] = dependencyResult.satisfied
            status["missing_dependencies"] = dependencyResult.missingDependencies
            status["missing_soft_dependencies"] = dependencyResult.missingSoftDependencies
        }
        
        return status
    }
    
    /**
     * 记录依赖状态
     * @param sender 发送器实例
     */
    private fun logDependencyStatus(sender: OneBotQRCodeSender) {
        val dependencyResult = sender.checkDependencies()
        
        if (dependencyResult.satisfied) {
            info("所有必需依赖都已满足")
        } else {
            warning("存在未满足的依赖: ${dependencyResult.missingDependencies}")
        }
        
        if (dependencyResult.missingSoftDependencies.isNotEmpty()) {
            info("软依赖状态: ${dependencyResult.missingSoftDependencies}")
        }
        
        // 记录详细状态
        if (SenderConfig.debugEnabled) {
            dependencyResult.details.forEach { (name, status) ->
                info("依赖 $name: $status")
            }
        }
    }
    
    /**
     * 重新加载发送器（用于配置热重载）
     */
    fun reload() {
        info("正在重新加载二维码发送器...")
        
        // 先卸载
        onDisable()
        
        // 等待一段时间确保清理完成
        Thread.sleep(100)
        
        // 重新加载
        onEnable()
        
        info("二维码发送器重新加载完成")
    }
}
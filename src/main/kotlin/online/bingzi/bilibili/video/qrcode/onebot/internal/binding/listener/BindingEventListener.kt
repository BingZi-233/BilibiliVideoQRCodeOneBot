package online.bingzi.bilibili.video.qrcode.onebot.internal.binding.listener

import online.bingzi.bilibili.video.qrcode.onebot.internal.binding.BindingManager
import online.bingzi.bilibili.video.qrcode.onebot.internal.binding.config.BindingConfig
import online.bingzi.onebot.api.OneBotAPI
import online.bingzi.onebot.api.event.message.PrivateMessageEvent
import online.bingzi.onebot.api.event.status.OneBotConnectedEvent
import online.bingzi.onebot.api.event.status.OneBotDisconnectedEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning

/**
 * 绑定事件监听器
 * 处理来自 OneBot 的消息事件和连接状态事件
 */
object BindingEventListener {

    @SubscribeEvent
    fun onPrivateMessage(event: PrivateMessageEvent) {
        val rawMessage = event.message.trim()
        val message = extractTextFromMessage(rawMessage)
        val userId = event.userId
        
        // 调试日志：记录接收到的消息
        info("收到私聊消息 - 用户ID: $userId, 原始消息: '$rawMessage', 解析后: '$message', 消息ID: ${event.messageId}")
        info("原始消息数据 - selfId: ${event.selfId}, messageType: ${event.messageType}")
        
        when {
            message.equals("!help", ignoreCase = true) -> {
                // 显示帮助信息
                event.reply("🤖 QQ绑定机器人帮助\n\n可用命令：\n!bind <验证码> - 绑定游戏账户\n!unbind - 解除绑定\n!info - 查看绑定信息\n!help - 显示此帮助\n\n如需绑定，请先在游戏中执行 /qqbind 命令获取验证码。")
            }
            
            message.startsWith("!bind ", ignoreCase = true) -> {
                // 处理绑定请求
                val code = message.substring(6).trim()
                info("解析绑定命令 - 用户ID: $userId, 验证码: '$code'")
                handleBindRequest(event, userId, code)
            }
            
            message.equals("!bind", ignoreCase = true) -> {
                // 显示绑定用法
                event.reply("使用方法：!bind <验证码>\n请先在游戏中执行 /qqbind 命令获取验证码。")
            }
            
            message.equals("!unbind", ignoreCase = true) -> {
                // 处理解绑请求
                handleUnbindRequest(event, userId)
            }
            
            message.equals("!info", ignoreCase = true) -> {
                // 处理信息查询请求
                handleInfoRequest(event, userId)
            }
            
            else -> {
                // 检查是否为纯数字验证码
                if (message.matches(Regex("\\d+")) && message.length == BindingConfig.codeLength) {
                    info("识别为纯数字验证码 - 用户ID: $userId, 验证码: '$message'")
                    handleBindRequest(event, userId, message)
                } else {
                    info("未识别的消息格式 - 用户ID: $userId, 消息: '$message', 消息长度: ${message.length}, 期望长度: ${BindingConfig.codeLength}")
                }
                // 对于其他消息，不做任何回应
            }
        }
    }
    
    private fun handleBindRequest(event: PrivateMessageEvent, userId: Long, code: String) {
        info("开始处理绑定请求 - 用户ID: $userId, 验证码: '$code'")
        BindingManager.processQQBindingRequest(userId, code) { success, message ->
            info("绑定请求处理完成 - 用户ID: $userId, 成功: $success, 回复消息: '$message'")
            event.reply(message)
            
            if (success) {
                info("用户 $userId 成功绑定游戏账户，验证码: $code")
            } else {
                info("用户 $userId 绑定失败，验证码: $code")
            }
        }
    }
    
    private fun handleUnbindRequest(event: PrivateMessageEvent, userId: Long) {
        BindingManager.processQQUnbindRequest(userId) { success, message ->
            event.reply(message)
            
            if (success) {
                info("用户 $userId 成功解绑游戏账户")
            } else {
                info("用户 $userId 解绑失败")
            }
        }
    }
    
    private fun handleInfoRequest(event: PrivateMessageEvent, userId: Long) {
        BindingManager.processQQInfoRequest(userId) { success, message ->
            event.reply(message)
        }
    }
    
    @SubscribeEvent
    fun onBotConnected(event: OneBotConnectedEvent) {
        info("OneBot已连接，绑定功能可用，机器人QQ: ${event.selfId}")
    }
    
    @SubscribeEvent
    fun onBotDisconnected(event: OneBotDisconnectedEvent) {
        warning("OneBot连接断开，绑定功能不可用，断开原因: ${event.reason}")
    }
    
    /**
     * 检查OneBot是否可用
     * @return true如果可用，false如果不可用
     */
    fun isOneBotAvailable(): Boolean {
        return try {
            OneBotAPI.isConnected()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 向指定QQ发送消息
     * @param qqNumber 目标QQ号
     * @param message 消息内容
     * @param callback 发送结果回调
     */
    fun sendPrivateMessage(qqNumber: Long, message: String, callback: (Boolean) -> Unit = {}) {
        if (!isOneBotAvailable()) {
            callback(false)
            return
        }
        
        OneBotAPI.sendPrivateMessage(qqNumber, message) { success ->
            callback(success)
        }
    }
    
    /**
     * 从 OneBot 消息中提取纯文本内容
     * 支持两种格式：
     * 1. 纯文本：直接返回
     * 2. JSON数组格式：[{"type":"text","data":{"text":"实际内容"}}]
     * 
     * @param message 原始消息
     * @return 提取出的纯文本内容
     */
    private fun extractTextFromMessage(message: String): String {
        // 如果已经是纯文本，直接返回
        if (!message.startsWith("[{")) {
            return message
        }
        
        // 解析 JSON 数组格式的消息
        try {
            // 使用正则表达式提取 text 字段的值
            val regex = """"text"\s*:\s*"([^"]*)"""".toRegex()
            val match = regex.find(message)
            if (match != null) {
                val extractedText = match.groupValues[1]
                info("成功解析消息：'$message' -> '$extractedText'")
                return extractedText
            } else {
                warning("未找到匹配的文本内容：$message")
            }
        } catch (e: Exception) {
            warning("解析消息格式失败：$message，错误：${e.message}")
        }
        
        // 解析失败时返回原始消息
        return message
    }
}
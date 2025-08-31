package online.bingzi.bilibili.video.qrcode.onebot.internal.sender.config

import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration

/**
 * 发送器配置管理类
 * 负责管理 OneBot 二维码发送器的相关配置
 */
object SenderConfig {
    
    @Config("sender.yml", autoReload = true)
    lateinit var config: Configuration
        private set
    
    @ConfigNode("sender.id", bind = "sender.yml")
    var senderId: String = "onebot"
    
    @ConfigNode("sender.name", bind = "sender.yml")
    var senderName: String = "OneBot二维码发送器"
    
    @ConfigNode("sender.enabled", bind = "sender.yml")
    var senderEnabled: Boolean = true
    
    @ConfigNode("sender.retry-count", bind = "sender.yml")
    var retryCount: Int = 3
    
    @ConfigNode("sender.timeout", bind = "sender.yml")
    var timeout: Long = 30000L
    
    @ConfigNode("messages.qrcode-header", bind = "sender.yml")
    var qrcodeHeaderMessage: String = """
        🔗 Bilibili登录二维码
        请使用手机扫描以下链接中的二维码完成登录：
    """.trimIndent()
    
    @ConfigNode("messages.qrcode-footer", bind = "sender.yml")
    var qrcodeFooterMessage: String = """
        
        📱 扫码步骤：
        1. 打开链接
        2. 使用哔哩哔哩APP扫描二维码
        3. 确认登录
        
        ⏰ 二维码有效期：3分钟
        ❗ 请不要将链接分享给他人
    """.trimIndent()
    
    @ConfigNode("messages.send-success", bind = "sender.yml")
    var sendSuccessMessage: String = "§a二维码已发送到你的QQ，请查收！"
    
    @ConfigNode("messages.send-failed", bind = "sender.yml")
    var sendFailedMessage: String = "§c发送二维码失败，请检查QQ绑定状态或稍后重试。"
    
    @ConfigNode("messages.not-bound", bind = "sender.yml")
    var notBoundMessage: String = """
        §c你还没有绑定QQ账户！
        §7请先使用 §e/qqbind §7命令绑定QQ，然后再尝试登录。
    """.trimIndent()
    
    @ConfigNode("messages.onebot-unavailable", bind = "sender.yml")
    var onebotUnavailableMessage: String = """
        §c OneBot机器人服务暂时不可用！
        §7请联系管理员检查机器人连接状态，或使用其他登录方式。
    """.trimIndent()
    
    @ConfigNode("debug.enabled", bind = "sender.yml")
    var debugEnabled: Boolean = false
    
    @ConfigNode("debug.log-qrcode-content", bind = "sender.yml")
    var logQrcodeContent: Boolean = false
    
    /**
     * 格式化二维码消息
     * @param qrcodeUrl 二维码URL
     * @return 格式化后的完整消息
     */
    fun formatQRCodeMessage(qrcodeUrl: String): String {
        return buildString {
            append(qrcodeHeaderMessage)
            append("\n\n")
            append(qrcodeUrl)
            append("\n")
            append(qrcodeFooterMessage)
        }
    }
    
    /**
     * 重载配置
     */
    fun reload() {
        if (::config.isInitialized) {
            config.reload()
        }
    }
}
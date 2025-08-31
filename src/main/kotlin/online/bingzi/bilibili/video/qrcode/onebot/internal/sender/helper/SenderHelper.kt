package online.bingzi.bilibili.video.qrcode.onebot.internal.sender.helper

import online.bingzi.bilibili.video.qrcode.onebot.internal.sender.config.SenderConfig
import online.bingzi.onebot.api.OneBotAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import java.util.regex.Pattern

object SenderHelper {
    
    private val URL_PATTERN = Pattern.compile(
        "https?://(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"
    )
    
    /**
     * 检查 OneBot 插件是否存在
     * @return true 如果 OneBot 插件已加载
     */
    fun isOneBotPluginAvailable(): Boolean {
        return try {
            val plugin = Bukkit.getPluginManager().getPlugin("OneBot")
            plugin != null && plugin.isEnabled
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查 OneBot API 是否可用
     * @return true 如果 OneBot 已连接
     */
    fun isOneBotApiAvailable(): Boolean {
        return try {
            isOneBotPluginAvailable() && OneBotAPI.isConnected()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 验证二维码内容是否为有效的URL
     * @param content 二维码内容
     * @return true 如果是有效的URL
     */
    fun isValidQRCodeUrl(content: String): Boolean {
        return content.isNotBlank() && URL_PATTERN.matcher(content.trim()).matches()
    }
    
    /**
     * 格式化二维码消息
     * @param qrcodeContent 二维码内容（URL）
     * @return 格式化后的消息
     */
    fun formatQRCodeMessage(qrcodeContent: String): String {
        return if (isValidQRCodeUrl(qrcodeContent)) {
            SenderConfig.formatQRCodeMessage(qrcodeContent.trim())
        } else {
            "二维码链接：$qrcodeContent"
        }
    }
    
    /**
     * 发送私聊消息
     * @param qqNumber 目标QQ号
     * @param message 消息内容
     * @param callback 发送结果回调
     */
    fun sendPrivateMessage(qqNumber: Long, message: String, callback: (Boolean) -> Unit) {
        if (!isOneBotApiAvailable()) {
            callback(false)
            return
        }
        
        try {
            OneBotAPI.sendPrivateMessage(qqNumber, message) { success ->
                if (SenderConfig.debugEnabled) {
                    if (success) {
                        info("成功发送私聊消息到 QQ: $qqNumber")
                    } else {
                        warning("发送私聊消息失败，QQ: $qqNumber")
                    }
                }
                callback(success)
            }
        } catch (e: Exception) {
            if (SenderConfig.debugEnabled) {
                warning("发送私聊消息异常: ${e.message}")
            }
            callback(false)
        }
    }
    
    /**
     * 通知玩家发送结果
     * @param player 玩家
     * @param success 是否发送成功
     */
    fun notifyPlayer(player: Player, success: Boolean) {
        val message = if (success) {
            SenderConfig.sendSuccessMessage
        } else {
            SenderConfig.sendFailedMessage
        }
        player.sendMessage(message)
    }
    
    /**
     * 通知玩家未绑定QQ
     * @param player 玩家
     */
    fun notifyPlayerNotBound(player: Player) {
        player.sendMessage(SenderConfig.notBoundMessage)
    }
    
    /**
     * 通知玩家OneBot不可用
     * @param player 玩家
     */
    fun notifyPlayerOneBotUnavailable(player: Player) {
        player.sendMessage(SenderConfig.onebotUnavailableMessage)
    }
    
    /**
     * 记录调试信息
     * @param message 消息
     */
    fun debugLog(message: String) {
        if (SenderConfig.debugEnabled) {
            info("[QRCodeSender] $message")
        }
    }
    
    /**
     * 记录二维码内容（仅在调试模式下）
     * @param content 二维码内容
     */
    fun debugLogQRCodeContent(content: String) {
        if (SenderConfig.debugEnabled && SenderConfig.logQrcodeContent) {
            info("[QRCodeSender] 二维码内容: $content")
        }
    }
    
    /**
     * 清理二维码内容，移除可能的敏感信息
     * @param content 原始内容
     * @return 清理后的内容
     */
    fun sanitizeQRCodeContent(content: String): String {
        // 移除多余的空白字符
        var cleaned = content.trim()
        
        // 如果不是有效URL，可能需要进一步处理
        if (!isValidQRCodeUrl(cleaned)) {
            // 可以在这里添加其他格式的处理逻辑
            debugLog("警告: 二维码内容不是有效的URL格式: $cleaned")
        }
        
        return cleaned
    }
    
    /**
     * 构建错误信息
     * @param reason 错误原因
     * @param exception 异常信息（可选）
     * @return 格式化的错误信息
     */
    fun buildErrorMessage(reason: String, exception: Exception? = null): String {
        return if (exception != null) {
            "$reason: ${exception.message}"
        } else {
            reason
        }
    }
}
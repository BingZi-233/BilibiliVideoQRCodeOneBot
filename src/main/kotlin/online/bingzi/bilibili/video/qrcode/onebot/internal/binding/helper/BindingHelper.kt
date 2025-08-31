package online.bingzi.bilibili.video.qrcode.onebot.internal.binding.helper

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 绑定功能辅助工具类
 * 负责管理绑定请求和相关工具方法
 * 消息格式化功能已迁移至 I18n 系统
 */
object BindingHelper {
    
    private val bindingRequests = ConcurrentHashMap<String, BindingRequest>()
    
    /**
     * 绑定请求数据类
     */
    data class BindingRequest(
        val playerUuid: String,
        val playerName: String,
        val code: String,
        val expiryTime: Long,
        val createdTime: Long = System.currentTimeMillis()
    )
    
    /**
     * 创建绑定请求
     */
    fun createBindingRequest(
        playerUuid: String, 
        playerName: String, 
        code: String, 
        expiryTimeMillis: Long
    ): BindingRequest {
        val expiryTime = System.currentTimeMillis() + expiryTimeMillis
        val request = BindingRequest(playerUuid, playerName, code, expiryTime)
        bindingRequests[playerUuid] = request
        return request
    }
    
    /**
     * 获取绑定请求（自动清理过期请求）
     */
    fun getBindingRequest(playerUuid: String): BindingRequest? {
        val request = bindingRequests[playerUuid] ?: return null
        return if (System.currentTimeMillis() > request.expiryTime) {
            bindingRequests.remove(playerUuid)
            null
        } else {
            request
        }
    }
    
    /**
     * 通过验证码查找绑定请求
     */
    fun findBindingRequestByCode(code: String): BindingRequest? {
        return bindingRequests.values.find { request ->
            request.code == code && System.currentTimeMillis() <= request.expiryTime
        }
    }
    
    /**
     * 移除绑定请求
     */
    fun removeBindingRequest(playerUuid: String): Boolean {
        return bindingRequests.remove(playerUuid) != null
    }
    
    /**
     * 清理过期的绑定请求
     */
    fun cleanExpiredRequests() {
        val currentTime = System.currentTimeMillis()
        bindingRequests.entries.removeIf { (_, request) ->
            currentTime > request.expiryTime
        }
    }
    
    /**
     * 验证验证码格式是否有效
     */
    fun isValidCode(code: String, codeLength: Int): Boolean {
        return code.matches(Regex("\\d{$codeLength}"))
    }
    
    /**
     * 通过 UUID 获取在线玩家
     */
    fun getOnlinePlayerByUuid(uuid: String): Player? {
        return Bukkit.getPlayer(UUID.fromString(uuid))
    }
    
    /**
     * 通过名称获取在线玩家
     */
    fun getOnlinePlayerByName(name: String): Player? {
        return Bukkit.getPlayer(name)
    }
    
    /**
     * 向玩家发送消息（如果在线）
     */
    fun notifyPlayer(playerUuid: String, message: String) {
        getOnlinePlayerByUuid(playerUuid)?.sendMessage(message)
    }
    
    /**
     * 获取剩余时间显示文本
     */
    fun getRemainingTime(expiryTime: Long): String {
        val remaining = (expiryTime - System.currentTimeMillis()) / 1000
        return if (remaining > 0) {
            "${remaining}秒"
        } else {
            "已过期"
        }
    }
}
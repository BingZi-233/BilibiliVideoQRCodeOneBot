package online.bingzi.bilibili.video.qrcode.onebot.internal.verification

import online.bingzi.bilibili.video.qrcode.onebot.api.verification.VerificationService
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class VerificationCodeGenerator : VerificationService {
    
    private val codeStorage = ConcurrentHashMap<String, VerificationData>()
    private val scheduler = Executors.newScheduledThreadPool(1)
    
    init {
        // 定时清理过期验证码
        scheduler.scheduleAtFixedRate({
            cleanExpiredCodes()
        }, 1, 1, TimeUnit.MINUTES)
    }
    
    override fun generateCode(length: Int): String {
        return generateRandomCode(length)
    }
    
    override fun generateCode(userId: String, length: Int): String {
        val code = generateRandomCode(length)
        val expiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(VerificationService.DEFAULT_EXPIRY_MINUTES.toLong())
        codeStorage[userId] = VerificationData(code, expiryTime)
        return code
    }
    
    override fun verifyCode(userId: String, code: String): Boolean {
        val data = codeStorage[userId] ?: return false
        if (System.currentTimeMillis() > data.expiryTime) {
            codeStorage.remove(userId)
            return false
        }
        val isValid = data.code == code
        if (isValid) {
            codeStorage.remove(userId) // 验证成功后移除验证码
        }
        return isValid
    }
    
    override fun isCodeValid(userId: String, code: String): Boolean {
        val data = codeStorage[userId] ?: return false
        if (System.currentTimeMillis() > data.expiryTime) {
            codeStorage.remove(userId)
            return false
        }
        return data.code == code
    }
    
    override fun clearCode(userId: String): Boolean {
        return codeStorage.remove(userId) != null
    }
    
    override fun getCodeExpiryTime(userId: String): Long? {
        return codeStorage[userId]?.expiryTime
    }
    
    private fun generateRandomCode(length: Int): String {
        val range = 0 until 10
        return (1..length).map { Random.nextInt(range.first, range.last) }.joinToString("")
    }
    
    private fun cleanExpiredCodes() {
        val currentTime = System.currentTimeMillis()
        codeStorage.entries.removeIf { (_, data) ->
            currentTime > data.expiryTime
        }
    }
    
    private data class VerificationData(
        val code: String,
        val expiryTime: Long
    )
    
    fun shutdown() {
        scheduler.shutdown()
    }
}
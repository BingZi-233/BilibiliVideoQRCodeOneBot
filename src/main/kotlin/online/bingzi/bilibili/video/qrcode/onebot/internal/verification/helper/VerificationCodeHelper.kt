package online.bingzi.bilibili.video.qrcode.onebot.internal.verification.helper

import online.bingzi.bilibili.video.qrcode.onebot.api.verification.VerificationService
import online.bingzi.bilibili.video.qrcode.onebot.internal.verification.VerificationCodeGenerator

object VerificationCodeHelper {
    
    private val generator = VerificationCodeGenerator()
    
    fun generateCode(length: Int = VerificationService.DEFAULT_LENGTH): String {
        return generator.generateCode(length)
    }
    
    fun generateCodeForUser(userId: String, length: Int = VerificationService.DEFAULT_LENGTH): String {
        return generator.generateCode(userId, length)
    }
    
    fun verifyCode(userId: String, code: String): Boolean {
        return generator.verifyCode(userId, code)
    }
    
    fun isCodeValid(userId: String, code: String): Boolean {
        return generator.isCodeValid(userId, code)
    }
    
    fun clearCode(userId: String): Boolean {
        return generator.clearCode(userId)
    }
    
    fun getCodeExpiryTime(userId: String): Long? {
        return generator.getCodeExpiryTime(userId)
    }
    
    fun isCodeExpired(userId: String): Boolean {
        val expiryTime = getCodeExpiryTime(userId) ?: return true
        return System.currentTimeMillis() > expiryTime
    }
    
    fun getCodeRemainingTime(userId: String): Long {
        val expiryTime = getCodeExpiryTime(userId) ?: return 0L
        val remaining = expiryTime - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0L
    }
    
    fun formatCode(code: String): String {
        return code.chunked(3).joinToString(" ")
    }
    
    fun shutdown() {
        generator.shutdown()
    }
}
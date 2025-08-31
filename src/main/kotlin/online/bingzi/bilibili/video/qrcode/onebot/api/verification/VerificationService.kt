package online.bingzi.bilibili.video.qrcode.onebot.api.verification

interface VerificationService {
    
    fun generateCode(length: Int = 6): String
    
    fun generateCode(userId: String, length: Int = 6): String
    
    fun verifyCode(userId: String, code: String): Boolean
    
    fun isCodeValid(userId: String, code: String): Boolean
    
    fun clearCode(userId: String): Boolean
    
    fun getCodeExpiryTime(userId: String): Long?
    
    companion object {
        const val DEFAULT_LENGTH = 6
        const val DEFAULT_EXPIRY_MINUTES = 5
    }
}
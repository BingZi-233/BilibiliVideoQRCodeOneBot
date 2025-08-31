package online.bingzi.bilibili.video.qrcode.onebot.api.qrcode

import online.bingzi.bilibili.video.qrcode.onebot.internal.qrcode.config.QRCodeConfig

/**
 * 二维码服务接口
 */
interface QRCodeService {
    
    /**
     * 生成二维码图片字节数组
     * 
     * @param content 二维码内容
     * @return 二维码图片字节数组
     * @throws QRCodeGenerationException 生成失败时抛出异常
     */
    fun generateQRCode(content: String): ByteArray
    
    /**
     * 使用自定义配置生成二维码图片字节数组
     * 
     * @param content 二维码内容
     * @param config 二维码配置
     * @return 二维码图片字节数组
     * @throws QRCodeGenerationException 生成失败时抛出异常
     */
    fun generateQRCode(content: String, config: QRCodeConfig): ByteArray
    
    /**
     * 生成 Base64 编码的二维码图片
     * 
     * @param content 二维码内容
     * @return Base64 编码的二维码图片字符串
     * @throws QRCodeGenerationException 生成失败时抛出异常
     */
    fun generateQRCodeBase64(content: String): String
    
    /**
     * 使用自定义配置生成 Base64 编码的二维码图片
     * 
     * @param content 二维码内容
     * @param config 二维码配置
     * @return Base64 编码的二维码图片字符串
     * @throws QRCodeGenerationException 生成失败时抛出异常
     */
    fun generateQRCodeBase64(content: String, config: QRCodeConfig): String
}

/**
 * 二维码生成异常
 */
class QRCodeGenerationException(message: String, cause: Throwable? = null) : Exception(message, cause)
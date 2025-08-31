package online.bingzi.bilibili.video.qrcode.onebot.internal.qrcode

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import online.bingzi.bilibili.video.qrcode.onebot.api.qrcode.QRCodeGenerationException
import online.bingzi.bilibili.video.qrcode.onebot.api.qrcode.QRCodeService
import online.bingzi.bilibili.video.qrcode.onebot.internal.qrcode.config.QRCodeConfig
import online.bingzi.bilibili.video.qrcode.onebot.internal.qrcode.helper.QRCodeHelper

/**
 * 二维码生成器实现类
 */
object QRCodeGenerator : QRCodeService {
    
    private val writer = QRCodeWriter()
    
    override fun generateQRCode(content: String): ByteArray {
        return generateQRCode(content, QRCodeConfig.DEFAULT)
    }
    
    override fun generateQRCode(content: String, config: QRCodeConfig): ByteArray {
        try {
            // 验证输入参数
            QRCodeHelper.validateContent(content)
            QRCodeHelper.validateSize(config.width, config.height)
            
            // 设置编码参数
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to config.errorCorrectionLevel,
                EncodeHintType.CHARACTER_SET to config.characterSet,
                EncodeHintType.MARGIN to config.margin
            )
            
            // 生成二维码矩阵
            val bitMatrix = writer.encode(
                content,
                BarcodeFormat.QR_CODE,
                config.width,
                config.height,
                hints
            )
            
            // 转换为图片
            val bufferedImage = QRCodeHelper.matrixToBufferedImage(bitMatrix)
            
            // 转换为字节数组
            return QRCodeHelper.imageToByteArray(bufferedImage, config.imageFormat)
            
        } catch (e: Exception) {
            throw QRCodeGenerationException("生成二维码失败: ${e.message}", e)
        }
    }
    
    override fun generateQRCodeBase64(content: String): String {
        return generateQRCodeBase64(content, QRCodeConfig.DEFAULT)
    }
    
    override fun generateQRCodeBase64(content: String, config: QRCodeConfig): String {
        try {
            val qrCodeBytes = generateQRCode(content, config)
            return QRCodeHelper.bytesToBase64(qrCodeBytes)
        } catch (e: Exception) {
            throw QRCodeGenerationException("生成Base64二维码失败: ${e.message}", e)
        }
    }
}
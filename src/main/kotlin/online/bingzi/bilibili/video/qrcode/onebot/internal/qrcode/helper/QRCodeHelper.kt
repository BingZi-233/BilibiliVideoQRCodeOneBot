package online.bingzi.bilibili.video.qrcode.onebot.internal.qrcode.helper

import com.google.zxing.common.BitMatrix
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

/**
 * 二维码生成辅助工具类
 */
object QRCodeHelper {
    
    /**
     * 将二维码矩阵转换为 BufferedImage
     * 
     * @param matrix 二维码矩阵
     * @return BufferedImage 对象
     */
    fun matrixToBufferedImage(matrix: BitMatrix): BufferedImage {
        val width = matrix.width
        val height = matrix.height
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                val color = if (matrix.get(x, y)) Color.BLACK.rgb else Color.WHITE.rgb
                image.setRGB(x, y, color)
            }
        }
        
        return image
    }
    
    /**
     * 将 BufferedImage 转换为字节数组
     * 
     * @param image BufferedImage 对象
     * @param format 图片格式
     * @return 图片字节数组
     */
    fun imageToByteArray(image: BufferedImage, format: String): ByteArray {
        ByteArrayOutputStream().use { outputStream ->
            ImageIO.write(image, format, outputStream)
            return outputStream.toByteArray()
        }
    }
    
    /**
     * 将字节数组编码为 Base64 字符串
     * 
     * @param bytes 字节数组
     * @return Base64 编码的字符串
     */
    fun bytesToBase64(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes)
    }
    
    /**
     * 验证二维码内容是否有效
     * 
     * @param content 二维码内容
     * @throws IllegalArgumentException 内容无效时抛出异常
     */
    fun validateContent(content: String) {
        if (content.isBlank()) {
            throw IllegalArgumentException("二维码内容不能为空")
        }
        
        if (content.length > 7089) {
            throw IllegalArgumentException("二维码内容过长，最大支持7089个字符")
        }
    }
    
    /**
     * 验证二维码尺寸是否有效
     * 
     * @param width 宽度
     * @param height 高度
     * @throws IllegalArgumentException 尺寸无效时抛出异常
     */
    fun validateSize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) {
            throw IllegalArgumentException("二维码尺寸必须大于0")
        }
        
        if (width > 2000 || height > 2000) {
            throw IllegalArgumentException("二维码尺寸不能超过2000像素")
        }
    }
}
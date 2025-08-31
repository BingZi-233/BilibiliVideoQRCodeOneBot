package online.bingzi.bilibili.video.qrcode.onebot.internal.qrcode.config

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * 二维码生成配置类
 */
data class QRCodeConfig(
    /**
     * 二维码宽度（像素）
     */
    val width: Int = 300,
    
    /**
     * 二维码高度（像素）
     */
    val height: Int = 300,
    
    /**
     * 二维码边距
     */
    val margin: Int = 1,
    
    /**
     * 纠错级别
     */
    val errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M,
    
    /**
     * 字符编码
     */
    val characterSet: String = "UTF-8",
    
    /**
     * 图片格式
     */
    val imageFormat: String = "PNG"
) {
    companion object {
        /**
         * 默认配置
         */
        val DEFAULT = QRCodeConfig()
        
        /**
         * 小尺寸配置
         */
        val SMALL = QRCodeConfig(
            width = 200,
            height = 200
        )
        
        /**
         * 大尺寸配置
         */
        val LARGE = QRCodeConfig(
            width = 500,
            height = 500
        )
        
        /**
         * 高纠错级别配置
         */
        val HIGH_ERROR_CORRECTION = QRCodeConfig(
            errorCorrectionLevel = ErrorCorrectionLevel.H
        )
    }
}
package online.bingzi.bilibili.video.qrcode.onebot

import online.bingzi.bilibili.video.qrcode.onebot.api.binding.BindingService
import online.bingzi.bilibili.video.qrcode.onebot.api.qrcode.QRCodeService
import online.bingzi.bilibili.video.qrcode.onebot.api.verification.VerificationService
import online.bingzi.bilibili.video.qrcode.onebot.internal.binding.BindingManager
import online.bingzi.bilibili.video.qrcode.onebot.internal.qrcode.QRCodeGenerator
import online.bingzi.bilibili.video.qrcode.onebot.internal.sender.SenderManager
import online.bingzi.bilibili.video.qrcode.onebot.internal.verification.VerificationCodeGenerator
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info

object BilibiliVideoQRCodeOneBot : Plugin() {
    
    /**
     * 获取绑定服务实例
     */
    val bindingService: BindingService
        get() = BindingManager
    
    /**
     * 获取二维码服务实例
     */
    val qrcodeService: QRCodeService
        get() = QRCodeGenerator
    
    /**
     * 获取验证码服务实例
     */
    val verificationService: VerificationService
        get() = VerificationCodeGenerator()
    
    /**
     * 获取二维码发送器管理器
     */
    val senderManager: SenderManager
        get() = SenderManager
    
    override fun onLoad() {
        info("正在加载 BilibiliVideoQRCodeOneBot 插件...")
        info("BilibiliVideoQRCodeOneBot 插件加载完成！")
    }

    override fun onEnable() {
        info("正在启动 BilibiliVideoQRCodeOneBot 插件...")
        info("数据库模块已初始化完成")
        info("二维码生成模块已初始化完成")
        info("验证码服务已初始化完成")
        info("绑定管理器已启动")
        info("二维码发送器管理器已启动")
        info("BilibiliVideoQRCodeOneBot 插件启动完成！")
    }

    override fun onActive() {
        info("正在激活 BilibiliVideoQRCodeOneBot 插件...")
        info("绑定功能已激活，支持 OneBot QQ 机器人绑定")
        if (senderManager.isRegistered()) {
            info("BilibiliVideo 二维码发送器已注册并激活")
        } else {
            info("BilibiliVideo 二维码发送器未注册（OneBot可能不可用）")
        }
        info("BilibiliVideoQRCodeOneBot 插件激活完成！")
    }

    override fun onDisable() {
        info("正在禁用 BilibiliVideoQRCodeOneBot 插件...")
        info("BilibiliVideoQRCodeOneBot 插件禁用完成！")
    }
}
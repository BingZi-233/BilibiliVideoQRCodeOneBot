package online.bingzi.bilibili.video.qrcode.onebot

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info

object BilibiliVideoQRCodeOneBot : Plugin() {
    override fun onLoad() {
        info("正在加载 BilibiliVideoQRCodeOneBot 插件...")
        info("BilibiliVideoQRCodeOneBot 插件加载完成！")
    }

    override fun onEnable() {
        info("正在启动 BilibiliVideoQRCodeOneBot 插件...")
        info("BilibiliVideoQRCodeOneBot 插件启动完成！")
    }

    override fun onActive() {
        info("正在激活 BilibiliVideoQRCodeOneBot 插件...")
        info("BilibiliVideoQRCodeOneBot 插件激活完成！")
    }

    override fun onDisable() {
        info("正在禁用 BilibiliVideoQRCodeOneBot 插件...")
        info("BilibiliVideoQRCodeOneBot 插件禁用完成！")
    }
}
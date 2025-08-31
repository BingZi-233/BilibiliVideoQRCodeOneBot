package online.bingzi.bilibili.video.qrcode.onebot.internal.binding.config

import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration
import java.util.concurrent.TimeUnit

/**
 * 绑定功能配置管理类
 * 负责管理玩家与QQ账户绑定相关的核心配置项
 * 消息内容已迁移至语言文件 (lang/zh_CN.yml, lang/en_US.yml)
 */
object BindingConfig {
    
    @Config("binding.yml", autoReload = true)
    lateinit var config: Configuration
        private set
    
    @ConfigNode("verification.code-length", bind = "binding.yml")
    var codeLength: Int = 6
    
    @ConfigNode("verification.expiry-minutes", bind = "binding.yml")
    var expiryMinutes: Int = 5
    
    @ConfigNode("commands.bind.enabled", bind = "binding.yml")
    var bindCommandEnabled: Boolean = true
    
    @ConfigNode("commands.bind.aliases", bind = "binding.yml")
    var bindCommandAliases: List<String> = listOf("qqbind", "bind")
    
    @ConfigNode("commands.unbind.enabled", bind = "binding.yml")
    var unbindCommandEnabled: Boolean = true
    
    @ConfigNode("commands.unbind.aliases", bind = "binding.yml")
    var unbindCommandAliases: List<String> = listOf("qqunbind", "unbind")
    
    /**
     * 获取验证码过期时间（毫秒）
     */
    fun getExpiryTimeMillis(): Long {
        return TimeUnit.MINUTES.toMillis(expiryMinutes.toLong())
    }
    
    /**
     * 重载配置
     * 可在运行时调用此方法重新加载配置文件
     */
    fun reload() {
        if (::config.isInitialized) {
            config.reload()
        }
    }
}
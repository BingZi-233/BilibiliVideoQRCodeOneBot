package online.bingzi.bilibili.video.qrcode.onebot.internal.binding

import online.bingzi.bilibili.video.qrcode.onebot.api.binding.BindingService
import online.bingzi.bilibili.video.qrcode.onebot.api.binding.PlayerQQBinding
import online.bingzi.bilibili.video.qrcode.onebot.api.verification.VerificationService
import online.bingzi.bilibili.video.qrcode.onebot.internal.binding.config.BindingConfig
import online.bingzi.bilibili.video.qrcode.onebot.internal.binding.helper.BindingHelper
import online.bingzi.bilibili.video.qrcode.onebot.internal.database.service.BindingDatabaseService
import online.bingzi.bilibili.video.qrcode.onebot.internal.verification.VerificationCodeGenerator
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

object BindingManager : BindingService {
    
    private val verificationService: VerificationService = VerificationCodeGenerator()
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    
    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        info("绑定管理器已启用")
        
        // 每分钟清理一次过期的绑定请求
        scheduler.scheduleAtFixedRate({
            try {
                BindingHelper.cleanExpiredRequests()
            } catch (e: Exception) {
                info("清理过期绑定请求时发生错误: ${e.message}")
            }
        }, 1, 1, TimeUnit.MINUTES)
    }
    
    @Awake(LifeCycle.DISABLE)
    fun onDisable() {
        info("绑定管理器正在关闭...")
        scheduler.shutdown()
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow()
            }
        } catch (e: InterruptedException) {
            scheduler.shutdownNow()
        }
        info("绑定管理器已关闭")
    }
    
    override fun bindPlayer(playerUuid: String, qqNumber: Long, playerName: String, callback: (Boolean) -> Unit) {
        BindingDatabaseService.bindPlayer(playerUuid, qqNumber, playerName, callback)
    }
    
    override fun unbindPlayer(playerUuid: String, callback: (Boolean) -> Unit) {
        BindingDatabaseService.unbindPlayer(playerUuid, callback)
    }
    
    override fun getQQByPlayer(playerUuid: String, callback: (Long?) -> Unit) {
        BindingDatabaseService.getQQByPlayer(playerUuid, callback)
    }
    
    override fun getPlayerByQQ(qqNumber: Long, callback: (String?) -> Unit) {
        BindingDatabaseService.getPlayerByQQ(qqNumber, callback)
    }
    
    override fun getBindingByPlayer(playerUuid: String, callback: (PlayerQQBinding?) -> Unit) {
        BindingDatabaseService.getBindingByPlayer(playerUuid, callback)
    }
    
    override fun getBindingByQQ(qqNumber: Long, callback: (PlayerQQBinding?) -> Unit) {
        BindingDatabaseService.getBindingByQQ(qqNumber, callback)
    }
    
    override fun isPlayerBound(playerUuid: String, callback: (Boolean) -> Unit) {
        BindingDatabaseService.isPlayerBound(playerUuid, callback)
    }
    
    override fun isQQBound(qqNumber: Long, callback: (Boolean) -> Unit) {
        BindingDatabaseService.isQQBound(qqNumber, callback)
    }
    
    /**
     * 创建绑定请求，生成验证码
     * @param playerUuid 玩家UUID
     * @param playerName 玩家名称
     * @return 生成的验证码
     */
    fun createBindingRequest(playerUuid: String, playerName: String): String {
        val code = verificationService.generateCode(playerUuid, BindingConfig.codeLength)
        BindingHelper.createBindingRequest(
            playerUuid, 
            playerName, 
            code, 
            BindingConfig.getExpiryTimeMillis()
        )
        return code
    }
    
    /**
     * 处理来自QQ的绑定请求
     * @param qqNumber 发起绑定的QQ号
     * @param code 验证码
     * @param callback 回调函数，返回处理结果和消息内容
     */
    fun processQQBindingRequest(qqNumber: Long, code: String, callback: (Boolean, String) -> Unit) {
        // 验证验证码格式
        if (!BindingHelper.isValidCode(code, BindingConfig.codeLength)) {
            callback(false, "❌ 验证码格式错误！请发送 6 位数字验证码。")
            return
        }
        
        // 查找对应的绑定请求
        val bindingRequest = BindingHelper.findBindingRequestByCode(code)
        if (bindingRequest == null) {
            callback(false, "❌ 未找到绑定请求！请先在游戏中执行 /qqbind 命令。")
            return
        }
        
        // 验证码有效性检查
        if (!verificationService.verifyCode(bindingRequest.playerUuid, code)) {
            callback(false, "❌ 绑定失败！验证码无效或已过期。")
            return
        }
        
        // 检查QQ是否已绑定其他账户
        isQQBound(qqNumber) { alreadyBound ->
            if (alreadyBound) {
                callback(false, "❌ 此QQ号已绑定其他玩家！\n如需重新绑定请联系管理员。")
                return@isQQBound
            }
            
            // 执行绑定操作
            bindPlayer(bindingRequest.playerUuid, qqNumber, bindingRequest.playerName) { success ->
                if (success) {
                    // 绑定成功，移除请求和验证码
                    BindingHelper.removeBindingRequest(bindingRequest.playerUuid)
                    verificationService.clearCode(bindingRequest.playerUuid)
                    
                    // 通知游戏内玩家
                    submit {
                        val player = BindingHelper.getOnlinePlayerByUuid(bindingRequest.playerUuid)
                        player?.sendMessage("§a绑定成功！你的QQ号 §e$qqNumber §a已成功绑定到你的游戏账户。")
                    }
                    
                    // 返回成功消息
                    val bindTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())
                    callback(true, "✅ 绑定成功！\n玩家：${bindingRequest.playerName}\nQQ：$qqNumber\n时间：$bindTime")
                } else {
                    callback(false, "❌ 绑定失败！验证码无效或已过期。")
                }
            }
        }
    }
    
    /**
     * 处理来自QQ的解绑请求
     * @param qqNumber 发起解绑的QQ号
     * @param callback 回调函数，返回处理结果和消息内容
     */
    fun processQQUnbindRequest(qqNumber: Long, callback: (Boolean, String) -> Unit) {
        // 检查是否有绑定关系
        getBindingByQQ(qqNumber) { binding ->
            if (binding == null) {
                callback(false, "❌ 你还没有绑定任何游戏账户。")
                return@getBindingByQQ
            }
            
            // 执行解绑操作
            unbindPlayer(binding.playerUuid) { success ->
                if (success) {
                    // 通知游戏内玩家
                    submit {
                        val player = BindingHelper.getOnlinePlayerByUuid(binding.playerUuid)
                        player?.sendMessage("§c解绑成功！你的QQ号 §e$qqNumber §c已与游戏账户解除绑定。")
                    }
                    callback(true, "✅ 解绑成功！")
                } else {
                    callback(false, "❌ 解绑失败！请稍后重试。")
                }
            }
        }
    }
    
    /**
     * 处理来自QQ的信息查询请求
     * @param qqNumber 查询者的QQ号
     * @param callback 回调函数，返回查询结果和消息内容
     */
    fun processQQInfoRequest(qqNumber: Long, callback: (Boolean, String) -> Unit) {
        getBindingByQQ(qqNumber) { binding ->
            if (binding == null) {
                callback(false, "❌ 你还没有绑定任何游戏账户。")
            } else {
                val bindTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(java.util.Date(binding.createTime))
                callback(true, "📊 绑定信息\n玩家：${binding.playerName}\nQQ：${binding.qqNumber}\n绑定时间：$bindTime")
            }
        }
    }
    
    /**
     * 获取绑定请求信息
     * @param playerUuid 玩家UUID
     * @return 绑定请求信息，如果不存在或已过期则返回null
     */
    fun getBindingRequest(playerUuid: String): BindingHelper.BindingRequest? {
        return BindingHelper.getBindingRequest(playerUuid)
    }
    
    /**
     * 移除绑定请求
     * @param playerUuid 玩家UUID
     * @return 是否成功移除
     */
    fun removeBindingRequest(playerUuid: String): Boolean {
        return BindingHelper.removeBindingRequest(playerUuid)
    }
}
package online.bingzi.bilibili.video.qrcode.onebot.internal.database.manager

import online.bingzi.bilibili.video.qrcode.onebot.api.binding.BindingService
import online.bingzi.bilibili.video.qrcode.onebot.api.binding.PlayerQQBinding
import online.bingzi.bilibili.video.qrcode.onebot.internal.database.config.DatabaseConfig
import online.bingzi.bilibili.video.qrcode.onebot.internal.database.service.BindingDatabaseService
import online.bingzi.bilibili.video.qrcode.onebot.internal.database.table.BindingAuditLogTable
import online.bingzi.bilibili.video.qrcode.onebot.internal.database.table.PlayerQQBindingTable
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

/**
 * 数据库管理器
 * 负责数据库初始化和提供对外服务实例
 */
object DatabaseManager : BindingService {
    
    /**
     * 初始化数据库连接和表结构
     */
    @Awake(LifeCycle.ENABLE)
    fun init() {
        try {
            val host = DatabaseConfig.createHost()
            val dataSource = DatabaseConfig.createDataSource()
            
            // 创建玩家QQ绑定关系表
            PlayerQQBindingTable.initTable(host, dataSource)
            
            // 创建绑定审计日志表
            BindingAuditLogTable.initTable(host, dataSource)
            
            println("数据库初始化完成")
        } catch (e: Exception) {
            println("数据库初始化失败: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // 实现 BindingService 接口，委托给 BindingDatabaseService
    
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
}
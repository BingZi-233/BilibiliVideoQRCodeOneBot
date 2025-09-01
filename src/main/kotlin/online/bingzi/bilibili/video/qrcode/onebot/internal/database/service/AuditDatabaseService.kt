package online.bingzi.bilibili.video.qrcode.onebot.internal.database.service

import online.bingzi.bilibili.video.qrcode.onebot.internal.database.config.DatabaseConfig
import online.bingzi.bilibili.video.qrcode.onebot.internal.database.table.BindingAuditLogTable
import taboolib.common.platform.function.severe
import taboolib.common.platform.function.submitAsync
import taboolib.module.database.Host
import taboolib.module.database.HostSQLite
import taboolib.module.database.Order

/**
 * 绑定审计日志数据库服务
 * 记录所有绑定操作的审计日志
 */
object AuditDatabaseService {
    
    private val host: Host<*> by lazy { DatabaseConfig.createHost() }
    private val dataSource by lazy { DatabaseConfig.createDataSource() }
    private val table by lazy { BindingAuditLogTable.createTable(host) }
    
    /**
     * 操作类型枚举
     */
    enum class AuditAction {
        BIND,      // 绑定操作
        UNBIND,    // 解绑操作
        UPDATE     // 更新操作
    }
    
    /**
     * 记录审计日志
     * @param playerUuid 玩家UUID
     * @param qqNumber QQ号
     * @param action 操作类型
     * @param success 操作是否成功
     * @param reason 失败原因或备注
     * @param operator 操作者
     */
    fun logAudit(
        playerUuid: String,
        qqNumber: Long,
        action: AuditAction,
        success: Boolean,
        reason: String? = null,
        operator: String
    ) {
        submitAsync {
            try {
                table.insert(dataSource,
                    "player_uuid", "qq_number", "action", "success", 
                    "reason", "operator", "timestamp"
                ) {
                    value(playerUuid, qqNumber, action.name, 
                          if (host is HostSQLite) if (success) 1 else 0 else success,
                          reason, operator, System.currentTimeMillis())
                }
            } catch (e: Exception) {
                severe("记录审计日志失败: ${e.message}")
            }
        }
    }
    
    /**
     * 获取玩家的审计日志
     * @param playerUuid 玩家UUID
     * @param limit 限制条数
     * @param callback 回调函数，返回审计日志列表
     */
    fun getAuditLogsByPlayer(playerUuid: String, limit: Int = 50, callback: (List<AuditLog>) -> Unit) {
        submitAsync {
            try {
                val logs = table.select(dataSource) {
                    where { "player_uuid" eq playerUuid }
                    orderBy("timestamp", Order.Type.DESC)
                    if (limit > 0) limit(limit)
                }.map {
                    AuditLog(
                        id = getLong("id"),
                        playerUuid = getString("player_uuid"),
                        qqNumber = getLong("qq_number"),
                        action = AuditAction.valueOf(getString("action")),
                        success = if (host is HostSQLite) getInt("success") == 1 else getBoolean("success"),
                        reason = getString("reason"),
                        operator = getString("operator"),
                        timestamp = getLong("timestamp")
                    )
                }
                
                callback(logs)
            } catch (e: Exception) {
                severe("查询审计日志失败: ${e.message}")
                callback(emptyList())
            }
        }
    }
    
    /**
     * 获取QQ号的审计日志
     * @param qqNumber QQ号
     * @param limit 限制条数
     * @param callback 回调函数，返回审计日志列表
     */
    fun getAuditLogsByQQ(qqNumber: Long, limit: Int = 50, callback: (List<AuditLog>) -> Unit) {
        submitAsync {
            try {
                val logs = table.select(dataSource) {
                    where { "qq_number" eq qqNumber }
                    orderBy("timestamp", Order.Type.DESC)
                    if (limit > 0) limit(limit)
                }.map {
                    AuditLog(
                        id = getLong("id"),
                        playerUuid = getString("player_uuid"),
                        qqNumber = getLong("qq_number"),
                        action = AuditAction.valueOf(getString("action")),
                        success = if (host is HostSQLite) getInt("success") == 1 else getBoolean("success"),
                        reason = getString("reason"),
                        operator = getString("operator"),
                        timestamp = getLong("timestamp")
                    )
                }
                
                callback(logs)
            } catch (e: Exception) {
                severe("查询审计日志失败: ${e.message}")
                callback(emptyList())
            }
        }
    }
    
    /**
     * 按时间范围获取审计日志
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @param limit 限制条数
     * @param callback 回调函数，返回审计日志列表
     */
    fun getAuditLogsByTimeRange(
        startTime: Long,
        endTime: Long,
        limit: Int = 100,
        callback: (List<AuditLog>) -> Unit
    ) {
        submitAsync {
            try {
                val logs = table.select(dataSource) {
                    where { 
                        "timestamp" gte startTime
                        "timestamp" lte endTime
                    }
                    orderBy("timestamp", Order.Type.DESC)
                    if (limit > 0) limit(limit)
                }.map {
                    AuditLog(
                        id = getLong("id"),
                        playerUuid = getString("player_uuid"),
                        qqNumber = getLong("qq_number"),
                        action = AuditAction.valueOf(getString("action")),
                        success = if (host is HostSQLite) getInt("success") == 1 else getBoolean("success"),
                        reason = getString("reason"),
                        operator = getString("operator"),
                        timestamp = getLong("timestamp")
                    )
                }
                
                callback(logs)
            } catch (e: Exception) {
                severe("查询审计日志失败: ${e.message}")
                callback(emptyList())
            }
        }
    }
    
    /**
     * 审计日志数据类
     */
    data class AuditLog(
        val id: Long,
        val playerUuid: String,
        val qqNumber: Long,
        val action: AuditAction,
        val success: Boolean,
        val reason: String?,
        val operator: String,
        val timestamp: Long
    )
}
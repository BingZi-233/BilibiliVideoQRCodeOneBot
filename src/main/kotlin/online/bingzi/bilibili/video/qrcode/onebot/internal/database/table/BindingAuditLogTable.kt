package online.bingzi.bilibili.video.qrcode.onebot.internal.database.table

import online.bingzi.bilibili.video.qrcode.onebot.internal.database.config.DatabaseConfig
import taboolib.module.database.*
import javax.sql.DataSource

/**
 * 绑定审计日志表定义
 * 记录所有绑定操作的历史记录
 */
object BindingAuditLogTable {
    
    /**
     * 创建绑定审计日志表
     * 根据Host类型自动适配MySQL或SQLite
     */
    fun createTable(host: Host<*>): Table<*, *> {
        val tableName = DatabaseConfig.getTableName("binding_audit_log")
        
        return when (host) {
            is HostSQL -> {
                // MySQL实现
                Table(tableName, host) {
                    add("id") {
                        type(ColumnTypeSQL.BIGINT) {
                            options(ColumnOptionSQL.PRIMARY_KEY, ColumnOptionSQL.AUTO_INCREMENT)
                        }
                    }
                    add("player_uuid") {
                        type(ColumnTypeSQL.VARCHAR, 36) {
                            options(ColumnOptionSQL.NOTNULL)
                        }
                    }
                    add("qq_number") {
                        type(ColumnTypeSQL.BIGINT) {
                            options(ColumnOptionSQL.NOTNULL)
                        }
                    }
                    add("action") {
                        type(ColumnTypeSQL.VARCHAR, 20) {
                            options(ColumnOptionSQL.NOTNULL)
                        }
                    }
                    add("success") {
                        type(ColumnTypeSQL.BOOLEAN) {
                            options(ColumnOptionSQL.NOTNULL)
                        }
                    }
                    add("reason") {
                        type(ColumnTypeSQL.VARCHAR, 255)
                    }
                    add("operator") {
                        type(ColumnTypeSQL.VARCHAR, 64) {
                            options(ColumnOptionSQL.NOTNULL)
                        }
                    }
                    add("timestamp") {
                        type(ColumnTypeSQL.BIGINT) {
                            options(ColumnOptionSQL.NOTNULL)
                        }
                    }
                }.also { table ->
                    // 添加查询优化索引
                    table.index("idx_player_uuid", listOf("player_uuid"))
                    table.index("idx_timestamp", listOf("timestamp"))
                    table.index("idx_action", listOf("action"))
                }
            }
            
            is HostSQLite -> {
                // SQLite实现
                Table(tableName, host) {
                    add("id") {
                        type(ColumnTypeSQLite.INTEGER) {
                            options(ColumnOptionSQLite.PRIMARY_KEY, ColumnOptionSQLite.AUTOINCREMENT)
                        }
                    }
                    add("player_uuid") {
                        type(ColumnTypeSQLite.TEXT) {
                            options(ColumnOptionSQLite.NOTNULL)
                        }
                    }
                    add("qq_number") {
                        type(ColumnTypeSQLite.INTEGER) {
                            options(ColumnOptionSQLite.NOTNULL)
                        }
                    }
                    add("action") {
                        type(ColumnTypeSQLite.TEXT) {
                            options(ColumnOptionSQLite.NOTNULL)
                        }
                    }
                    add("success") {
                        type(ColumnTypeSQLite.INTEGER) {
                            options(ColumnOptionSQLite.NOTNULL)
                        }
                    }
                    add("reason") {
                        type(ColumnTypeSQLite.TEXT)
                    }
                    add("operator") {
                        type(ColumnTypeSQLite.TEXT) {
                            options(ColumnOptionSQLite.NOTNULL)
                        }
                    }
                    add("timestamp") {
                        type(ColumnTypeSQLite.INTEGER) {
                            options(ColumnOptionSQLite.NOTNULL)
                        }
                    }
                }.also { table ->
                    // 添加查询优化索引
                    table.index("idx_player_uuid", listOf("player_uuid"))
                    table.index("idx_timestamp", listOf("timestamp"))
                    table.index("idx_action", listOf("action"))
                }
            }
            
            else -> {
                throw IllegalArgumentException("未知的数据库类型: ${host.javaClass}")
            }
        }
    }

    /**
     * 初始化表结构
     */
    fun initTable(host: Host<*>, dataSource: DataSource) {
        val table = createTable(host)
        table.createTable(dataSource)
    }
}
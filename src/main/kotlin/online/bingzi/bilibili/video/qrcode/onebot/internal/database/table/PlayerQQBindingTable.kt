package online.bingzi.bilibili.video.qrcode.onebot.internal.database.table

import online.bingzi.bilibili.video.qrcode.onebot.internal.database.config.DatabaseConfig
import taboolib.module.database.*
import javax.sql.DataSource

/**
 * 玩家QQ绑定关系表定义
 * 支持MySQL和SQLite双数据库适配
 */
object PlayerQQBindingTable {
    
    /**
     * 创建玩家QQ绑定表
     * 根据Host类型自动适配MySQL或SQLite
     */
    fun createTable(host: Host<*>): Table<*, *> {
        val tableName = DatabaseConfig.getTableName("player_qq_binding")
        
        return when (host) {
            is HostSQL -> {
                // MySQL实现
                Table(tableName, host) {
                    add("player_uuid") {
                        type(ColumnTypeSQL.VARCHAR, 36) {
                            options(ColumnOptionSQL.PRIMARY_KEY)
                        }
                    }
                    add("qq_number") {
                        type(ColumnTypeSQL.BIGINT) {
                            options(ColumnOptionSQL.UNIQUE_KEY, ColumnOptionSQL.NOTNULL)
                        }
                    }
                    add("player_name") {
                        type(ColumnTypeSQL.VARCHAR, 64) {
                            options(ColumnOptionSQL.NOTNULL)
                        }
                    }
                    add("create_time") {
                        type(ColumnTypeSQL.BIGINT) {
                            options(ColumnOptionSQL.NOTNULL)
                        }
                    }
                    add("update_time") {
                        type(ColumnTypeSQL.BIGINT) {
                            options(ColumnOptionSQL.NOTNULL)
                        }
                    }
                    add("create_player") {
                        type(ColumnTypeSQL.VARCHAR, 64) {
                            options(ColumnOptionSQL.NOTNULL)
                        }
                    }
                    add("update_player") {
                        type(ColumnTypeSQL.VARCHAR, 64) {
                            options(ColumnOptionSQL.NOTNULL)
                        }
                    }
                }.also { table ->
                    // 添加查询优化索引
                    table.index("idx_update_time", listOf("update_time"))
                }
            }
            
            is HostSQLite -> {
                // SQLite实现
                Table(tableName, host) {
                    add("player_uuid") {
                        type(ColumnTypeSQLite.TEXT) {
                            options(ColumnOptionSQLite.PRIMARY_KEY)
                        }
                    }
                    add("qq_number") {
                        type(ColumnTypeSQLite.INTEGER) {
                            options(ColumnOptionSQLite.NOTNULL)
                        }
                    }
                    add("player_name") {
                        type(ColumnTypeSQLite.TEXT) {
                            options(ColumnOptionSQLite.NOTNULL)
                        }
                    }
                    add("create_time") {
                        type(ColumnTypeSQLite.INTEGER) {
                            options(ColumnOptionSQLite.NOTNULL)
                        }
                    }
                    add("update_time") {
                        type(ColumnTypeSQLite.INTEGER) {
                            options(ColumnOptionSQLite.NOTNULL)
                        }
                    }
                    add("create_player") {
                        type(ColumnTypeSQLite.TEXT) {
                            options(ColumnOptionSQLite.NOTNULL)
                        }
                    }
                    add("update_player") {
                        type(ColumnTypeSQLite.TEXT) {
                            options(ColumnOptionSQLite.NOTNULL)
                        }
                    }
                }.also { table ->
                    // SQLite中使用索引实现唯一约束
                    table.index("unique_qq_number", listOf("qq_number"), unique = true)
                    // 添加查询优化索引
                    table.index("idx_update_time", listOf("update_time"))
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
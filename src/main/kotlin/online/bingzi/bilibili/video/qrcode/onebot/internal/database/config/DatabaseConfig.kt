package online.bingzi.bilibili.video.qrcode.onebot.internal.database.config

import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration
import taboolib.module.database.Database
import taboolib.module.database.Host
import taboolib.module.database.HostSQL
import taboolib.module.database.HostSQLite
import java.io.File
import javax.sql.DataSource

/**
 * 数据库配置管理
 * 精简配置
 */
@ConfigNode(bind = "database.yml")
object DatabaseConfig {
    
    @Config("database.yml")
    lateinit var config: Configuration
        private set
    
    @ConfigNode("type")
    var type: String = "sqlite"  // "mysql" or "sqlite"
    
    @ConfigNode("mysql.host")
    var mysqlHost: String = "localhost"
    
    @ConfigNode("mysql.port")
    var mysqlPort: Int = 3306
    
    @ConfigNode("mysql.database")
    var mysqlDatabase: String = "onebot"
    
    @ConfigNode("mysql.user")
    var mysqlUser: String = "root"
    
    @ConfigNode("mysql.password")
    var mysqlPassword: String = ""
    
    @ConfigNode("mysql.useSSL")
    var mysqlUseSSL: Boolean = false
    
    @ConfigNode("sqlite.file")
    var sqliteFile: String = "data/database.db"
    
    @ConfigNode("tablePrefix")
    var tablePrefix: String = "onebot_"
    
    /**
     * 创建 Host 对象
     */
    fun createHost(): Host<*> = when (type.lowercase()) {
        "mysql" -> createMySQLHost()
        "sqlite" -> HostSQLite(getSqliteFile())
        else -> throw IllegalArgumentException("未知的数据库类型: $type")
    }
    
    /**
     * 创建 DataSource 对象
     */
    fun createDataSource(): DataSource = Database.createDataSource(createHost())
    
    /**
     * 获取带前缀的表名
     */
    fun getTableName(name: String): String = tablePrefix + name
    
    /**
     * 创建 MySQL Host
     */
    private fun createMySQLHost(): HostSQL {
        return HostSQL(
            host = mysqlHost,
            port = mysqlPort.toString(),
            user = mysqlUser,
            password = mysqlPassword,
            database = mysqlDatabase
        ).apply {
            flags.clear()
            flags.addAll(listOf(
                "characterEncoding=utf8mb4",
                "useSSL=$mysqlUseSSL",
                "allowPublicKeyRetrieval=true",
                "autoReconnect=true",
                "useUnicode=true",
                "serverTimezone=Asia/Shanghai"
            ))
        }
    }
    
    /**
     * 获取 SQLite 文件
     */
    private fun getSqliteFile(): File {
        val file = if (sqliteFile.startsWith("/")) {
            File(sqliteFile)
        } else {
            newFile(getDataFolder(), sqliteFile)
        }
        
        // 确保父目录存在
        file.parentFile?.mkdirs()
        
        return file
    }
}
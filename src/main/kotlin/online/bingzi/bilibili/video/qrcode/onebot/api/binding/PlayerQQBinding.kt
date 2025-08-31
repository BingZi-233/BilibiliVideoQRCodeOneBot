package online.bingzi.bilibili.video.qrcode.onebot.api.binding

/**
 * 玩家QQ绑定关系数据实体
 */
data class PlayerQQBinding(
    /**
     * 玩家UUID
     */
    val playerUuid: String,
    
    /**
     * QQ号
     */
    val qqNumber: Long,
    
    /**
     * 玩家名称
     */
    val playerName: String,
    
    /**
     * 创建时间（毫秒时间戳）
     */
    val createTime: Long,
    
    /**
     * 更新时间（毫秒时间戳）
     */
    val updateTime: Long,
    
    /**
     * 创建者玩家名称
     */
    val createPlayer: String,
    
    /**
     * 更新者玩家名称
     */
    val updatePlayer: String
)
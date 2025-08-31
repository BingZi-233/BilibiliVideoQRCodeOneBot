package online.bingzi.bilibili.video.qrcode.onebot.api.binding

/**
 * 绑定服务接口
 * 提供玩家和QQ号绑定关系的公开API
 */
interface BindingService {
    
    /**
     * 绑定玩家与QQ号
     * @param playerUuid 玩家UUID
     * @param qqNumber QQ号
     * @param playerName 玩家名称
     * @param callback 回调函数，返回绑定是否成功
     */
    fun bindPlayer(playerUuid: String, qqNumber: Long, playerName: String, callback: (Boolean) -> Unit)
    
    /**
     * 解绑玩家
     * @param playerUuid 玩家UUID
     * @param callback 回调函数，返回解绑是否成功
     */
    fun unbindPlayer(playerUuid: String, callback: (Boolean) -> Unit)
    
    /**
     * 通过玩家UUID获取QQ号
     * @param playerUuid 玩家UUID
     * @param callback 回调函数，返回QQ号（如果未绑定则为null）
     */
    fun getQQByPlayer(playerUuid: String, callback: (Long?) -> Unit)
    
    /**
     * 通过QQ号获取玩家UUID
     * @param qqNumber QQ号
     * @param callback 回调函数，返回玩家UUID（如果未绑定则为null）
     */
    fun getPlayerByQQ(qqNumber: Long, callback: (String?) -> Unit)
    
    /**
     * 获取完整的绑定信息（通过玩家UUID）
     * @param playerUuid 玩家UUID
     * @param callback 回调函数，返回绑定信息（如果未绑定则为null）
     */
    fun getBindingByPlayer(playerUuid: String, callback: (PlayerQQBinding?) -> Unit)
    
    /**
     * 获取完整的绑定信息（通过QQ号）
     * @param qqNumber QQ号
     * @param callback 回调函数，返回绑定信息（如果未绑定则为null）
     */
    fun getBindingByQQ(qqNumber: Long, callback: (PlayerQQBinding?) -> Unit)
    
    /**
     * 检查玩家是否已绑定
     * @param playerUuid 玩家UUID
     * @param callback 回调函数，返回是否已绑定
     */
    fun isPlayerBound(playerUuid: String, callback: (Boolean) -> Unit)
    
    /**
     * 检查QQ号是否已绑定
     * @param qqNumber QQ号
     * @param callback 回调函数，返回是否已绑定
     */
    fun isQQBound(qqNumber: Long, callback: (Boolean) -> Unit)
}
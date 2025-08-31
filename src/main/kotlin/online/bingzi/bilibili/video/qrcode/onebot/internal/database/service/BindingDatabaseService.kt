package online.bingzi.bilibili.video.qrcode.onebot.internal.database.service

import online.bingzi.bilibili.video.qrcode.onebot.api.binding.PlayerQQBinding
import online.bingzi.bilibili.video.qrcode.onebot.internal.database.config.DatabaseConfig
import online.bingzi.bilibili.video.qrcode.onebot.internal.database.service.AuditDatabaseService.AuditAction
import online.bingzi.bilibili.video.qrcode.onebot.internal.database.table.PlayerQQBindingTable
import taboolib.common.platform.function.severe
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.submitAsync
import taboolib.module.database.Host

/**
 * 绑定关系数据库服务
 * 提供玩家和QQ号绑定关系的数据库操作
 */
object BindingDatabaseService {

    private val host: Host<*> by lazy { DatabaseConfig.createHost() }
    private val dataSource by lazy { DatabaseConfig.createDataSource() }
    private val table by lazy { PlayerQQBindingTable.createTable(host) }

    /**
     * 绑定玩家与QQ号
     * @param playerUuid 玩家UUID
     * @param qqNumber QQ号
     * @param playerName 玩家名称
     * @param callback 回调函数，返回绑定是否成功
     */
    fun bindPlayer(playerUuid: String, qqNumber: Long, playerName: String, callback: (Boolean) -> Unit) {
        submitAsync {
            try {
                val currentTime = System.currentTimeMillis()

                // 检查是否已存在该玩家的绑定记录
                val playerBindingQQ = table.select(dataSource) {
                    where { "player_uuid" eq playerUuid }
                }.firstOrNull {
                    getLong("qq_number")
                }

                // 检查是否已存在该QQ的绑定记录
                val qqBindingPlayer = table.select(dataSource) {
                    where { "qq_number" eq qqNumber }
                }.firstOrNull {
                    getString("player_uuid")
                }

                val result = when {
                    // 如果玩家和QQ都已绑定，检查是否绑定的是彼此
                    playerBindingQQ != null && qqBindingPlayer != null -> {
                        if (playerBindingQQ == qqNumber && qqBindingPlayer == playerUuid) {
                            // 已经正确绑定，更新时间即可
                            val updateSuccess = table.update(dataSource) {
                                set("update_time", currentTime)
                                set("update_player", playerName)
                                where { "player_uuid" eq playerUuid }
                            } > 0

                            if (updateSuccess) {
                                AuditDatabaseService.logAudit(
                                    playerUuid, qqNumber, AuditAction.UPDATE, true,
                                    "更新现有绑定关系", playerName
                                )
                            } else {
                                AuditDatabaseService.logAudit(
                                    playerUuid, qqNumber, AuditAction.UPDATE, false,
                                    "更新绑定关系失败", playerName
                                )
                            }
                            updateSuccess
                        } else {
                            // 存在冲突绑定，返回失败
                            val reason = when {
                                playerBindingQQ != qqNumber -> "玩家已绑定其他QQ: $playerBindingQQ"
                                qqBindingPlayer != playerUuid -> "QQ已绑定其他玩家: $qqBindingPlayer"
                                else -> "存在冲突绑定"
                            }
                            AuditDatabaseService.logAudit(
                                playerUuid, qqNumber, AuditAction.BIND, false, reason, playerName
                            )
                            false
                        }
                    }

                    // 如果玩家已绑定其他QQ或QQ已绑定其他玩家
                    playerBindingQQ != null || qqBindingPlayer != null -> {
                        val reason = when {
                            playerBindingQQ != null -> "玩家已绑定其他QQ: $playerBindingQQ"
                            qqBindingPlayer != null -> "QQ已绑定其他玩家: $qqBindingPlayer"
                            else -> "绑定冲突"
                        }
                        AuditDatabaseService.logAudit(
                            playerUuid, qqNumber, AuditAction.BIND, false, reason, playerName
                        )
                        false
                    }

                    // 都没有绑定，创建新绑定记录
                    else -> {
                        val insertSuccess = table.insert(dataSource,
                            "player_uuid", "qq_number", "player_name", 
                            "create_time", "update_time", "create_player", "update_player"
                        ) {
                            value(playerUuid, qqNumber, playerName, 
                                  currentTime, currentTime, playerName, playerName)
                        } > 0

                        if (insertSuccess) {
                            AuditDatabaseService.logAudit(
                                playerUuid, qqNumber, AuditAction.BIND, true,
                                "创建新的绑定关系", playerName
                            )
                        } else {
                            AuditDatabaseService.logAudit(
                                playerUuid, qqNumber, AuditAction.BIND, false,
                                "创建绑定关系失败", playerName
                            )
                        }
                        insertSuccess
                    }
                }

                submit { callback(result) }
            } catch (e: Exception) {
                submit {
                    severe("绑定玩家失败: ${e.message}")
                    callback(false)
                }
            }
        }
    }

    /**
     * 解绑玩家
     * @param playerUuid 玩家UUID
     * @param callback 回调函数，返回解绑是否成功
     */
    fun unbindPlayer(playerUuid: String, callback: (Boolean) -> Unit) {
        unbindPlayer(playerUuid, "系统", callback)
    }

    /**
     * 解绑玩家
     * @param playerUuid 玩家UUID
     * @param operator 操作者名称
     * @param callback 回调函数，返回解绑是否成功
     */
    fun unbindPlayer(playerUuid: String, operator: String, callback: (Boolean) -> Unit) {
        submitAsync {
            try {
                // 先查询现有绑定信息用于审计日志
                val qqNumber = table.select(dataSource) {
                    where { "player_uuid" eq playerUuid }
                }.firstOrNull {
                    getLong("qq_number")
                }

                val result = table.delete(dataSource) {
                    where { "player_uuid" eq playerUuid }
                } > 0

                // 记录审计日志
                if (qqNumber != null) {
                    if (result) {
                        AuditDatabaseService.logAudit(
                            playerUuid, qqNumber, AuditAction.UNBIND, true,
                            "成功解除绑定关系", operator
                        )
                    } else {
                        AuditDatabaseService.logAudit(
                            playerUuid, qqNumber, AuditAction.UNBIND, false,
                            "解绑操作失败", operator
                        )
                    }
                } else if (!result) {
                    // 没有找到绑定关系但操作失败，仍记录日志
                    AuditDatabaseService.logAudit(
                        playerUuid, 0L, AuditAction.UNBIND, false,
                        "玩家未绑定任何QQ", operator
                    )
                }

                submit { callback(result) }
            } catch (e: Exception) {
                submit {
                    severe("解绑玩家失败: ${e.message}")
                    AuditDatabaseService.logAudit(
                        playerUuid, 0L, AuditAction.UNBIND, false,
                        "解绑操作异常: ${e.message}", operator
                    )
                    callback(false)
                }
            }
        }
    }

    /**
     * 通过玩家UUID获取QQ号
     * @param playerUuid 玩家UUID
     * @param callback 回调函数，返回QQ号（如果未绑定则为null）
     */
    fun getQQByPlayer(playerUuid: String, callback: (Long?) -> Unit) {
        submitAsync {
            try {
                val qqNumber = table.select(dataSource) {
                    where { "player_uuid" eq playerUuid }
                }.firstOrNull {
                    getLong("qq_number")
                }

                submit { callback(qqNumber) }
            } catch (e: Exception) {
                submit {
                    severe("查询玩家QQ失败: ${e.message}")
                    callback(null)
                }
            }
        }
    }

    /**
     * 通过QQ号获取玩家UUID
     * @param qqNumber QQ号
     * @param callback 回调函数，返回玩家UUID（如果未绑定则为null）
     */
    fun getPlayerByQQ(qqNumber: Long, callback: (String?) -> Unit) {
        submitAsync {
            try {
                val playerUuid = table.select(dataSource) {
                    where { "qq_number" eq qqNumber }
                }.firstOrNull {
                    getString("player_uuid")
                }

                submit { callback(playerUuid) }
            } catch (e: Exception) {
                submit {
                    severe("查询QQ绑定玩家失败: ${e.message}")
                    callback(null)
                }
            }
        }
    }

    /**
     * 获取完整的绑定信息
     * @param playerUuid 玩家UUID
     * @param callback 回调函数，返回绑定信息（如果未绑定则为null）
     */
    fun getBindingByPlayer(playerUuid: String, callback: (PlayerQQBinding?) -> Unit) {
        submitAsync {
            try {
                val binding = table.select(dataSource) {
                    where { "player_uuid" eq playerUuid }
                }.firstOrNull {
                    PlayerQQBinding(
                        playerUuid = getString("player_uuid"),
                        qqNumber = getLong("qq_number"),
                        playerName = getString("player_name"),
                        createTime = getLong("create_time"),
                        updateTime = getLong("update_time"),
                        createPlayer = getString("create_player"),
                        updatePlayer = getString("update_player")
                    )
                }

                submit { callback(binding) }
            } catch (e: Exception) {
                submit {
                    severe("查询绑定信息失败: ${e.message}")
                    callback(null)
                }
            }
        }
    }

    /**
     * 获取完整的绑定信息
     * @param qqNumber QQ号
     * @param callback 回调函数，返回绑定信息（如果未绑定则为null）
     */
    fun getBindingByQQ(qqNumber: Long, callback: (PlayerQQBinding?) -> Unit) {
        submitAsync {
            try {
                val binding = table.select(dataSource) {
                    where { "qq_number" eq qqNumber }
                }.firstOrNull {
                    PlayerQQBinding(
                        playerUuid = getString("player_uuid"),
                        qqNumber = getLong("qq_number"),
                        playerName = getString("player_name"),
                        createTime = getLong("create_time"),
                        updateTime = getLong("update_time"),
                        createPlayer = getString("create_player"),
                        updatePlayer = getString("update_player")
                    )
                }

                submit { callback(binding) }
            } catch (e: Exception) {
                submit {
                    severe("查询绑定信息失败: ${e.message}")
                    callback(null)
                }
            }
        }
    }

    /**
     * 检查玩家是否已绑定
     * @param playerUuid 玩家UUID
     * @param callback 回调函数，返回是否已绑定
     */
    fun isPlayerBound(playerUuid: String, callback: (Boolean) -> Unit) {
        getQQByPlayer(playerUuid) { qqNumber ->
            callback(qqNumber != null)
        }
    }

    /**
     * 检查QQ号是否已绑定
     * @param qqNumber QQ号
     * @param callback 回调函数，返回是否已绑定
     */
    fun isQQBound(qqNumber: Long, callback: (Boolean) -> Unit) {
        getPlayerByQQ(qqNumber) { playerUuid ->
            callback(playerUuid != null)
        }
    }
}
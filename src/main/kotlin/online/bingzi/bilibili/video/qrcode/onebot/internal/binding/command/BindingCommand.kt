package online.bingzi.bilibili.video.qrcode.onebot.internal.binding.command

import online.bingzi.bilibili.video.qrcode.onebot.internal.binding.BindingManager
import online.bingzi.bilibili.video.qrcode.onebot.internal.binding.config.BindingConfig
import online.bingzi.bilibili.video.qrcode.onebot.internal.binding.helper.BindingHelper
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.sendError
import taboolib.platform.util.sendInfo
import taboolib.platform.util.sendLang
import taboolib.platform.util.sendWarn

@CommandHeader(name = "qqbind", aliases = ["bind"], description = "QQ账户绑定命令")
object BindingCommand {
    
    @CommandBody
    val main = mainCommand {
        execute<CommandSender> { sender, _, _ ->
            if (sender is Player) {
                handleBindCommand(sender)
            } else {
                sender.sendLang("playerOnly")
            }
        }
    }
    
    @CommandBody
    val unbind = subCommand {
        execute<CommandSender> { sender, _, _ ->
            if (sender is Player) {
                handleUnbindCommand(sender)
            } else {
                sender.sendLang("playerOnly")
            }
        }
    }
    
    @CommandBody
    val info = subCommand {
        execute<CommandSender> { sender, _, _ ->
            handleInfoCommand(sender)
        }
    }
    
    @CommandBody
    val status = subCommand {
        execute<CommandSender> { sender, _, _ ->
            handleStatusCommand(sender)
        }
    }
    
    private fun handleBindCommand(player: Player) {
        val playerUuid = player.uniqueId.toString()
        val playerName = player.name
        
        BindingManager.isPlayerBound(playerUuid) { alreadyBound ->
            if (alreadyBound) {
                player.sendError("alreadyBound")
                return@isPlayerBound
            }
            
            // 检查是否已有进行中的绑定请求
            val existingRequest = BindingManager.getBindingRequest(playerUuid)
            if (existingRequest != null) {
                val remaining = BindingHelper.getRemainingTime(existingRequest.expiryTime)
                player.sendWarn("existingRequest")
                player.sendLang("requestCode", existingRequest.code)
                player.sendLang("requestRemaining", remaining)
                player.sendMessage("")
                player.sendLang("bindUsage", existingRequest.code, BindingConfig.expiryMinutes)
                return@isPlayerBound
            }
            
            // 创建新的绑定请求
            val code = BindingManager.createBindingRequest(playerUuid, playerName)
            
            // 显示验证码和使用说明
            player.sendMessage("")
            player.sendLang("codeGenerated", code, BindingConfig.expiryMinutes)
            player.sendMessage("")
            player.sendLang("bindUsage", code, BindingConfig.expiryMinutes)
        }
    }
    
    private fun handleUnbindCommand(player: Player) {
        val playerUuid = player.uniqueId.toString()
        
        BindingManager.isPlayerBound(playerUuid) { isBound ->
            if (!isBound) {
                player.sendError("notBound")
                return@isPlayerBound
            }
            
            // 询问确认
            player.sendWarn("unbindConfirm")
            
            // 这里可以进一步实现确认机制，但为了简化，我们直接执行解绑
            BindingManager.unbindPlayer(playerUuid) { success ->
                if (success) {
                    player.sendInfo("unbindSuccess")
                    
                    // 移除可能存在的绑定请求
                    BindingManager.removeBindingRequest(playerUuid)
                } else {
                    player.sendError("unbindFailed")
                }
            }
        }
    }
    
    private fun handleInfoCommand(sender: CommandSender) {
        if (sender is Player) {
            val playerUuid = sender.uniqueId.toString()
            
            BindingManager.getBindingByPlayer(playerUuid) { binding ->
                if (binding == null) {
                    sender.sendError("notBound")
                    sender.sendLang("bindHint")
                } else {
                    val bindTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(java.util.Date(binding.createTime))
                    
                    sender.sendLang("bindingInfoTitle")
                    sender.sendLang("bindingInfoPlayer", binding.playerName)
                    sender.sendLang("bindingInfoQQ", binding.qqNumber)
                    sender.sendLang("bindingInfoTime", bindTime)
                    
                    // 显示进行中的绑定请求信息（如果有）
                    val pendingRequest = BindingManager.getBindingRequest(playerUuid)
                    if (pendingRequest != null) {
                        val remaining = BindingHelper.getRemainingTime(pendingRequest.expiryTime)
                        sender.sendMessage("")
                        sender.sendLang("pendingRequestTitle")
                        sender.sendLang("pendingRequestCode", pendingRequest.code)
                        sender.sendLang("pendingRequestRemaining", remaining)
                    }
                }
            }
        } else {
            sender.sendLang("playerOnlySubCommand")
        }
    }
    
    private fun handleStatusCommand(sender: CommandSender) {
        // 检查OneBot连接状态
        val isOneBotConnected = try {
            online.bingzi.onebot.api.OneBotAPI.isConnected()
        } catch (e: Exception) {
            false
        }
        
        sender.sendLang("systemStatusTitle")
        if (isOneBotConnected) {
            sender.sendLang("onebotConnected")
        } else {
            sender.sendLang("onebotDisconnected")
        }
        
        if (sender is Player) {
            val playerUuid = sender.uniqueId.toString()
            
            BindingManager.isPlayerBound(playerUuid) { isBound ->
                if (isBound) {
                    sender.sendLang("bindingStatusBound")
                } else {
                    sender.sendLang("bindingStatusUnbound")
                }
                
                // 显示进行中的请求
                val pendingRequest = BindingManager.getBindingRequest(playerUuid)
                if (pendingRequest != null) {
                    val remaining = BindingHelper.getRemainingTime(pendingRequest.expiryTime)
                    sender.sendLang("pendingRequestExists")
                    sender.sendLang("pendingRequestCode", pendingRequest.code)
                    sender.sendLang("pendingRequestRemaining", remaining)
                } else {
                    sender.sendLang("pendingRequestNone")
                }
            }
        }
        
        sender.sendLang("availableCommands")
        sender.sendLang("commandBind")
        sender.sendLang("commandUnbind")
        sender.sendLang("commandInfo")
        sender.sendLang("commandStatus")
    }
}
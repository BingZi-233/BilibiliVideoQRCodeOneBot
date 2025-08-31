# OneBot 插件开发者指南

本文档面向希望在自己的Bukkit插件中集成OneBot功能的开发者。

## 目录

- [简介](#简介)
- [添加依赖](#添加依赖)
- [核心API](#核心api)
- [事件监听](#事件监听)
- [完整示例](#完整示例)
- [最佳实践](#最佳实践)
- [故障排除](#故障排除)

## 简介

OneBot插件提供了完整的公共API，允许其他Bukkit插件：
- 发送QQ消息（私聊/群聊）
- 监听QQ消息和事件
- 执行群管理操作
- 获取好友/群列表信息

所有API都采用异步回调模式，确保不会阻塞Minecraft服务器主线程。

## 添加依赖

### Maven

```xml
<repositories>
    <repository>
        <id>taboolib</id>
        <url>https://repo.tabooproject.org/repository/releases/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>online.bingzi</groupId>
        <artifactId>onebot</artifactId>
        <version>版本号</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Gradle

```kotlin
repositories {
    maven { url = uri("https://repo.tabooproject.org/repository/releases/") }
}

dependencies {
    compileOnly("online.bingzi:onebot:版本号")
}
```

### plugin.yml 配置

在您的插件的 `plugin.yml` 中添加依赖：

```yaml
depend: [OneBot]
# 或者软依赖
softdepend: [OneBot]
```

## 核心API

### OneBotAPI 类

`OneBotAPI` 是主要的公共接口，提供所有与OneBot交互的方法。

#### 连接状态检查

```kotlin
// 检查是否已连接到OneBot
val isConnected = OneBotAPI.isConnected()
```

#### 消息发送

所有消息发送方法都是异步的，使用回调函数处理结果。

##### 发送私聊消息

```kotlin
OneBotAPI.sendPrivateMessage(
    userId = 123456789L,           // QQ号
    message = "Hello!",            // 消息内容
    autoEscape = false,            // 是否作为纯文本发送
    callback = Consumer { success ->
        if (success) {
            println("私聊消息发送成功")
        } else {
            println("私聊消息发送失败")
        }
    }
)
```

##### 发送群消息

```kotlin
OneBotAPI.sendGroupMessage(
    groupId = 987654321L,          // 群号
    message = "Hello Group!",      // 消息内容
    autoEscape = false,            // 是否作为纯文本发送
    callback = Consumer { success ->
        if (success) {
            println("群消息发送成功")
        } else {
            println("群消息发送失败")
        }
    }
)
```

#### 消息管理

##### 撤回消息

```kotlin
OneBotAPI.deleteMessage(
    messageId = 12345,             // 消息ID
    callback = Consumer { success ->
        if (success) {
            println("消息撤回成功")
        } else {
            println("消息撤回失败")
        }
    }
)
```

#### 群管理

##### 禁言群成员

```kotlin
OneBotAPI.banGroupMember(
    groupId = 987654321L,          // 群号
    userId = 123456789L,           // 用户QQ号
    duration = 3600,               // 禁言时长（秒），0为解除禁言
    callback = Consumer { success ->
        if (success) {
            println("禁言操作成功")
        } else {
            println("禁言操作失败")
        }
    }
)
```

##### 踢出群成员

```kotlin
OneBotAPI.kickGroupMember(
    groupId = 987654321L,          // 群号
    userId = 123456789L,           // 用户QQ号
    rejectAddRequest = false,      // 是否拒绝此人的加群请求
    callback = Consumer { success ->
        if (success) {
            println("踢出成员成功")
        } else {
            println("踢出成员失败")
        }
    }
)
```

#### 信息获取

##### 获取好友列表

```kotlin
OneBotAPI.getFriendList(Consumer { jsonData ->
    if (jsonData != null) {
        println("好友列表: $jsonData")
        // 解析JSON数据获取好友信息
    } else {
        println("获取好友列表失败")
    }
})
```

##### 获取群列表

```kotlin
OneBotAPI.getGroupList(Consumer { jsonData ->
    if (jsonData != null) {
        println("群列表: $jsonData")
        // 解析JSON数据获取群信息
    } else {
        println("获取群列表失败")
    }
})
```

##### 获取群成员列表

```kotlin
OneBotAPI.getGroupMemberList(
    groupId = 987654321L,          // 群号
    callback = Consumer { jsonData ->
        if (jsonData != null) {
            println("群成员列表: $jsonData")
            // 解析JSON数据获取成员信息
        } else {
            println("获取群成员列表失败")
        }
    }
)
```

## 事件监听

OneBot插件提供了丰富的事件系统，您可以使用TabooLib的事件监听机制来处理这些事件。

### 事件类型层次结构

```
OneBotEvent (基类)
├── MessageEvent (消息事件基类)
│   ├── PrivateMessageEvent (私聊消息)
│   └── GroupMessageEvent (群消息)
├── NoticeEvent (通知事件基类)
│   ├── GroupIncreaseNotice (群成员增加)
│   ├── GroupDecreaseNotice (群成员减少)
│   ├── GroupBanNotice (群禁言通知)
│   └── FriendAddNotice (好友添加通知)
├── RequestEvent (请求事件基类)
│   ├── FriendRequestEvent (好友请求)
│   └── GroupRequestEvent (群请求)
└── StatusEvent (状态事件基类)
    ├── OneBotConnectedEvent (连接成功)
    ├── OneBotDisconnectedEvent (连接断开)
    └── OneBotReconnectingEvent (正在重连)
```

### 消息事件监听

#### 监听所有消息

```kotlin
@SubscribeEvent
fun onMessage(event: MessageEvent) {
    println("收到消息: ${event.message}")
    println("发送者: ${event.userId}")
    println("消息ID: ${event.messageId}")
    
    // 回复消息
    event.reply("我收到了你的消息!")
}
```

#### 监听私聊消息

```kotlin
@SubscribeEvent
fun onPrivateMessage(event: PrivateMessageEvent) {
    println("收到私聊消息: ${event.message}")
    println("发送者: ${event.userId}")
    
    // 回复私聊
    event.reply("收到你的私聊!")
    
    // 回复并引用原消息
    event.replyWithQuote("引用回复!")
}
```

#### 监听群消息

```kotlin
@SubscribeEvent
fun onGroupMessage(event: GroupMessageEvent) {
    println("收到群消息: ${event.message}")
    println("群号: ${event.groupId}")
    println("发送者: ${event.userId}")
    
    // 回复群消息
    event.reply("收到群消息!")
    
    // @某人回复
    event.replyWithAt("@你好!")
    
    // 引用回复
    event.replyWithQuote("引用你的消息")
}
```

### 通知事件监听

#### 监听群成员变化

```kotlin
@SubscribeEvent
fun onGroupMemberIncrease(event: GroupIncreaseNotice) {
    println("新成员加入群: ${event.groupId}")
    println("新成员QQ: ${event.userId}")
    println("操作者: ${event.operatorId}")
    
    // 发送欢迎消息
    OneBotAPI.sendGroupMessage(
        event.groupId,
        "欢迎新成员 [CQ:at,qq=${event.userId}]!"
    ) { success ->
        if (success) {
            println("欢迎消息发送成功")
        }
    }
}

@SubscribeEvent
fun onGroupMemberDecrease(event: GroupDecreaseNotice) {
    println("成员离开群: ${event.groupId}")
    println("离开成员QQ: ${event.userId}")
}
```

#### 监听好友添加

```kotlin
@SubscribeEvent
fun onFriendAdd(event: FriendAddNotice) {
    println("新增好友: ${event.userId}")
    
    // 发送问候消息
    OneBotAPI.sendPrivateMessage(
        event.userId,
        "很高兴认识你!"
    ) { success ->
        if (success) {
            println("问候消息发送成功")
        }
    }
}
```

### 请求事件监听

#### 监听好友申请

```kotlin
@SubscribeEvent
fun onFriendRequest(event: FriendRequestEvent) {
    println("收到好友申请")
    println("申请人: ${event.userId}")
    println("验证消息: ${event.comment}")
    
    // 注意: 目前好友请求处理功能还在开发中
    // event.approve() 和 event.reject() 方法暂不可用
}
```

### 状态事件监听

#### 监听连接状态

```kotlin
@SubscribeEvent
fun onConnected(event: OneBotConnectedEvent) {
    println("OneBot连接成功!")
    println("机器人QQ: ${event.selfId}")
}

@SubscribeEvent
fun onDisconnected(event: OneBotDisconnectedEvent) {
    println("OneBot连接断开")
    println("断开原因: ${event.reason}")
}

@SubscribeEvent
fun onReconnecting(event: OneBotReconnectingEvent) {
    println("正在尝试重连...")
}
```

## 完整示例

以下是一个完整的插件示例，展示如何集成OneBot功能：

### 主插件类

```kotlin
package com.example.mybot

import online.bingzi.onebot.api.OneBotAPI
import online.bingzi.onebot.api.event.message.GroupMessageEvent
import online.bingzi.onebot.api.event.message.PrivateMessageEvent
import online.bingzi.onebot.api.event.notice.GroupIncreaseNotice
import online.bingzi.onebot.api.event.status.OneBotConnectedEvent
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.platform.BukkitPlugin

object MyBotPlugin : Plugin() {
    
    override fun onEnable() {
        console().sendMessage("MyBot 插件已启用")
    }
}

class MyBotListener : Listener {
    
    @SubscribeEvent
    fun onConnected(event: OneBotConnectedEvent) {
        console().sendMessage("OneBot连接成功，机器人QQ: ${event.selfId}")
    }
    
    @SubscribeEvent
    fun onPrivateMessage(event: PrivateMessageEvent) {
        // 简单的私聊回复机器人
        when {
            event.message.contains("hello", true) -> {
                event.reply("Hello! 很高兴见到你!")
            }
            event.message.contains("time", true) -> {
                event.reply("当前时间: ${System.currentTimeMillis()}")
            }
            event.message.equals("help", true) -> {
                event.reply("可用命令:\n- hello: 问候\n- time: 获取时间\n- help: 帮助")
            }
        }
    }
    
    @SubscribeEvent
    fun onGroupMessage(event: GroupMessageEvent) {
        // 群消息处理
        when {
            event.message.startsWith("!info") -> {
                val info = "服务器信息:\n在线玩家: ${Bukkit.getOnlinePlayers().size}\nTPS: 20.0"
                event.reply(info)
            }
            event.message.equals("!help", true) -> {
                event.reply("群命令:\n- !info: 服务器信息\n- !help: 帮助")
            }
        }
    }
    
    @SubscribeEvent
    fun onNewMember(event: GroupIncreaseNotice) {
        // 欢迎新成员
        submit(delay = 20L) { // 延迟1秒发送
            OneBotAPI.sendGroupMessage(
                event.groupId,
                "欢迎新成员 [CQ:at,qq=${event.userId}] 加入我们的群聊！\n请阅读群公告，遵守群规则。"
            ) { success ->
                if (!success) {
                    console().sendMessage("欢迎消息发送失败")
                }
            }
        }
    }
}
```

### 命令处理示例

```kotlin
@CommandHeader(name = "mybot")
object MyBotCommand {
    
    @CommandBody
    val main = mainCommand {
        createHelper()
    }
    
    @CommandBody
    val send = subCommand {
        dynamic(comment = "type") {
            suggestion<CommandSender> { _, _ ->
                listOf("private", "group")
            }
            dynamic(comment = "target") {
                dynamic(comment = "message") {
                    execute<CommandSender> { sender, _, argument ->
                        val type = argument.get("type")
                        val target = argument.get("target").toLongOrNull()
                        val message = argument.get("message")
                        
                        if (target == null) {
                            sender.sendMessage("无效的目标ID")
                            return@execute
                        }
                        
                        when (type) {
                            "private" -> {
                                OneBotAPI.sendPrivateMessage(target, message) { success ->
                                    sender.sendMessage(if (success) "私聊消息发送成功" else "私聊消息发送失败")
                                }
                            }
                            "group" -> {
                                OneBotAPI.sendGroupMessage(target, message) { success ->
                                    sender.sendMessage(if (success) "群消息发送成功" else "群消息发送失败")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @CommandBody
    val status = subCommand {
        execute<CommandSender> { sender, _, _ ->
            val isConnected = OneBotAPI.isConnected()
            sender.sendMessage("OneBot连接状态: ${if (isConnected) "已连接" else "未连接"}")
        }
    }
}
```

## 最佳实践

### 1. 异步处理

始终使用提供的回调机制，不要尝试同步等待结果：

```kotlin
// ✅ 正确的异步处理
OneBotAPI.sendPrivateMessage(userId, message) { success ->
    if (success) {
        // 处理成功情况
    } else {
        // 处理失败情况
    }
}

// ❌ 错误：不要尝试同步等待
// val result = OneBotAPI.sendPrivateMessageSync(userId, message) // 此方法不存在
```

### 2. 错误处理

始终在回调中处理失败情况：

```kotlin
OneBotAPI.sendGroupMessage(groupId, message) { success ->
    if (success) {
        logger.info("消息发送成功")
    } else {
        logger.warning("消息发送失败，可能的原因：连接断开、权限不足、目标不存在")
        // 可以尝试重新发送或记录错误
    }
}
```

### 3. 连接状态检查

在发送消息前检查连接状态：

```kotlin
if (OneBotAPI.isConnected()) {
    OneBotAPI.sendPrivateMessage(userId, message) { success ->
        // 处理结果
    }
} else {
    logger.warning("OneBot未连接，无法发送消息")
}
```

### 4. 事件取消

某些情况下可能需要阻止事件传播：

```kotlin
@SubscribeEvent
fun onMessage(event: MessageEvent) {
    if (event.message.contains("敏感词")) {
        event.isCancelled = true // 阻止其他插件处理此事件
        event.reply("消息包含敏感内容")
    }
}
```

### 5. 资源管理

在插件禁用时清理资源：

```kotlin
override fun onDisable() {
    // OneBot插件会自动处理连接清理
    // 您只需要清理自己的资源
    myScheduledTasks.forEach { it.cancel() }
}
```

## 故障排除

### 常见问题

#### 1. API调用失败

**现象**: 所有API回调都返回false

**可能原因**:
- OneBot插件未启用
- OneBot未连接到QQ机器人
- 权限不足

**解决方案**:
```kotlin
// 检查插件状态
if (Bukkit.getPluginManager().getPlugin("OneBot") == null) {
    logger.warning("OneBot插件未安装")
    return
}

// 检查连接状态
if (!OneBotAPI.isConnected()) {
    logger.warning("OneBot未连接")
    return
}
```

#### 2. 事件不触发

**现象**: 事件监听器不执行

**可能原因**:
- 监听器未正确注册
- 事件类导入错误
- TabooLib版本不兼容

**解决方案**:
```kotlin
// 确保使用正确的事件类
import online.bingzi.onebot.api.event.message.GroupMessageEvent

// 确保监听器类被正确注册
@Awake(LifeCycle.ENABLE)
fun registerListener() {
    // TabooLib会自动注册标有@SubscribeEvent的方法
}
```

#### 3. 依赖问题

**现象**: 插件启动时找不到OneBot类

**解决方案**:
- 检查plugin.yml中的依赖配置
- 确保OneBot插件版本兼容
- 检查构建配置中的依赖范围

### 调试技巧

1. **启用详细日志**:
   在OneBot的config.yml中启用调试模式
   ```yaml
   debug:
     enabled: true
     log_raw_messages: true
     log_actions: true
   ```

2. **监听连接事件**:
   ```kotlin
   @SubscribeEvent
   fun onStatusChange(event: StatusEvent) {
       logger.info("OneBot状态变化: ${event.statusType}")
   }
   ```

3. **测试连接**:
   ```kotlin
   OneBotAPI.sendPrivateMessage(你的QQ号, "测试消息") { success ->
       logger.info("测试结果: $success")
   }
   ```

### 获取帮助

- **插件文档**: 查看OneBot插件的完整文档
- **GitHub Issues**: 报告bug或请求功能
- **社区讨论**: 参与开发者讨论

---

## 更新日志

- v1.0.0: 初始版本，提供基础API和事件系统
- v1.1.0: 添加群管理功能和状态事件
- v1.2.0: 优化异步处理机制

## 许可证

本文档遵循与OneBot插件相同的许可证。
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

BilibiliVideoQRCodeOneBot 是一个基于 TabooLib 的 Minecraft 插件，为 BilibiliVideo 插件提供 OneBot QQ 机器人二维码发送适配器。该插件将 Bilibili 登录二维码通过 QQ 机器人发送给用户。

### 技术栈
- **语言**: Kotlin
- **框架**: TabooLib (基于 Bukkit)
- **构建工具**: Gradle with Kotlin DSL
- **依赖管理**: Maven Central + 私有仓库
- **目标平台**: Minecraft 1.8+ (JVM 1.8)

## 构建命令

### 发行版本构建
```bash
./gradlew build
```
发行版本用于正常使用，不含 TabooLib 本体。构建产物位于 `build/libs/` 目录。

### 开发版本构建
```bash
./gradlew taboolibBuildApi -PDeleteCode
```
开发版本包含 TabooLib 本体，用于开发者使用，但不可运行。参数 `-PDeleteCode` 会移除所有逻辑代码以减少体积。

### 清理构建
```bash
./gradlew clean
```

### 重新加载依赖
```bash
./gradlew --refresh-dependencies build
```

## 关键依赖

### 核心依赖
- **TabooLib 6.2.3**: Minecraft 插件开发框架
- **ZXing 3.5.3**: 二维码生成库（`core` 和 `javase` 模块）
- **BilibiliVideo API**: 位于 `libs/BilibiliVideo-1.9.0-api.jar`
- **OneBot API**: `online.bingzi:onebot:1.2.0-65732a8`

### TabooLib 模块
项目使用以下 TabooLib 模块：
- `Basic`: 基础模块和插件生命周期
- `Database`: 数据库操作支持
- `Bukkit`: Bukkit 平台适配
- `BukkitUtil`: Bukkit 工具类
- `I18n`, `Metrics`, `MinecraftChat`, `CommandHelper`

## 项目架构

### 核心组件
- **主插件类**: `BilibiliVideoQRCodeOneBot` (object) - 实现 TabooLib Plugin 接口
- **生命周期**: 完整实现了 TabooLib 的 `onLoad()`, `onEnable()`, `onActive()`, `onDisable()` 方法

### 依赖关系
该插件依赖于两个核心组件：
1. **BilibiliVideo 插件**: 提供 Bilibili 相关功能和事件系统
2. **OneBot 插件**: 提供 QQ 机器人通信能力

### API 集成

#### BilibiliVideo API
基于 `libs/BilibiliVideo-1.9.0-api.jar`，主要功能包括：
- 事件监听：登录/登出事件、UP主关注检查、视频三连状态检查
- 二维码发送系统：可扩展的 QRCodeSender 框架
- 异步消息处理：支持同步和异步发送

#### OneBot API  
基于 `online.bingzi:onebot:1.2.0-65732a8`，主要功能包括：
- QQ 消息发送：私聊/群聊消息
- 事件监听：消息事件、通知事件、状态事件
- 群管理：禁言、踢出成员等操作
- 异步回调机制

## 开发指南

### 实现 QRCodeSender
基于 BilibiliVideo API 的二维码发送系统，需要：
1. 实现 `QRCodeSender` 接口
2. 在 `QRCodeSenderRegistry` 中注册
3. 处理依赖检查和生命周期管理
4. 使用 OneBot API 发送二维码到 QQ

### 事件监听模式
使用 TabooLib 的 `@SubscribeEvent` 注解：
- 监听 BilibiliVideo 事件获取二维码内容
- 监听 OneBot 连接状态确保发送可用性
- 异步处理避免阻塞主线程

### 配置管理
- 使用 TabooLib 的配置系统
- 支持 BilibiliVideo 和 OneBot 的配置集成
- 提供用户自定义发送选项

## 架构设计

### 分层架构
项目采用清晰的分层架构设计：

#### API 层 (`api/`)
- **对外接口层**：提供稳定的公开 API，供其他插件或模块调用
- **核心服务接口**：
  - `BindingService`: 玩家-QQ 绑定关系管理
  - `QRCodeService`: 二维码生成服务
  - `VerificationService`: 验证码生成与验证

#### 内部实现层 (`internal/`)
- **具体实现**：实现 API 层定义的接口
- **配置模块** (`config/`): 各功能模块的配置类
- **辅助工具** (`helper/`): 各模块专用的辅助工具类
- **数据库层** (`database/`): 数据持久化相关功能

### 模块化设计
每个功能都有独立的模块结构：
```
功能模块/
├── 配置类 (config/)
├── 实现类 (XxxGenerator/XxxManager)  
└── 辅助工具 (helper/)
```

### 服务访问模式
主插件类 `BilibiliVideoQRCodeOneBot` 提供统一的服务访问入口：
- `bindingService`: 访问绑定服务
- `qrcodeService`: 访问二维码服务

## 开发最佳实践

### TabooLib 规范
- 使用 `@Awake` 注解管理生命周期
- 遵循 TabooLib 的异步任务调度
- 使用 `info()`, `warning()`, `severe()` 进行日志记录

### 异步编程模式
所有数据库操作和外部 API 调用都采用异步回调模式：
```kotlin
// 示例：绑定服务异步调用
bindingService.bindPlayer(uuid, qqNumber, playerName) { success ->
    // 回调处理结果
}
```

### 错误处理策略
- **自定义异常**：为每个功能模块定义专用异常类
- **优雅降级**：外部依赖不可用时提供默认行为
- **日志记录**：使用 TabooLib 的日志系统记录详细错误信息

### TabooLib 生命周期集成
插件完整实现 TabooLib 生命周期：
- `onLoad()`: 初始化基础配置
- `onEnable()`: 启动各个服务模块  
- `onActive()`: 激活插件功能
- `onDisable()`: 清理资源和注销服务

## 文档资源

项目包含完整的开发文档：

### 核心 API 文档
- `docs/BilibiliVideo/API.md`: BilibiliVideo 插件 API 详细文档
- `docs/OneBot/developer-guide.md`: OneBot 插件开发者指南

### TabooLib 模块文档
项目使用了多个 TabooLib 模块，每个模块都有详细文档：

#### 核心模块
- `docs/taboolib/Basic.md`: 基础模块，插件生命周期管理
- `docs/taboolib/Configuration.md`: 配置管理，支持 @Config 注解和热重载
- `docs/taboolib/TabooLibCommand.md`: 命令系统，支持 DSL 和注解两种方式

#### 平台模块
- `docs/taboolib/Bukkit.md`: Bukkit 平台适配器
- `docs/taboolib/BukkitUtil.md`: Bukkit 工具类集合
- `docs/taboolib/BukkitHook.md`: Bukkit 插件挂钩系统

#### 功能模块
- `docs/taboolib/CommandHelper.md`: 命令辅助工具
- `docs/taboolib/Database.md`: 数据库操作模块
- `docs/taboolib/I18n.md`: 国际化支持
- `docs/taboolib/MinecraftChat.md`: 聊天组件处理
- `docs/taboolib/Metrics.md`: 数据统计模块
- `docs/taboolib/Kether.md`: 脚本引擎支持
- `docs/taboolib/TabooLibChain.md`: 链式任务处理

这些文档提供了详细的 API 使用方法、配置选项、事件系统说明和开发示例。开发时应优先参考相关模块文档。
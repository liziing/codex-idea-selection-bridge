# Codex IDEA Selection Bridge

[English](README.md) | 简体中文

[![CI](https://github.com/liziing/codex-idea-selection-bridge/actions/workflows/ci.yml/badge.svg)](https://github.com/liziing/codex-idea-selection-bridge/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

一个注重隐私的桥接工具，让 Codex 能通过文件路径和行号范围定位 IntelliJ IDEA 当前选中的代码，无需把源码复制到剪贴板或临时文件。

## 为什么需要它

Codex 桌面端能够读取本地代码仓库，但不知道你在 IDEA 中选中了哪一段代码。本项目通过一条刻意保持精简的链路连接二者：

```text
IDEA 代码选区
  → localhost 元数据接口
  → Codex Skill 查询脚本
  → Codex 读取真实项目文件
```

桥接接口只返回代码位置元数据，不会返回选中的源码文本。

## 功能特性

- 追踪 IntelliJ IDEA 当前的单个代码选区。
- 返回项目根目录、文件绝对路径、行号范围和捕获时间。
- 对未保存文件直接返回错误，不传输编辑器内存中的代码。
- 仅监听 `127.0.0.1`。
- 内置支持中英文隐式触发词的 Codex Skill。
- 始终以本地真实文件为代码事实来源。

## 环境要求

- IntelliJ IDEA 2025.1（`251.*`）
- 支持本地 Skill 的 Codex 桌面端
- Node.js 18 或更高版本
- 构建 IDEA 插件时需要 JDK 21

## 安装

### 1. 安装 IDEA 插件

从 [最新 GitHub Release](https://github.com/liziing/codex-idea-selection-bridge/releases/latest) 下载 IDEA 插件 ZIP，或在本地构建：

```bash
cd idea-plugin
./gradlew -PideaPath="/Applications/IntelliJ IDEA.app" test buildPlugin verifyPlugin
```

构建产物位于 `idea-plugin/build/distributions/`。

在 IDEA 中打开 **Settings → Plugins → 齿轮图标 → Install Plugin from Disk**，选择 ZIP，然后重启 IDEA。

### 2. 安装 Codex Skill

将 Skill 目录复制到 Codex 的本地 Skill 目录：

```bash
cp -R codex-skill/idea-selection ~/.codex/skills/idea-selection
```

如果 Skill 没有立即出现，请新建一个 Codex 任务。

## 使用方式

1. 在 IDEA 和 Codex 中打开同一个代码仓库。
2. 在 IDEA 中保存当前文件。
3. 框选需要讨论的代码。
4. 向 Codex 提问，例如：
   - `解释当前选区。`
   - `分析一下这段代码。`
   - `这个代码有什么问题？`
   - `Explain the code currently selected in IDEA.`

Skill 会解析文件路径和行号范围，随后由 Codex 读取真实文件以及完成请求所必需的最小周边上下文。

## 检查桥接服务

确保 IDEA 已启动并打开项目，然后执行：

```bash
curl http://127.0.0.1:47321/health
```

预期返回：

```json
{"ok":true,"service":"codex-idea-selection-bridge"}
```

选区错误会明确返回：

| 错误码 | 含义 |
|---|---|
| `NO_SELECTION` | 请先在 IDEA 中框选代码。 |
| `FILE_NOT_SAVED` | 请保存文件、重新框选代码后再试。 |
| `IDEA_BRIDGE_UNAVAILABLE` | IDEA 未启动、插件未启用，或端口不可用。 |

## 隐私与安全

- 不读取剪贴板。
- 不创建源码选区快照文件。
- localhost 响应中不包含源码。
- 不监听远程网络地址。
- 不支持读取未保存的编辑器内容。

该接口会向本机进程暴露项目路径和文件路径。安全问题报告方式请参阅 [SECURITY.md](SECURITY.md)。

## 当前限制

- 只追踪一个当前选区。
- 文件必须已经保存。
- IDEA 与 Codex 使用同一个项目根目录时效果最佳。
- 桥接服务固定使用 `47321` 端口；同时运行多个 IDEA 进程时，只有一个进程能够占用该端口。

## 开发与验证

使用项目配置的 IntelliJ Platform 版本构建：

```bash
cd idea-plugin
./gradlew test buildPlugin verifyPlugin
```

使用本机 IDEA 安装目录构建：

```bash
./gradlew -PideaPath="/Applications/IntelliJ IDEA.app" test buildPlugin verifyPlugin
```

仓库包含单元测试、IntelliJ Plugin Verifier 校验和 GitHub Actions CI。欢迎参与贡献，详情参阅 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 开源协议

本项目基于 [MIT License](LICENSE) 开源。

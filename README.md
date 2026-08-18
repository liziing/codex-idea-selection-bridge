# Codex IDEA Selection Bridge

[![CI](https://github.com/liziing/codex-idea-selection-bridge/actions/workflows/ci.yml/badge.svg)](https://github.com/liziing/codex-idea-selection-bridge/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A privacy-focused bridge that lets Codex resolve the code currently selected in IntelliJ IDEA by file path and line range—without copying source code to the clipboard or a temporary file.

## Why

Codex Desktop can read a local repository, but it does not know which code is selected in IDEA. This project connects the two while keeping the integration deliberately small:

```text
IDEA selection
  → localhost metadata endpoint
  → Codex Skill script
  → Codex reads the real project file
```

The bridge endpoint returns location metadata only. It never returns the selected source text.

## Features

- Tracks the current single selection in IntelliJ IDEA.
- Returns project root, absolute file path, line range, and capture time.
- Rejects unsaved files instead of transmitting editor-buffer contents.
- Listens on `127.0.0.1` only.
- Includes a Codex Skill with Chinese and English implicit trigger phrases.
- Uses the real local file as the source of truth.

## Requirements

- IntelliJ IDEA 2025.1 (`251.*`)
- Codex Desktop with local Skill support
- Node.js 18 or newer
- JDK 21 for building the IDEA plugin

## Installation

### 1. Install the IDEA plugin

Download the IDEA plugin ZIP from the latest GitHub release, or build it locally:

```bash
cd idea-plugin
./gradlew -PideaPath="/Applications/IntelliJ IDEA.app" test buildPlugin verifyPlugin
```

The ZIP is created under `idea-plugin/build/distributions/`.

In IDEA, open **Settings → Plugins → gear icon → Install Plugin from Disk**, choose the ZIP, and restart IDEA.

### 2. Install the Codex Skill

Copy the Skill directory into your local Codex skills directory:

```bash
cp -R codex-skill/idea-selection ~/.codex/skills/idea-selection
```

Start a new Codex task if the Skill does not appear immediately.

## Usage

1. Open the same repository in IDEA and Codex.
2. Save the file in IDEA.
3. Select the code you want to discuss.
4. Ask Codex, for example:
   - `Explain the code currently selected in IDEA.`
   - `解释当前选区。`
   - `分析一下这段代码。`
   - `这个代码有什么问题？`

The Skill resolves the file and line range, then Codex reads the real file and the minimum surrounding context needed for the request.

## Verify the bridge

With IDEA running and a project open:

```bash
curl http://127.0.0.1:47321/health
```

Expected response:

```json
{"ok":true,"service":"codex-idea-selection-bridge"}
```

Selection errors are explicit:

| Error | Meaning |
|---|---|
| `NO_SELECTION` | Select code in IDEA and try again. |
| `FILE_NOT_SAVED` | Save the file, reselect the code, and try again. |
| `IDEA_BRIDGE_UNAVAILABLE` | IDEA is closed, the plugin is disabled, or the port is unavailable. |

## Privacy and security

- No clipboard access.
- No source-code snapshot files.
- No source code in the localhost response.
- No remote network listener.
- No support for unsaved editor content.

The endpoint does expose local project and file paths to processes running on the same machine. See [SECURITY.md](SECURITY.md) for reporting security issues.

## Limitations

- Only one active selection is tracked.
- The file must be saved.
- The Skill works best when IDEA and Codex are using the same project root.
- The bridge uses fixed port `47321`; when multiple IDEA processes are running, only one can own the port.

## Development

Build against the configured IntelliJ Platform version:

```bash
cd idea-plugin
./gradlew test buildPlugin verifyPlugin
```

Build against a local IDEA installation:

```bash
./gradlew -PideaPath="/Applications/IntelliJ IDEA.app" test buildPlugin verifyPlugin
```

The repository includes unit tests, IntelliJ Plugin Verifier checks, and GitHub Actions CI. Contributions are welcome; see [CONTRIBUTING.md](CONTRIBUTING.md).

## 中文快速开始

1. 安装 IDEA 插件 ZIP 并重启 IDEA。
2. 将 `codex-skill/idea-selection` 复制到 `~/.codex/skills/idea-selection`。
3. 在 IDEA 中保存文件并框选代码。
4. 在打开同一项目的 Codex 任务中输入“解释这个代码”或“分析这段代码”。

插件只向 Codex 提供文件路径和行号，不传输选中的代码文本。

## License

Released under the [MIT License](LICENSE).

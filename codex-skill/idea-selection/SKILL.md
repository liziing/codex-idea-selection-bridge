---
name: idea-selection
description: Use the current saved-file selection from IntelliJ IDEA when the user refers to code without pasting or identifying it, including “这个代码”“这段代码”“这行代码”“这一行代码”“这几行代码”“这块代码”“这里的代码”“当前代码”“刚才选中的代码”, or asks “根据这个代码”“根据这行代码”“解释一下这个代码”“解释一下这几行代码”“分析/检查/优化这段代码”“增加/修改/删除这行代码或这几行代码”“这个代码是什么意思/有什么问题”. Also use for explicit IDEA/IntelliJ selection requests such as 当前选区、IDEA 选中的代码、解释选中代码、检查当前选择. Do not trigger when the prompt already provides a code block, file path or line range, attachment, or clearly refers to code already present in the conversation.
---

# IDEA Selection

Use the IDEA bridge only to locate the current selection. The bridge returns a project root, file path, and line range; it never returns source code. Prefer explicit code context already supplied in the prompt or conversation; query IDEA only when the reference would otherwise be unresolved or the user explicitly refers to the IDEA selection.

1. Run `node scripts/get-selection.mjs` from this skill directory.
2. Parse the single JSON object printed by the script.
3. Handle errors exactly:
   - `IDEA_BRIDGE_UNAVAILABLE`: tell the user to open IDEA with the bridge plugin enabled.
   - `NO_SELECTION`: tell the user to select code in IDEA.
   - `FILE_NOT_SAVED`: tell the user to save the file and reselect the code.
   - Any other error: report the returned message without guessing a selection.
4. For a successful response, verify that `filePath` exists and that `projectRoot` matches the current Codex project. If it does not match, identify the mismatch before reading anything.
5. Read the selected line range from the real file, plus only the surrounding code required for the request.
6. Lead with `filePath:startLine-endLine`, then answer the user's request. Distinguish direct code evidence from inferred behavior.

Do not use clipboard contents, temporary selection files, screenshots, or stale selections as substitutes.

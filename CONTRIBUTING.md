# Contributing

Thanks for helping improve Codex IDEA Selection Bridge.

## Before opening a change

- Search existing issues before creating a new one.
- Keep changes focused on the IDEA selection bridge or its Codex Skill.
- Do not add source-code capture, clipboard access, or remote network transport without first discussing the privacy impact in an issue.

## Development setup

Requirements:

- JDK 21
- IntelliJ IDEA 2025.1 when building against a local IDE
- Node.js 18 or newer for the Skill script

Run the IDEA plugin checks:

```bash
cd idea-plugin
./gradlew test buildPlugin verifyPlugin
```

To build against a locally installed IDE instead of downloading the configured platform version:

```bash
./gradlew -PideaPath="/Applications/IntelliJ IDEA.app" test buildPlugin verifyPlugin
```

Validate the Codex Skill with the `quick_validate.py` script bundled with Codex's `skill-creator` Skill.

## Pull requests

- Explain the user-visible behavior and privacy impact.
- Add or update tests when behavior changes.
- Confirm that the Gradle checks and Skill validation pass.
- Avoid committing generated ZIP files, Gradle caches, IDE metadata, or build output.

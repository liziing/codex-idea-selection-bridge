# Security Policy

## Supported versions

Security fixes are provided for the latest released version.

## Reporting a vulnerability

Please do not open a public issue for a suspected vulnerability. Use GitHub's private vulnerability reporting for this repository:

https://github.com/liziing/codex-idea-selection-bridge/security/advisories/new

Include reproduction steps, affected versions, and the expected impact. The maintainer will acknowledge the report and coordinate disclosure after a fix is available.

## Security model

- The IDEA plugin listens only on `127.0.0.1`.
- The endpoint exposes file location metadata, not source code.
- Unsaved files are rejected.
- The Codex Skill reads source code from the user's existing local workspace permissions.

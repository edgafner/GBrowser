---
description: Fast compilation check without full build
allowed-tools: Bash
---

Run a fast compilation check for the current changes:

1. Run `./gradlew compileKotlin --no-scan` for main sources
2. If test files were recently modified (check `git diff --name-only HEAD`), also run `./gradlew compileTestKotlin --no-scan`
3. Report any errors with file paths and line numbers
4. Do NOT run full `buildPlugin` or `test` unless explicitly asked

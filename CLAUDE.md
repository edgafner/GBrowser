# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Important: Don't run runIde nor ui-test

## Project Overview

GBrowser is an IntelliJ IDEA plugin that embeds a web browser tool window directly in the IDE. It uses JCEF (Java Chromium Embedded Framework) to provide a full-featured browser
experience without leaving the development environment.

## Build and Development Commands

```bash
# Build the plugin
./gradlew buildPlugin --no-scan

# Run tests (excluding UI tests)
./gradlew test --no-scan

# Run all tests including UI tests
./gradlew check --no-scan

# Run a specific test class
./gradlew test --no-scan --tests "com.github.gbrowser.MyTestClass"

# Run a specific test method
./gradlew test --no-scan --tests "com.github.gbrowser.MyTestClass.myTestMethod"

# Clean and rebuild
./gradlew clean buildPlugin --no-scan

# Verify plugin compatibility with different IDE versions
./gradlew runPluginVerifier --no-scan

# Generate code coverage report
./gradlew koverHtmlReport --no-scan

# Check for dependency updates
./gradlew dependencyUpdates --no-scan
```

## Architecture Overview

### Key Components

1. **GBrowserService & GBrowserProjectService** (`services/`): Core state management
  - Global and project-specific settings persistence using `@State` and `@Service` annotations
  - Manages tabs, bookmarks, history, and request headers
  - Uses IntelliJ's persistent state components

2. **GCefBrowser** (`ui/gcef/GCefBrowser.kt`): Enhanced JCEF browser wrapper
  - Extends `JBCefBrowser` with custom handlers
  - Context menus, lifecycle management, resource requests
  - DevTools integration (accessible via context menu or actions)

3. **GBrowserToolWindow** (`ui/toolwindow/gbrowser/`): Tool window integration
  - Creates and manages the tool window UI
  - Combines browser UI with toolbar controls
  - Handles tab management and navigation

4. **Actions** (`actions/` directory): Browser control actions
  - Navigation actions (back, forward, refresh, home)
  - Tab management (new, close, duplicate)
  - Browser features (zoom, find, dev tools)
  - All actions extend `AnAction` and implement `DumbAware` for indexing responsiveness

5. **Settings** (`settings/` directory): Configuration management
  - UI components using IntelliJ UI DSL and Swing
  - Manages bookmarks, history, request headers
  - Project-specific and global settings

### Plugin Integration Points

- **plugin.xml**: Defines all actions, tool windows, and extension points
- **Tool Window**: Registered as "GBrowser" with icon and factory class
- **Actions**: Mapped to keyboard shortcuts and toolbar buttons
- **Error Reporting**: Custom error report submitter that creates GitHub issues with stack traces
- **Message Bus**: Uses IntelliJ's event system for communication between components

### Testing Strategy

- **Unit Tests**: Standard JUnit 5 tests with Mockk for mocking
- **UI Tests**: Separate source set using IntelliJ UI driver SDK
  - Tests actual IDE and browser integration
  - Uses fixtures and automated UI interaction
  - Requires JCEF-enabled JBR (JetBrains Runtime)
  - Automated cleanup of test projects after all tests complete (@AfterAll)
  - Cleans up projects from both build/out/ide-tests and ~/IdeaProjects
- **Static Analysis**: Qodana integration for code quality checks
- **Code Coverage**: Automated reporting via Codecov

### Important Development Notes

1. **JCEF Requirements**: Development and testing require JBR with JCEF support
2. **Platform Version**: Currently targeting IntelliJ 261 EAP-SNAPSHOT
3. **Kotlin Version**: Uses Kotlin 2.3.20 with JVM 25
4. **Threading**: Actions specify execution threads (EDT for UI, BGT for background)
5. **Memory**: IDE runs with 4GB heap, tests with 2GB heap
6. **Caching**: Uses Caffeine cache for favicons and webpage titles with expiration
7. **External Libraries**:
  - Jackson for JSON/YAML processing
  - Jsoup for HTML parsing (webpage titles)
  - OkHttp for HTTP requests (URL suggestions)
  - Caffeine for caching

### Common Development Patterns

- Use `service<GBrowserService>()` or `project.service<GBrowserProjectService>()` for state access
- Actions should check `CefApp.isSupported()` before browser operations
- UI updates must happen on EDT (Event Dispatch Thread)
- Background operations should run on BGT (Background Thread)
- Browser operations are asynchronous - use `CompletableFuture` for async operations
- Settings are persisted using `@State` and `@Service` annotations
- Use `GBrowserToolWindowUtil` for common tool window operations
- Event communication via IntelliJ's `MessageBus` and `EventDispatcher`
- Safe URL handling with validation utilities
- DevTools accessible via right-click context menu or dedicated action

## Gotchas

- Use `contentManagerIfCreated` (not `contentManager`) for read-only tool window access outside EDT — `contentManager` lazily initializes and requires EDT
- Never catch `CancellationException` or `ProcessCanceledException` without rethrowing
- `createContentTab` intentionally uses `contentManager` (not `contentManagerIfCreated`) because it runs inside `invokeLater` on EDT and needs the content manager initialized
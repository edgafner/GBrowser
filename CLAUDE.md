# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Important: Don't run runIde nor ui-test

## Project Overview

GBrowser is an IntelliJ IDEA plugin that embeds a web browser tool window directly in the IDE. It uses JCEF (Java Chromium Embedded Framework) to provide a full-featured browser
experience without leaving the development environment.

## Build and Development Commands

```bash
# Build the plugin
./gradlew buildPlugin

# Run tests (excluding UI tests)
./gradlew test


# Run all tests including UI tests
./gradlew check


# Run a specific test class
./gradlew test --tests "com.gafner.giv.MyTestClass"

# Run a specific test method
./gradlew test --tests "com.gafner.giv.MyTestClass.myTestMethod"

# Clean and rebuild
./gradlew clean buildPlugin

# Verify plugin compatibility with different IDE versions
./gradlew runPluginVerifier

# Generate code coverage report
./gradlew koverHtmlReport

# Check for dependency updates
./gradlew dependencyUpdates
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
2. **Platform Version**: Currently targeting IntelliJ 252 EAP (2025.2)
3. **Kotlin Version**: Uses Kotlin 2.2.0 with JVM 21
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

## Personal Memories

- Memorize the current request I need to restart the IDE
# GBrowser Freezing Issues Analysis

## Overview

Multiple users have reported IDE freezing issues when using GBrowser, particularly:

- Issue #417: Freezing when opening GitLab review requests
- Issue #398: IDE cannot exit after using GBrowser
- Issue #284: Freezing with Markdown files during real-time preview
- Issue #287: Freezing with NextJS hot reload
- Issue #277: Cursor stuck in text fields

## Analysis of Dump Files

### Key Findings:

1. **No Deadlocks**: The thread dumps show no blocked threads or deadlocks
2. **EDT is Healthy**: The AWT-EventQueue-0 thread is in WAITING state (normal)
3. **No CEF/JCEF Threads**: Surprisingly, no CEF-specific threads are visible in the dumps
4. **GBrowser Minimal**: Only mentioned in tool window resize collector

### Possible Root Causes:

#### 1. CEF Process Management

The absence of CEF threads in the dump suggests potential issues with:

- CEF subprocess lifecycle management
- Browser process not properly terminating
- CEF initialization/shutdown sequence

#### 2. Focus Management Issues

Multiple issues mention focus problems:

- Cursor getting stuck in text fields (#277)
- Auto-focusing during hot reload (#287)
- This suggests focus event handling conflicts between JCEF and IntelliJ

#### 3. Event Loop Integration

The freezing during specific operations (GitLab, Markdown preview) indicates:

- Possible synchronous operations on EDT
- Event dispatching conflicts between CEF and Swing
- JavaScript execution blocking the UI thread

## Recommended Fixes

### 1. Implement Proper CEF Lifecycle Management

```kotlin
class GCefBrowserLifecycleManager {
    fun ensureProperShutdown() {
        // Ensure all CEF browsers are properly disposed
        // Force CEF shutdown on IDE exit
    }
}
```

### 2. Fix Focus Handling

```kotlin
class GCefBrowserFocusManager {
    fun preventFocusStuck() {
        // Implement focus release mechanism
        // Add timeout for focus operations
    }
}
```

### 3. Async Operations for Heavy Tasks

```kotlin
class GBrowserAsyncOperations {
    suspend fun loadPageAsync(url: String) {
        withContext(Dispatchers.IO) {
            // Load page off EDT
        }
    }
}
```

### 4. Add Defensive Timeouts

```kotlin
class GBrowserTimeoutManager {
    fun withTimeout(operation: () -> Unit) {
        runBlocking {
            withTimeoutOrNull(5000) {
                operation()
            } ?: handleTimeout()
        }
    }
}
```

## Next Steps

1. **Add Comprehensive Logging**:
  - CEF lifecycle events
  - Focus change events
  - Page load timings
  - JavaScript execution times

2. **Implement Safety Mechanisms**:
  - Timeout for all CEF operations
  - Force release focus on timeout
  - Emergency shutdown for frozen browsers

3. **Test Specific Scenarios**:
  - GitLab review pages with heavy JavaScript
  - Markdown preview with rapid updates
  - Pages with multiple input fields

4. **Consider CEF Settings**:
   ```kotlin
   val settings = CefSettings().apply {
       // Disable GPU acceleration if causing issues
       command_line_args_disabled = false
       // Set proper subprocess path
       browser_subprocess_path = getSubprocessPath()
   }
   ```

## Workaround for Users

Until fixes are implemented, users can:

1. Disable GBrowser before shutting down IDE
2. Close all GBrowser tabs before opening heavy pages
3. Use external browser for problematic sites
4. Report specific URLs that cause freezing
# IntelliJ Platform Coroutines Guidelines

## Summary of Key Principles

This document summarizes the IntelliJ Platform coroutine guidelines based on the official documentation. These principles should be followed to ensure proper plugin behavior and
prevent IDE hanging or resource leaks.

## 1. Structured Concurrency and Scopes

### ✅ DO: Use Service Scopes

- **Always use service scopes for launching coroutines**
- Services receive their scope via constructor injection
- Each service instance gets its own isolated scope

```kotlin
@Service
class MyApplicationService(
    private val cs: CoroutineScope
) {
    fun scheduleSomething() {
        cs.launch {
            // coroutine work
        }
    }
}

@Service(Service.Level.PROJECT)
class MyProjectService(
    private val project: Project,
    private val cs: CoroutineScope
) {
    fun scheduleSomething() {
        cs.launch {
            // coroutine work
        }
    }
}
```

### ❌ DON'T: Use Application/Project Scopes Directly

- **Never use** `Application.getCoroutineScope()` or `Project.getCoroutineScope()`
- These are deprecated and will be removed
- Can easily lead to project or plugin class leaks

```kotlin
// BAD - causes leaks
application.coroutineScope.launch {
    project.getService(MyService::class.java) // Project leak!
}
```

### ❌ DON'T: Use Static Initializers or Shutdown Hooks

- Avoid static state for lifecycle management
- Don't use `Runtime.getRuntime().addShutdownHook()`
- Use proper IntelliJ lifecycle components instead

## 2. Threading and Dispatchers

### Dispatchers to Use

- **Dispatchers.Default** - For CPU-intensive work
- **Dispatchers.IO** - For I/O operations
- **Dispatchers.EDT** - For UI operations (Event Dispatch Thread)

### EDT Operations

```kotlin
// UI operations must run on EDT
scope.launch {
    withContext(Dispatchers.EDT) {
        // UI updates here
    }
}
```

### Current EDT Behavior (Will Change!)

- Currently, coroutines on EDT have implicit write intent lock
- This will be removed in future releases
- Always use explicit locking when needed

## 3. Locking and Read/Write Actions

### Suspending APIs (Preferred)

```kotlin
// Best - can reschedule to background thread
readAction {
    // read PSI or project model
}

// For read-then-write operations
readAndWriteAction {
    // prepare under read lock
    // then modify under write lock
}
```

### Blocking APIs (Use Only When Necessary)

```kotlin
// When you must stay on current thread
ReadAction.run {
    // read operations
}

// Last resort - avoid in plugin code
WriteIntentReadAction.run {
    // operations needing write intent
}
```

## 4. Common Anti-Patterns to Avoid

### ❌ Static Lifecycle Management

```kotlin
// BAD
companion object {
    init {
        Runtime.getRuntime().addShutdownHook { /* cleanup */ }
    }
}
```

### ❌ Manual Scope Creation

```kotlin
// BAD - creates unmanaged scope
val myScope = CoroutineScope(Dispatchers.Default)
```

### ❌ Blocking in Coroutines

```kotlin
// BAD - blocks the thread
runBlocking {
    // some work
}

// GOOD - use in plugin code
runBlockingCancellable {
    // integrates with IDE's progress system
}
```

## 5. Resource Cleanup

### Proper Disposal Pattern

```kotlin
class MyComponent(private val scope: CoroutineScope) : Disposable {
    override fun dispose() {
        scope.cancel("Component disposed")
    }
}
```

### Parent-Child Relationships

- Use `Disposer.register(parent, child)` for proper cleanup chains
- Coroutine scopes are automatically canceled when their parent is disposed

## 6. Service Scope Lifetimes

### Scope Hierarchy

1. **Root** - All coroutines
2. **Application** - Application lifetime
3. **Plugin** - Plugin lifetime
4. **Project** - Project lifetime
5. **Application×Plugin** - Intersection (canceled when either ends)
6. **Project×Plugin** - Intersection (canceled when either ends)
7. **Service Scopes** - Bound to service lifetime

## 7. Best Practices Summary

1. **Always use service scopes** - Let IntelliJ manage coroutine lifecycles
2. **Respect EDT requirements** - UI operations on EDT, heavy work on background
3. **Use proper read/write actions** - Explicit locking prevents race conditions
4. **Follow disposal patterns** - Proper cleanup prevents leaks
5. **Avoid static state** - Use IntelliJ's component system
6. **Handle cancellation** - Coroutines should be cancellable
7. **Use supervisor scopes** - Failures in children don't affect parents

## Common Issues to Check

1. Services without proper scope injection
2. Direct use of application/project scopes
3. Static initializers with lifecycle code
4. Missing EDT dispatch for UI operations
5. Blocking operations in coroutines
6. Manual scope creation without proper disposal
7. Missing read/write actions for PSI/model access
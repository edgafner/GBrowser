# CompletableFuture to Coroutines Migration Guide

## Overview

This guide documents the migration pattern used to convert `CompletableFuture`-based async operations to Kotlin coroutines in the GBrowser plugin, following IntelliJ Platform best
practices.

## Migration Pattern

### Before (CompletableFuture)

```kotlin
@Service(Service.Level.APP)
class MyService : Disposable {
    fun doAsyncWork(): CompletableFuture<String> {
        return CompletableFuture.supplyAsync {
            // Blocking I/O operation
            performNetworkCall()
        }
    }
}
```

### After (Coroutines)

```kotlin
@Service(Service.Level.APP)
class MyService(
    private val scope: CoroutineScope
) : Disposable {
    
    // Backward compatibility method
    fun doAsyncWork(): CompletableFuture<String> {
        val deferred = doAsyncWorkDeferred()
        val future = CompletableFuture<String>()
        
        scope.launch {
            try {
                future.complete(deferred.await())
            } catch (e: CancellationException) {
                future.cancel(true)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        
        return future
    }
    
    // New coroutine version for future use
    suspend fun doAsyncWorkSuspend(): String {
        return doAsyncWorkDeferred().await()
    }
    
    private fun doAsyncWorkDeferred(): Deferred<String> {
        return scope.async(Dispatchers.IO) {
            // Suspending I/O operation
            performNetworkCall()
        }
    }
    
    override fun dispose() {
        scope.cancel("Service disposed")
    }
}
```

## Key Changes

1. **Service Constructor Injection**
  - Add `CoroutineScope` parameter to service constructor
  - IntelliJ automatically injects the proper scope

2. **Cache Type Change**
  - From: `Cache<String, CompletableFuture<T>>`
  - To: `Cache<String, Deferred<T>>`

3. **Async Operations**
  - From: `CompletableFuture.supplyAsync { ... }`
  - To: `scope.async(Dispatchers.IO) { ... }`

4. **Proper Dispatchers**
  - Use `Dispatchers.IO` for I/O operations (network, file)
  - Use `Dispatchers.Default` for CPU-intensive work
  - Use `Dispatchers.EDT` for UI operations

5. **Cancellation Support**
  - Properly handle `CancellationException`
  - Cancel scope in `dispose()` method

## Migration Steps

1. **Update Service Declaration**
   ```kotlin
   @Service(Service.Level.APP)
   class MyService(private val scope: CoroutineScope) : Disposable
   ```

2. **Change Cache Types**
   ```kotlin
   // Before
   private val cache = Caffeine.newBuilder()
       .build<String, CompletableFuture<T>>()
   
   // After
   private val cache = Caffeine.newBuilder()
       .build<String, Deferred<T>>()
   ```

3. **Update Async Methods**
   ```kotlin
   // Before
   private fun loadAsync(): CompletableFuture<T> {
       return CompletableFuture.supplyAsync { ... }
   }
   
   // After
   private fun loadAsync(): Deferred<T> {
       return scope.async(Dispatchers.IO) { ... }
   }
   ```

4. **Add Backward Compatibility**
  - Keep existing public API returning `CompletableFuture`
  - Add new suspend functions for future migration
  - Bridge between Deferred and CompletableFuture

5. **Implement Proper Disposal**
   ```kotlin
   override fun dispose() {
       cache.cleanUp()
       scope.cancel("Service disposed")
   }
   ```

## Benefits

1. **Better Resource Management**
  - Automatic cancellation on service disposal
  - No thread pool leaks

2. **Structured Concurrency**
  - Operations tied to service lifecycle
  - Proper parent-child relationships

3. **Improved Performance**
  - Uses appropriate dispatchers for different operations
  - Better integration with IntelliJ's threading model

4. **Future-Proof**
  - Aligns with IntelliJ Platform direction
  - Easier to maintain and debug

## Remaining Usages to Migrate

The following components still use CompletableFuture.thenAccept() and should be migrated to use the new suspend functions when appropriate:

1. `GCefBrowser.kt` - Bookmark addition
2. `GBrowserToolWindowUtil.kt` - Tab creation with title
3. `JBToolWindowBrowser.kt` - Favicon loading for search items
4. `GBrowserCefLifeSpanDelegate.kt` - New tab creation
5. `GBrowserToolWindowActionBar.kt` - Bookmark icon loading
6. `GBrowserToolWindowBrowser.kt` - Tab icon and search icon loading
7. `GBrowserBookmarkGroupAction.kt` - Bookmark menu icons

These can be migrated gradually as the codebase moves toward full coroutine adoption.
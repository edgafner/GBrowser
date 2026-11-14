package com.github.gbrowser.ui

import com.intellij.ide.starter.path.GlobalPaths
import com.intellij.ide.starter.utils.Git
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories

private val IDE_CACHE_DIR: Path? = System.getenv("IDE_CACHE_DIR")
  ?.takeIf { it.isNotBlank() }
  ?.let { Paths.get(it) }

private val BUILD_DIR: Path = Git.getRepoRoot().resolve("build")

/**
 * IDE Starter paths for the GBrowser UI tests (same pattern as the AZD plugin's `AZDGlobalPaths`).
 *
 * On the self-hosted macOS runner the workflow sets `IDE_CACHE_DIR` to a directory outside the
 * workspace, so the IDE installer (~1 GB) and its extracted build survive between runs instead of
 * being re-downloaded after `actions/checkout` cleans `build/`. Test output (logs, reports) moves
 * there too; the workflow's artifact step collects both roots.
 *
 * Locally (`IDE_CACHE_DIR` unset) everything stays under the repo's `build/out/ide-tests`.
 */
class GBrowserGlobalPaths : GlobalPaths(BUILD_DIR) {

  // A computed getter, not a stored val: GlobalPaths.<init> reads testHomePath, and an overriding
  // stored val is still unassigned during the superclass constructor (NPE).
  override val testHomePath: Path
    get() = IDE_CACHE_DIR?.createDirectories()
      ?: BUILD_DIR.toAbsolutePath().resolve("out").resolve("ide-tests").createDirectories()
}

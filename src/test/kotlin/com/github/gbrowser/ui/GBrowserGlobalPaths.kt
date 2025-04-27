package com.github.gbrowser.ui

import com.intellij.ide.starter.path.GlobalPaths
import com.intellij.ide.starter.utils.Git

class GBrowserGlobalPaths : GlobalPaths(Git.getRepoRoot().resolve("build")) {}
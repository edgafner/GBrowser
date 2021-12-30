package com.github.gib.services

import com.github.gib.GIdeaBrowserBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(GIdeaBrowserBundle.message("projectService", project.name))
    }
}

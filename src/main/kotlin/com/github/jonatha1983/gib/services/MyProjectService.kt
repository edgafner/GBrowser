package com.github.jonatha1983.gib.services

import com.intellij.openapi.project.Project
import com.github.jonatha1983.gib.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}

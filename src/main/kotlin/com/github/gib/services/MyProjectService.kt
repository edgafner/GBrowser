package com.github.gib.services

import com.intellij.openapi.project.Project
import com.github.gib.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}

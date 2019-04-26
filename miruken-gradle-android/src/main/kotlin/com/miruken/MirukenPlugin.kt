package com.miruken

import org.gradle.api.Plugin
import org.gradle.api.Project

class MirukenPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.create(
                "findMirukenHandlers",
                FindMirukenHandlersTask::class.java)
    }
}



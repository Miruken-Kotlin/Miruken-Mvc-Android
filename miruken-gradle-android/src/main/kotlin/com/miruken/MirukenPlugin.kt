package com.miruken

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property

open class MirukenPluginExtension (project: Project){
    val resourceName:Property<String> = project.objects.property(String::class.java)
    init {
        resourceName.set("${project.name.toLowerCase().replace("-", "_")}_handlers")
    }
}

class MirukenPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("miruken", MirukenPluginExtension::class.java, project)
        project.tasks.create(
                "findMirukenHandlers",
                FindMirukenHandlersTask::class.java){
            it.resourceName.set(extension.resourceName)
        }
    }
}

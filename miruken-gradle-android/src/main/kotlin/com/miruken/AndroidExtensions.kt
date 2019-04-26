package com.miruken

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectCollection
import org.gradle.api.GradleException
import org.gradle.api.Project

val Project.android: BaseExtension
    get() {
        val android    = "android"
        if(!this.project.hasProperty(android))
            throw GradleException("Android gradle plugin is not present")

        return this.extensions.getByName(android) as BaseExtension
    }

val Project.variants: DomainObjectCollection<out BaseVariant>
    get() {
        val android = this.android
        return when (android) {
            is AppExtension ->
                android.applicationVariants
            is LibraryExtension ->
                android.libraryVariants
            else ->
                throw GradleException("Unexpected android extension object")
        }
    }

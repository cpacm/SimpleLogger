package com.cpacm.log

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.cpacm.log.extension.LogExtension
import com.cpacm.log.transform.LogTransform
import com.google.gson.JsonObject
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * <p>
 *
 * @author cpacm 2019-10-24
 */
class LogPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        println("---------Logger Plugin Begin----------")

        // create a extension
        project.extensions.create("loggerConfig", LogExtension::class.java)

        val logTransform = LogTransform(project)
        // 'android' extension for 'com.android.application' projects.
        val android = project.extensions.findByType(AppExtension::class.java)

        if (android == null) {
            // in library module
            val library = project.extensions.findByType(LibraryExtension::class.java)

            logTransform.isAndroidApp = false
            library?.registerTransform(logTransform)
        } else {
            // bind logtransform
            logTransform.isAndroidApp = true
            android.registerTransform(logTransform)
        }

    }
}
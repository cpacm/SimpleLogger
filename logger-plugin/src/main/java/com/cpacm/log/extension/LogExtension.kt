package com.cpacm.log.extension

import org.gradle.api.Action

/**
 * <p>
 *
 * @author cpacm 2019-10-30
 */
open class LogExtension {

    var enableLibrary = false
    var defaultContent = DefaultContent()

    fun defaultContent(action: Action<DefaultContent>) {
        action.execute(defaultContent)
    }

    override fun toString(): String {
        return defaultContent.toString() + enableLibrary
    }

    fun isConfigChange(old: String?):Boolean {
        return toString() != old
    }
}
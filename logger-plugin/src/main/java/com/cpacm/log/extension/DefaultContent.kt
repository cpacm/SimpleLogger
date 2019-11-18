package com.cpacm.log.extension

/**
 * <p>
 *
 * @author cpacm 2019-11-05
 */

open class DefaultContent {
    var clogKey = CLASS_NAME
    var clogContent = "<$METHOD_NAME>:($PARAMS_NAME)"
    var clogLevel = "UNDEFINED"

    var lifeLogKey = CLASS_NAME
    var lifeStartContent = "Lifecycle Start at <$METHOD_NAME>:($PARAMS_NAME)"
    var lifeRunningContent = "Lifecycle Running at <$METHOD_NAME>:($PARAMS_NAME)"
    var lifeEndContent = "Lifecycle End at <$METHOD_NAME>:($PARAMS_NAME)"
    var lifeLevel = "UNDEFINED"

    var mlogKey = CLASS_NAME
    var mlogContent = "<$METHOD_NAME>:($PARAMS_NAME)"
    var mlogLevel = "UNDEFINED"

    var tlogKey = CLASS_NAME
    var tlogContent = "<$METHOD_NAME>:cost mills $TIME_NAME"
    var tlogLevel = "UNDEFINED"

    fun clogLevel(s: String) {
        this.clogLevel = s
    }

    fun clogContent(s: String) {
        this.clogContent = s
    }

    fun clogKey(s: String) {
        this.clogKey = s
    }

    fun lifeLevel(s: String) {
        this.lifeLevel = s
    }

    fun lifeLogKey(s: String) {
        this.lifeLogKey = s
    }

    fun lifeStartContent(s: String) {
        this.lifeStartContent = s
    }

    fun lifeRunningContent(s: String) {
        this.lifeRunningContent = s
    }

    fun lifeEndContent(s: String) {
        this.lifeEndContent = s
    }

    fun mlogLevel(s: String) {
        this.mlogLevel = s
    }

    fun mlogKey(s: String) {
        this.mlogKey = s
    }

    fun mlogContent(s: String) {
        this.mlogContent = s
    }


    fun tlogKey(s: String) {
        this.tlogKey = s
    }

    fun tlogContent(s: String) {
        this.tlogContent = s
    }

    fun tlogLevel(s: String) {
        this.tlogLevel = s
    }

    companion object {
        const val CLASS_NAME = "{className}"
        const val METHOD_NAME = "{methodName}"
        const val PARAMS_NAME = "{params}"
        const val TIME_NAME = "{time}"
    }

    override fun toString(): String {
        return clogKey + clogContent + clogLevel + lifeLogKey + lifeStartContent +
                lifeRunningContent + lifeEndContent + lifeLevel + mlogKey +
                mlogContent + mlogLevel + tlogKey + tlogContent + tlogLevel
    }
}
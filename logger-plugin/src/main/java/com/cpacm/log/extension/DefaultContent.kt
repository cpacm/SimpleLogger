package com.cpacm.log.extension

/**
 * <p>
 *
 * @author cpacm 2019-11-05
 */

open class DefaultContent {
    var clogKey = "{className}"
    var clogContent = "<{methodName}>:({params})"
    var clogLevel = "VERBOSE"

    var lifeLogKey = "{className}"
    var lifeStartContent = "Lifecycle Start at <{methodName}>:({params})"
    var lifeRunningContent = "Lifecycle Running at <{methodName}>:({params})"
    var lifeEndContent = "Lifecycle End at <{methodName}>:({params})"
    var lifeLevel = "DEBUG"

    var mlogKey = "{className}"
    var mlogContent = "<{methodName}>:({params})"
    var mlogLevel = "VERBOSE"

    var tlogKey = "{className}"
    var tlogContent = "<{methodName}>:cost mills ({time})"
    var tlogLevel = "VERBOSE"

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

    override fun toString(): String {
        return clogKey + clogContent + clogLevel + lifeLogKey + lifeStartContent +
                lifeRunningContent + lifeEndContent + lifeLevel + mlogKey +
                mlogContent + mlogLevel + tlogKey + tlogContent + tlogLevel
    }
}
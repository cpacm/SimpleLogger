package com.cpacm.logger

import com.cpacm.annotations.LoggerLevel
import java.io.File

/**
 * <p>
 *     Simple Logger for annotations.
 * @author cpacm 2019-10-31
 */
object SimpleLogger {

    private var simpleLoggerImpl: SimpleLoggerImpl? = null

    fun init(config: SimpleLoggerConfig) {
        simpleLoggerImpl = SimpleLoggerImpl(config)
    }

    private fun _log_internal_(
        level: LoggerLevel,
        key: String,
        content: String,
        throwable: Throwable?,
        debug: Boolean,
        specialName: String?
    ) {
        simpleLoggerImpl?._log_internal_(level, key, content, throwable, debug, specialName)
    }

    /**
     * provide for plugin
     */
    @JvmStatic
    fun logger(
        level: String,
        key: String,
        content: String,
        debug: Boolean,
        specialName: String?
    ) {
        val l = when (level) {
            "VERBOSE" -> LoggerLevel.VERBOSE
            "DEBUG" -> LoggerLevel.DEBUG
            "INFO" -> LoggerLevel.INFO
            "WARN" -> LoggerLevel.WARN
            "ERROR" -> LoggerLevel.ERROR
            else -> LoggerLevel.UNDEFINED
        }
        _log_internal_(l, key, content, null, debug, specialName)
    }

    @JvmStatic
    fun lifeLogger(
        id: String,
        status: Int,
        level: String,
        key: String,
        content: String,
        debug: Boolean,
        specialName: String?
    ) {
        if (simpleLoggerImpl?.lifeLog(id, status) == true) {
            val l = when (level) {
                "VERBOSE" -> LoggerLevel.VERBOSE
                "DEBUG" -> LoggerLevel.DEBUG
                "INFO" -> LoggerLevel.INFO
                "WARN" -> LoggerLevel.WARN
                "ERROR" -> LoggerLevel.ERROR
                else -> LoggerLevel.UNDEFINED
            }
            _log_internal_(l, key, content, null, debug, specialName)
        }
    }


    /****** provide for code , adapter for java*******/

    @JvmStatic
    fun v(key: String, content: String) {
        v(key, content, null, null)
    }

    @JvmStatic
    fun v(key: String, content: String, throwable: Throwable? = null) {
        v(key, content, throwable, null)
    }

    @JvmStatic
    fun v(key: String, content: String, throwable: Throwable? = null, specialName: String? = null) {
        _log_internal_(LoggerLevel.VERBOSE, key, content, throwable, true, specialName)
    }

    @JvmStatic
    fun d(key: String, content: String) {
        d(key, content, null, null)
    }

    @JvmStatic
    fun d(key: String, content: String, throwable: Throwable? = null) {
        d(key, content, throwable, null)
    }

    @JvmStatic
    fun d(key: String, content: String, throwable: Throwable? = null, specialName: String? = null) {
        _log_internal_(LoggerLevel.DEBUG, key, content, throwable, true, specialName)
    }

    @JvmStatic
    fun i(key: String, content: String) {
        i(key, content, null, null)
    }

    @JvmStatic
    fun i(key: String, content: String, throwable: Throwable? = null) {
        i(key, content, throwable, null)
    }

    @JvmStatic
    fun i(key: String, content: String, throwable: Throwable? = null, specialName: String? = null) {
        _log_internal_(LoggerLevel.INFO, key, content, throwable, true, specialName)
    }

    @JvmStatic
    fun w(key: String, content: String) {
        w(key, content, null, null)
    }

    @JvmStatic
    fun w(key: String, content: String, throwable: Throwable? = null) {
        w(key, content, throwable, null)
    }

    @JvmStatic
    fun w(key: String, content: String, throwable: Throwable? = null, specialName: String? = null) {
        _log_internal_(LoggerLevel.WARN, key, content, throwable, true, specialName)
    }

    @JvmStatic
    fun e(key: String, content: String) {
        e(key, content, null, null)
    }

    @JvmStatic
    fun e(key: String, content: String, throwable: Throwable? = null) {
        e(key, content, throwable, null)
    }

    @JvmStatic
    fun e(key: String, content: String, throwable: Throwable? = null, specialName: String? = null) {
        _log_internal_(LoggerLevel.ERROR, key, content, throwable, true, specialName)
    }

    @JvmStatic
    fun getLogFiles():List<File>?{
        return simpleLoggerImpl?.getLogFiles()
    }

    @JvmStatic
    fun getLogAllFiles():List<File>?{
        return simpleLoggerImpl?.getLogFiles()
    }


}
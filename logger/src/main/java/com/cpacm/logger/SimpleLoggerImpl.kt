package com.cpacm.logger

import android.util.Log
import com.cpacm.annotations.LoggerLevel
import de.mindpipe.android.logging.log4j.LogConfigurator
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.PatternLayout
import org.apache.log4j.RollingFileAppender
import java.io.File
import java.io.IOException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

/**
 * <p>
 *     logger实现类
 * @author cpacm 2019-10-31
 */
internal class SimpleLoggerImpl(private val config: SimpleLoggerConfig) {

    private lateinit var debugLogger: Logger
    private lateinit var infoLogger: Logger
    private lateinit var errorLogger: Logger

    private val logFileMap = hashMapOf<String, String>()


    init {
        try {
            // init log4j
            val logConfigurator = LogConfigurator()
            logConfigurator.rootLevel = org.apache.log4j.Level.DEBUG
            logConfigurator.isUseLogCatAppender = false
            logConfigurator.isUseFileAppender = false
            logConfigurator.isInternalDebugging = false
            logConfigurator.configure()

            //with all log
            val debugAppender = getFileAppender("debug.log")

            //with info and warn log
            val infoAppender = getFileAppender("info.log")

            // error log
            val errorAppender = getFileAppender("error.log", true)

            debugLogger = Logger.getLogger("debug")
            debugLogger.addAppender(debugAppender)

            infoLogger = Logger.getLogger("info")
            infoLogger.addAppender(infoAppender)
            infoLogger.addAppender(debugAppender)

            errorLogger = Logger.getLogger("error")
            errorLogger.addAppender(errorAppender)
            errorLogger.addAppender(infoAppender)
            errorLogger.addAppender(debugAppender)

        } catch (e: RuntimeException) {
            Log.e("SimpleLogger", e.message, e)
        } catch (e1: IOException) {
            Log.e("SimpleLogger", "Exception configuring log system", e1)
        }
    }

    private val lifeLogQueue = LinkedBlockingQueue<String>(20)


    fun _log_internal_(
        level: LoggerLevel,
        key: String,
        content: String,
        throwable: Throwable?,
        debug: Boolean,
        specialName: String?
    ) {
        var levelTmp = level
        if (level == LoggerLevel.UNDEFINED) {
            levelTmp = getDefaultLevel()
        }
        when (levelTmp) {
            LoggerLevel.DEBUG -> d(
                key,
                content,
                throwable,
                debug,
                specialName
            )
            LoggerLevel.INFO -> i(
                key,
                content,
                throwable,
                debug,
                specialName
            )
            LoggerLevel.WARN -> w(
                key,
                content,
                throwable,
                debug,
                specialName
            )
            LoggerLevel.ERROR -> e(
                key,
                content,
                throwable,
                specialName
            )
            else -> v(
                key,
                content,
                throwable,
                debug,
                specialName
            )
        }
    }


    /**
     * @param id log类的特定id
     * @param status 0表示开始标记，1表示标记进行中，2表示退出标记
     * @return 返回是否成功操作
     */
    fun lifeLog(id: String, status: Int): Boolean {
        if (status == 0) {
            if (!lifeLogQueue.contains(id)) {
                lifeLogQueue.put(id)
                return true
            }
            return false
        }
        if (status == 1) {
            return lifeLogQueue.contains(id)
        }
        if (status == 2) {
            return lifeLogQueue.remove(id)
        }
        return false
    }

    fun v(
        tag: String,
        msg: Any?,
        throwable: Throwable? = null,
        debug: Boolean = true,
        specialName: String? = null
    ) {
        if (debug && !config.debugEnv) return
        val logger =
            if (specialName != null && specialName.isNotEmpty()) registerSpecialLog(specialName) else debugLogger

        logger.log(Level.DEBUG, "[" + tag + "] " + (msg ?: ""), throwable)
        var l = msg?.toString() ?: "null"
        while (l.length > 3500) {
            Log.v(tag, l.substring(0, 3500), throwable)
            l = l.substring(3500, l.length)
        }
        Log.v(tag, l)
    }

    fun d(
        tag: String, msg: Any?,
        throwable: Throwable? = null,
        debug: Boolean = true,
        specialName: String? = null
    ) {
        if (debug && !config.debugEnv) return
        val logger =
            if (specialName != null && specialName.isNotEmpty()) registerSpecialLog(specialName) else debugLogger
        logger.log(Level.DEBUG, "[" + tag + "] " + (msg ?: ""), throwable)
        var l = msg?.toString() ?: "null"
        while (l.length > 3500) {
            Log.d(tag, l.substring(0, 3500), throwable)
            l = l.substring(3500, l.length)
        }
        Log.d(tag, l)
    }

    fun i(
        tag: String, msg: Any?, throwable: Throwable? = null,
        debug: Boolean = true,
        specialName: String? = null
    ) {
        if (debug && !config.debugEnv) return
        val logger =
            if (specialName != null && specialName.isNotEmpty()) registerSpecialLog(specialName) else infoLogger
        logger.log(Level.INFO, "[" + tag + "] " + (msg ?: ""), throwable)
        var l = msg?.toString() ?: "null"
        while (l.length > 3500) {
            Log.i(tag, l.substring(0, 3500), throwable)
            l = l.substring(3500, l.length)
        }
        Log.i(tag, l)
    }

    fun w(
        tag: String, msg: Any?, throwable: Throwable? = null,
        debug: Boolean = true,
        specialName: String? = null
    ) {
        if (debug && !config.debugEnv) return
        val logger =
            if (specialName != null && specialName.isNotEmpty()) registerSpecialLog(specialName) else infoLogger

        logger.log(Level.WARN, "[" + tag + "] " + (msg ?: ""), throwable)
        var l = msg?.toString() ?: "null"
        while (l.length > 3500) {
            Log.w(tag, l.substring(0, 3500), throwable)
            l = l.substring(3500, l.length)
        }
        Log.w(tag, l)
    }

    fun e(
        tag: String, msg: Any?, throwable: Throwable? = null,
        specialName: String? = null
    ) {
        val logger =
            if (specialName != null && specialName.isNotEmpty()) registerSpecialLog(specialName) else errorLogger
        logger.log(Level.ERROR, "[" + tag + "] " + (msg ?: ""), throwable)
        var l = msg?.toString() ?: "null"
        while (l.length > 3500) {
            Log.e(tag, l.substring(0, 3500), throwable)
            l = l.substring(3500, l.length)
        }
        Log.e(tag, l)
    }

    private fun registerSpecialLog(name: String): Logger {
        val appender = getFileAppender("$name.log", true)
        val logger = Logger.getLogger(name)
        logger.addAppender(appender)
        logger.addAppender(debugLogger.getAppender("debug.log"))
        return logger
    }


    private fun getFileAppender(name: String, immediate: Boolean = false): RollingFileAppender {
        val ROLLING_FILE_SIZE = 4194304L
        val pattern = PatternLayout("%d %-5p %m%n")
        val path = File(config.filePath, name).absolutePath
        logFileMap.put(name, path)
        val appender =
            RollingFileAppender(pattern, path)
        appender.encoding = "utf-8"
        appender.maxBackupIndex = 3
        appender.maximumFileSize = ROLLING_FILE_SIZE
        appender.immediateFlush = immediate
        return appender
    }

    fun getDefaultLevel(): LoggerLevel {
        return config.defaultLevel
    }


}
package com.cpacm.logger

import android.content.Context
import android.os.Environment
import com.cpacm.annotations.LoggerLevel
import java.io.File

/**
 * <p>
 *
 * @author cpacm 2019-10-31
 */
class SimpleLoggerConfig {

    val defaultLevel: LoggerLevel
    val filePath: String
    val debugEnv: Boolean

    constructor(
        context: Context,
        debugEnv: Boolean = false,
        level: LoggerLevel = LoggerLevel.DEBUG,
        filePath: String = getDefaultPath(context)
    ) {
        this.debugEnv = debugEnv
        this.defaultLevel = level
        this.filePath = filePath
    }

    constructor(
        debugEnv: Boolean = false,
        level: LoggerLevel = LoggerLevel.DEBUG,
        filePath:String
    ) {
        this.debugEnv = debugEnv
        this.defaultLevel = level
        this.filePath = filePath
    }

    companion object {

        const val LOGGER_DIR = "logs"

        fun getDefaultPath(context: Context): String {
            var fileDir: File? = null
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
                fileDir = context.getExternalFilesDir(LOGGER_DIR)
                if (fileDir != null) return fileDir.absolutePath
            }
            if (fileDir == null) {
                fileDir = context.filesDir
                if (fileDir != null) {
                    val path = fileDir.absolutePath + File.separator + LOGGER_DIR
                    val path1 = File(path)
                    if (!path1.exists()) {
                        path1.mkdirs()
                    }
                    return path1.absolutePath
                }
            }
            val root = File(Environment.getExternalStorageDirectory(), LOGGER_DIR)
            if (!root.exists()) {
                root.mkdirs()
            }
            return root.absolutePath
        }
    }


}
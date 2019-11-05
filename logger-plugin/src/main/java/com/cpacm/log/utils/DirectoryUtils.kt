package com.cpacm.log.utils

import java.io.File

/**
 * <p>
 *
 * @author cpacm 2019-10-24
 */
object DirectoryUtils {

    /**
     * 获取某个目录下的所有文件，递归查找
     * @param dir 需要递归查找所有文件的目录
     * @return 返回含有所有平铺file的list结构，永远不为空
     */
    fun getAllFiles(dir: File): List<File> {
        val res = ArrayList<File>()
        if (!dir.isDirectory()) {
            res.add(dir)
            return res
        }
        val childFiles = dir.listFiles()
        if (childFiles == null || childFiles.size == 0) {
            return res
        }
        for (file in childFiles) {
            if (file.isDirectory()) {
                res.addAll(getAllFiles(file))
            } else {
                res.add(file)
            }
        }
        return res
    }

    /**
     * 删除某个目录的所有同级目录，包括自己
     * @param dir 要删除的某个目录
     */
    fun deleteSameLevelDirs(dir: String?) {
        if (dir == null) {
            return
        }
        val childFiles = File(dir).parentFile.listFiles()
        if (childFiles == null) {
            return
        }
        for (file in childFiles) {
            if (file.isDirectory) {
                file.delete()
            }
        }
    }

    /**
     * 判断某个路径的父级目录下是否有目录
     * @param path 要查看的路径
     * @return 有目录返回true，没有返回false
     */
    fun hasDir(path: String?): Boolean {
        if (path == null) {
            return false
        }
        val files = File(path).parentFile.listFiles() ?: return false
        for (file in files) {
            if (file.isDirectory) {
                return true
            }
        }
        return false

    }

    /**
     * 删除某个目录下的所有jar文件
     * @param path 目录地址
     */
    fun deleteAllJars(path: String?) {
        if (path == null) {
            return
        }
        val files = File(path).listFiles() ?: return
        for (file in files) {
            if (file.name.endsWith(".jar")) {
                file.delete()
            }
        }
    }

    /**
     * 删除某个路径下同级的多余的jar文件
     * @param validFiles 要保留的jar文件
     */
    fun deleteReduantJar(path: String, validFiles: Set<String>) {
        val file = File(path)
        if (file.parentFile == null) {
            return
        }
        val files = File(path).parentFile.listFiles() ?: return
        for (f in files) {
            if (!validFiles.contains(f.absolutePath) && f.absolutePath.endsWith(".jar")) {
                f.delete()
            }
        }
    }
}
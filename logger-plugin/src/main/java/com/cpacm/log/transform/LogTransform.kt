package com.cpacm.log.transform

import com.android.build.api.transform.*
import com.cpacm.log.asm.LogClassVisitor
import com.cpacm.log.extension.LogExtension
import com.cpacm.log.utils.DirectoryUtils
import com.google.gson.Gson
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap
import java.net.URLClassLoader


/**
 * <p>
 *     在这里可以对已经编译完毕的class文件进行修改
 *     常用修改工具：ASM:ClassVisitor可以查找相关特征
 *     和javassist
 * @author cpacm 2019-10-24
 */
class LogTransform(private val project: Project) : Transform() {

    var isAndroidApp = false
    var urlClassLoader: URLClassLoader? = null

    override fun getName(): String {
        return TAG
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return mutableSetOf(QualifiedContent.DefaultContentType.CLASSES)
    }

    /**
     * 暂不支持第三方jar包的处理
     * 需要每个模块都要导入该插件
     */
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        val scopes = hashSetOf<QualifiedContent.Scope>()
        scopes.add(QualifiedContent.Scope.PROJECT)
        if (isAndroidApp) {
            //application module中加入此项可以处理第三方jar包
            //scopes.add(QualifiedContent.Scope.EXTERNAL_LIBRARIES)
        }
        return scopes
    }

    override fun isIncremental(): Boolean {
        return true
    }


    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        if (transformInvocation == null) return
        val logExtension = project.extensions.findByType(LogExtension::class.java) ?: LogExtension()

        //此次是否是增量编译
        var isIncrement = false

        //保存上次依赖jar文件和输出的jar文件的依赖关系
        val lastConfigFile =
            File(transformInvocation.context.temporaryDir.absolutePath + File.separator + "config.json")

        //用来保存上一次jar文件的名称和输出路径的映射
        var lastJarMap = hashMapOf<String, String>()
        val gson = Gson()
        if (lastConfigFile.exists()) {
            // 非第一次编译
            isIncrement = true
            try {
                val lines = FileUtils.readLines(lastConfigFile)
                if (lines == null || lines.size == 0 || lines.size > 1) {
                    throw IllegalStateException("bad config file ,please clean the project and rebuild it")
                }
                val lastConfig = gson.fromJson(lines[0], LogConfig::class.java)
                if (lastConfig?.logConfigStr == null) {
                    //gson文件损坏
                    isIncrement = false
                } else {
                    lastJarMap = lastConfig.jarMap
                    val isConfigChange = logExtension.isConfigChange(lastConfig.logConfigStr)
                    if (isConfigChange) {
                        isIncrement = false
                    }
                }
            } catch (e: Exception) {
                isIncrement = false
            }
        }
        println("isIncrement:$isIncrement")
        if (!isIncrement) {
            transformInvocation.outputProvider.deleteAll()
        }
        val newJarMap = hashMapOf<String, String>()

        urlClassLoader = AndroidClassLoader.getClassLoader(
            transformInvocation.inputs,
            transformInvocation.referencedInputs,
            project
        )

        transformInvocation.inputs.forEach {
            transformSrc(transformInvocation, it, isIncrement)
            transformJar(transformInvocation, it, lastJarMap, newJarMap, isIncrement)
        }

        if (isIncrement) {
            //进入增量编译，移除被标记删除的jar
            val iterator = lastJarMap.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val file = File(entry.value)
                if (file.exists()) {
                    file.delete()
                }
            }
        }

        val newLogConfig = LogConfig()
        newLogConfig.logConfigStr = logExtension.toString()
        newLogConfig.jarMap = newJarMap
        val lines = Arrays.asList(gson.toJson(newLogConfig))
        FileUtils.writeLines(lastConfigFile, lines)
    }

    private fun transformSrc(
        transformInvocation: TransformInvocation,
        inputs: TransformInput,
        isIncrement: Boolean
    ) {

        inputs.directoryInputs.forEach { input ->
            val outputDirFile = transformInvocation.outputProvider
                .getContentLocation(input.name, input.contentTypes, input.scopes, Format.DIRECTORY)
            val outputFilePath = outputDirFile.absolutePath
            val inputFilePath = input.file.absolutePath

            if (isIncrement) {
                //此时进入增量编译
                if (input.changedFiles.isNotEmpty()) {
                    val changeFiles = input.changedFiles
                    for (fileEntry in changeFiles) {
                        if ((fileEntry.value == Status.CHANGED || fileEntry.value == Status.ADDED)
                            && !fileEntry.key.isDirectory
                        ) {
                            transformClass(fileEntry.key, inputFilePath, outputFilePath)
                        } else if (fileEntry.value == Status.REMOVED) {
                            val outputFullPath =
                                fileEntry.key.absolutePath.replace(inputFilePath, outputFilePath)
                            val outputFile = File(outputFullPath)
                            outputFile.delete()
                        }
                    }
                }
                //else 之前已经编译过，不做任何处理
            } else {
                val allFiles = DirectoryUtils.getAllFiles(input.file)
                for (file in allFiles) {
                    transformClass(file, inputFilePath, outputFilePath)
                }
            }

        }
    }

    private fun transformClass(
        file: File, inputFilePath: String, outputFilePath: String
    ) {
        val name = file.name
        val outputFullPath = file.absolutePath.replace(inputFilePath, outputFilePath)
        val outputFile = File(outputFullPath)
        if (!outputFile.parentFile.exists()) {
            outputFile.parentFile.mkdirs()
        }
        if (name.endsWith(".class") && !name.contains("R\$")
            && !name.endsWith("R.class")
            && !name.endsWith("BuildConfig.class")
        ) {
            // 获取ClassReader，参数是文件的字节数组
            val classReader = ClassReader(file.readBytes())
            var className = file.name
            try {
                className = file.name.split(".")[0]
            } catch (e: Exception) {
                println(e.message)
            }
            val classWriter = AndroidClassWriter(urlClassLoader, ClassWriter.COMPUTE_MAXS)
            //val classWriterWrapper = wrapClassWriter(classWriter)
            // 获取ClassWriter，参数1是reader，参数2用于修改类的默认行为，一般传入ClassWriter.COMPUTE_MAXS
            //val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            //自定义ClassVisitor
            val classVisitor = LogClassVisitor(Opcodes.ASM6, classWriter, className)
            //执行过滤操作
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)

            val bytes = classWriter.toByteArray()
            val destFile = File(file.parentFile.absoluteFile, name)
            val fileOutputStream = FileOutputStream(destFile);
            fileOutputStream.write(bytes)
            fileOutputStream.close()
        }
        FileUtils.copyFile(file, outputFile)
    }

    private fun transformJar(
        transformInvocation: TransformInvocation, inputs: TransformInput,
        lastJarMap: HashMap<String, String>,
        newJarMap: HashMap<String, String>,
        isIncrement: Boolean
    ) {
        //由于不对jar包修改，所以只要将jar复制过去就行了
        inputs.jarInputs.forEach { input ->
            val outputFile = transformInvocation.outputProvider.getContentLocation(
                input.name, input.contentTypes, input.scopes,
                Format.JAR
            )
            newJarMap.put(input.name, outputFile.absolutePath)
            if (!isIncrement) {
                FileUtils.copyFile(input.file, outputFile)
            } else {
                //进入增量编译
                if (lastJarMap.containsKey(name)) {
                    //排除已经存在的jar
                    lastJarMap.remove(name)
                }
                if (input.status == Status.CHANGED || input.status == Status.ADDED) {
                    FileUtils.copyFile(input.file, outputFile)
                }
                //else 剩下的为 NOTCHANGED 和 REMOVED 先不做处理
            }

            FileUtils.copyFile(input.file, outputFile)
        }
    }

    companion object {
        val TAG = "SimpleLogger"
    }

}
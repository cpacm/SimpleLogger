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

/**
 * <p>
 *     在这里可以对已经编译完毕的class文件进行修改
 *     常用修改工具：ASM:ClassVisitor可以查找相关特征
 *     和javassist
 * @author cpacm 2019-10-24
 */
class LogTransform(private val project: Project) : Transform() {

    var isAndroidApp = false

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
        var logExtension = project.extensions.findByType(LogExtension::class.java) ?: LogExtension()

        //此次是否是增量编译
        var isIncrement = false
        var isConfigChange = false

        //保存上次依赖jar文件和输出的jar文件的依赖关系
        val lastConfigFile =
            File(transformInvocation.context.temporaryDir.absolutePath + File.separator + "config.json")

        //用来保存jar文件的名称和输出路径的映射
        var lastJarMap = hashMapOf<String, String>()

        if (lastConfigFile.exists()) {
            try {
                val gson = Gson()
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
                    isConfigChange = logExtension.isConfigChange(lastConfig.logConfigStr)
                }
            } catch (e: Exception) {
                isIncrement = false
            }
        }

        //TODO judge incremental

        transformInvocation.inputs.forEach {
            transformSrc(transformInvocation, it)
            transformJar(transformInvocation, it)
        }

    }

    private fun transformSrc(transformInvocation: TransformInvocation, inputs: TransformInput) {
        inputs.directoryInputs.forEach { input ->
            val outputDirFile = transformInvocation.outputProvider
                .getContentLocation(input.name, input.contentTypes, input.scopes, Format.DIRECTORY)
            val outputFilePath = outputDirFile.absolutePath
            val inputFilePath = input.file.absolutePath

            //TODO judge incremental

            val allFiles = DirectoryUtils.getAllFiles(input.file)
            for (file in allFiles) {
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
                    System.out.println(file.absolutePath)

                    // 获取ClassReader，参数是文件的字节数组
                    val classReader = ClassReader(file.readBytes())
                    var className = file.name
                    try {
                        className = file.name.split(".")[0]
                    } catch (e: Exception) {
                        println(e.message)
                    }

                    // 获取ClassWriter，参数1是reader，参数2用于修改类的默认行为，一般传入ClassWriter.COMPUTE_MAXS
                    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
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

        }
    }

    private fun transformJar(transformInvocation: TransformInvocation, inputs: TransformInput) {
        inputs.jarInputs.forEach { input ->
            val outputFile = transformInvocation.outputProvider.getContentLocation(
                input.name, input.contentTypes, input.scopes,
                Format.JAR
            )

            //input.file.renameTo(outputFile)
            FileUtils.copyFile(input.file, outputFile)
        }
    }

    companion object {
        val TAG = "SimpleLogger"
    }

}
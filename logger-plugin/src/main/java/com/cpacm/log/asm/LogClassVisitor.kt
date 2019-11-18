package com.cpacm.log.asm

import com.cpacm.log.extension.LogExtension
import org.objectweb.asm.*

/**
 * <p>
 *
 * @author cpacm 2019-10-30
 */
class LogClassVisitor(
    api: Int,
    classWriter: ClassWriter,
    private val className: String,
    private val logExtension: LogExtension
) :
    ClassVisitor(api, classWriter) {

    private var classLog: LogAnnotation? = null
    private var lifeLog: LogAnnotation? = null

    /**
     * 只能检索class上的注解
     */
    override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor {
        if (desc?.equals("Lcom/cpacm/annotations/CLog;") == true) {
            println("----- $className:@CLog -----")
            classLog = LogAnnotation("CLog")
            classLog!!.key = logExtension.defaultContent.clogKey
            classLog!!.content = logExtension.defaultContent.clogContent
            classLog!!.level = logExtension.defaultContent.clogLevel
            val av = cv.visitAnnotation(desc, visible)
            return LogAnnotationVisitor(api, av, classLog!!)
        } else if (desc?.equals("Lcom/cpacm/annotations/LifeLog;") == true) {
            println("----- $className:@LifeLog -----")
            lifeLog = LogAnnotation("LifeLog")
            lifeLog!!.key = logExtension.defaultContent.lifeLogKey
            lifeLog!!.content = logExtension.defaultContent.lifeRunningContent
            lifeLog!!.level = logExtension.defaultContent.lifeLevel
            val av = cv.visitAnnotation(desc, visible)
            return LogAnnotationVisitor(api, av, lifeLog!!)
        } else {
            return super.visitAnnotation(desc, visible)
        }
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        desc: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        if (SkipMethodUtils.shallSkipMethod(name))
            return super.visitMethod(access, name, desc, signature, exceptions)

        val isStatic = access and Opcodes.ACC_STATIC == Opcodes.ACC_STATIC
        val mv = cv.visitMethod(access, name, desc, signature, exceptions)
        val logMethodVisitor =
            LogMethodVisitor(api, mv, access, name!!, desc, classLog, lifeLog, isStatic, className,logExtension)

        return logMethodVisitor
    }
}
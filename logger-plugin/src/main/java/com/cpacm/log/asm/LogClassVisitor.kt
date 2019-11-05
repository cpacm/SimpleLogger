package com.cpacm.log.asm

import org.objectweb.asm.*

/**
 * <p>
 *
 * @author cpacm 2019-10-30
 */
class LogClassVisitor(api: Int, classWriter: ClassWriter, private val className: String) :
    ClassVisitor(api, classWriter) {

    private var classLog: LogAnnotation? = null
    private var lifeLog: LogAnnotation? = null

    /**
     * 只能检索class上的注解
     */
    override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor {
        if (desc?.equals("Lcom/cpacm/annotations/CLog;") == true) {
            classLog = LogAnnotation("CLog")
            classLog!!.key = className
            val av = cv.visitAnnotation(desc, visible)
            return LogAnnotationVisitor(api, av, classLog!!)
        } else if (desc?.equals("Lcom/cpacm/annotations/LifeLog;") == true) {
            lifeLog = LogAnnotation("LifeLog")
            lifeLog!!.key = className
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
        val logMethodVisitor = LogMethodVisitor(api, mv, access, name!!, desc, classLog, lifeLog,isStatic,className)

        return logMethodVisitor
    }
}
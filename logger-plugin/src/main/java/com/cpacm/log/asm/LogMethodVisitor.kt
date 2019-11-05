package com.cpacm.log.asm

import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import com.cpacm.log.asm.LogAnnotation
import com.squareup.javapoet.ClassName


/**
 *
 *
 *
 * @author cpacm 2019-10-30
 */
class LogMethodVisitor
/**
 * Creates a new [AdviceAdapter].
 *
 * @param api    the ASM API version implemented by this visitor. Must be one
 * of [Opcodes.ASM4], [Opcodes.ASM5] or [Opcodes.ASM6].
 * @param mv     the method visitor to which this adapter delegates calls.
 * @param access the method's access flags (see [Opcodes]).
 * @param name   the method's name.
 * @param desc   the method's descriptor (see [Type]).
 */
constructor(
    api: Int,
    mv: MethodVisitor,
    access: Int,
    private val name: String,
    private val desc: String?,
    private val clog: LogAnnotation?,
    private val lifelog: LogAnnotation?,
    private val isStatic: Boolean = false,
    private val className: String
) : AdviceAdapter(api, mv, access, name, desc) {

    private val paramList: ArrayList<String>
    private val annotationMap: HashMap<String, LogAnnotation>

    init {
        paramList = getMethodParamsCount(desc)
        annotationMap = hashMapOf()
    }

    /**
     * 获取参数名在 {@link onMethodEnter()} 之后，且代码混淆后参数名无意义，故不做提取
     */
    override fun visitLocalVariable(
        name: String?,
        desc: String?,
        signature: String?,
        start: Label?,
        end: Label?,
        index: Int
    ) {
        super.visitLocalVariable(name, desc, signature, start, end, index)
    }

    /**
     * 会检索方法上的注解
     */
    override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor {
        if (desc == null) return super.visitAnnotation(desc, visible)
        var log: LogAnnotation? = null
        if (desc.equals("Lcom/cpacm/annotations/LifeLogEnd;")) {
            log = LogAnnotation("LifeLogEnd")
            log.key = className
            annotationMap.put("LifeLogEnd", log)
        } else if (desc.equals("Lcom/cpacm/annotations/LifeLogStart;")) {
            log = LogAnnotation("LifeLogStart")
            log.key = className
            annotationMap.put("LifeLogStart", log)
        } else if (desc.equals("Lcom/cpacm/annotations/MLog;")) {
            log = LogAnnotation("MLog")
            log.key = className
            annotationMap.put("MLog", log)
        } else if (desc.equals("Lcom/cpacm/annotations/TLog;")) {
            log = LogAnnotation("TLog")
            log.key = className
            annotationMap.put("TLog", log)
        } else if (desc.equals("Lcom/cpacm/annotations/NoLog;")) {
            log = LogAnnotation("NoLog")
            annotationMap.put("NoLog", log)
        }
        if (log == null) {
            return super.visitAnnotation(desc, visible)
        }
        val av = mv.visitAnnotation(desc, visible)
        return LogAnnotationVisitor(api, av, log)
    }

    override fun onMethodEnter() {
        if (annotationMap.containsKey("NoLog")) return
        if (clog != null) {
            executeCLog()
        }
        executeMLog()
        executeTLogEnter()

        if (lifelog != null) {
            executeLifeLog()
        }
    }

    override fun onMethodExit(opcode: Int) {
        if (annotationMap.containsKey("NoLog")) return
        executeTLogExit()
    }

    private fun executeLifeLog() {
        val logKey = "${lifelog!!.key}"
        var logContent = "Lifecycle Running at <${name}>:("
        val level = lifelog.level
        val debug = lifelog.debug
        val special = lifelog.special ?: ""

        var status = 1
        if (annotationMap.containsKey("LifeLogStart")) {
            status = 0
            logContent = "Lifecycle Start at <${name}>:("
        } else if (annotationMap.containsKey("LifeLogEnd")) {
            status = 2
            logContent = "Lifecycle End at <${name}>:("
        }
        generateLifeLogger(status, logKey, logContent, level, debug, special)
    }

    private fun executeMLog() {
        val log = annotationMap["MLog"] ?: return

        val logKey = "${log.key}"
        val logContent = "<${name}>:("
        val level = log.level
        val debug = log.debug
        val special = log.special ?: ""

        generateLogger(logKey, logContent, level, debug, special)
    }

    private fun executeCLog() {
        //MLog的注解优先
        if (annotationMap.containsKey("MLog")) {
            return
        }
        val logKey = "${clog!!.key}"
        val logContent = "<${name}>:("
        val level = clog.level
        val debug = clog.debug
        val special = clog.special ?: ""

        generateLogger(logKey, logContent, level, debug, special)
    }

    private fun executeTLogEnter() {
        val log = annotationMap["TLog"] ?: return
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "java/lang/System",
            "currentTimeMillis",
            "()J",
            false
        )
        mv.visitVarInsn(Opcodes.LSTORE, LSTORE_PARAM)
    }

    private fun executeTLogExit() {
        val log = annotationMap["TLog"] ?: return
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        mv.visitVarInsn(LLOAD, LSTORE_PARAM)
        mv.visitInsn(LSUB)
        mv.visitVarInsn(LSTORE, LSTORE_RESULT)

        val logKey = "${log.key}"
        val logContent = "<${name}>:cost mills--"
        val level = log.level
        val debug = log.debug
        val special = log.special ?: ""

        generateLogger(logKey, logContent, level, debug, special, true, LSTORE_RESULT)
    }

    private fun generateLifeLogger(
        status: Int,
        logKey: String,
        logContent: String,
        level: String,
        debug: Boolean,
        special: String,
        custom: Boolean = false
    ) {
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitLdcInsn("${className}@");
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);
        mv.visitMethodInsn(
            INVOKESTATIC,
            "java/lang/Integer",
            "toHexString",
            "(I)Ljava/lang/String;",
            false
        );
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        );
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "toString",
            "()Ljava/lang/String;",
            false
        )
        mv.visitLdcInsn(status)
        mv.visitLdcInsn(level)
        mv.visitLdcInsn(logKey)
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder")
        mv.visitInsn(DUP)
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        mv.visitLdcInsn(logContent)
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
        if (!custom) {
            val count = paramList.size
            var extraPlus = if (isStatic) 0 else 1
            for (i in 0 until count) {
                val loadCode = getParamCode(paramList[i])
                mv.visitVarInsn(loadCode, i + extraPlus)
                if (loadCode == Opcodes.DLOAD || loadCode == Opcodes.LLOAD) {
                    extraPlus += 1
                }
                mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(${getParamStr(paramList[i])})Ljava/lang/StringBuilder;",
                    false
                )
                if (i != count - 1) {
                    mv.visitLdcInsn(",");
                    mv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        "java/lang/StringBuilder",
                        "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                        false
                    )
                }
            }
            mv.visitLdcInsn(")");
            mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "java/lang/StringBuilder",
                "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                false
            )
        }

        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "toString",
            "()Ljava/lang/String;",
            false
        )
        mv.visitInsn(if (debug) ICONST_1 else ICONST_0)
        mv.visitLdcInsn(special)
        mv.visitMethodInsn(
            INVOKESTATIC,
            "com/cpacm/logger/SimpleLogger",
            "lifeLogger",
            "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;)V",
            false
        )

    }

    private fun generateLogger(
        logKey: String,
        logContent: String,
        level: String,
        debug: Boolean,
        special: String,
        custom: Boolean = false,
        loadVar: Int = 0
    ) {
        mv.visitLdcInsn(level)
        mv.visitLdcInsn(logKey)
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder")
        mv.visitInsn(DUP)
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        mv.visitLdcInsn(logContent)
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
        if (!custom) {
            val count = paramList.size
            var extraPlus = if (isStatic) 0 else 1
            for (i in 0 until count) {
                val loadCode = getParamCode(paramList[i])
                mv.visitVarInsn(loadCode, i + extraPlus)
                if (loadCode == Opcodes.DLOAD || loadCode == Opcodes.LLOAD) {
                    extraPlus += 1
                }
                mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(${getParamStr(paramList[i])})Ljava/lang/StringBuilder;",
                    false
                )
                if (i != count - 1) {
                    mv.visitLdcInsn(",");
                    mv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        "java/lang/StringBuilder",
                        "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                        false
                    )
                }
            }
            mv.visitLdcInsn(")");
            mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "java/lang/StringBuilder",
                "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                false
            )
        }
        if (loadVar > 0) {
            mv.visitVarInsn(Opcodes.LLOAD, LSTORE_RESULT)
            mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "java/lang/StringBuilder",
                "append",
                "(J)Ljava/lang/StringBuilder;",
                false
            )
        }

        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "toString",
            "()Ljava/lang/String;",
            false
        )
        mv.visitInsn(if (debug) ICONST_1 else ICONST_0)
        mv.visitLdcInsn(special)
        mv.visitMethodInsn(
            INVOKESTATIC,
            "com/cpacm/logger/SimpleLogger",
            "logger",
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;)V",
            false
        )

    }

    /**
     * 计算方法上的入参类型
     */
    private fun getMethodParamsCount(desc: String?): ArrayList<String> {
        val result = arrayListOf<String>()
        if (desc == null || desc.isEmpty()) return result
        var isObjectStr = ""
        for (a in desc) {
            if (a == '(') {
                result.clear()
                continue
            }
            if (isObjectStr.isNotEmpty()) {
                isObjectStr += a
                if (a == ';') {
                    result.add(isObjectStr)
                    isObjectStr = ""
                }
                continue
            }
            if (a == 'L') {
                isObjectStr += a
                continue
            }
            if (a == ')') {
                break
            }
            result.add(a.toString())
        }
        return result
    }

    /**
     * 根据类型判断读取类型
     */
    private fun getParamCode(load: String): Int {
        return when (load) {
            "I" -> Opcodes.ILOAD
            "C" -> Opcodes.ILOAD
            "F" -> Opcodes.FLOAD
            "D" -> Opcodes.DLOAD
            "J" -> Opcodes.LLOAD
            "Z" -> Opcodes.ILOAD
            else -> Opcodes.ALOAD
        }
    }

    /**
     * stringbuilder 的方法规避
     */
    private fun getParamStr(str: String): String {
        if (str.startsWith("L") && str.endsWith(";")) {
            if (!str.equals("Ljava/lang/String;")) {
                return "Ljava/lang/Object;"
            }
        }
        return str
    }

    companion object {
        const val LSTORE_PARAM = 10998
        const val LSTORE_RESULT = 10999
    }
}
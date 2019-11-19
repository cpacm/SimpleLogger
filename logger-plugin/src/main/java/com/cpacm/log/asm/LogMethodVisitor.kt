package com.cpacm.log.asm

import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import com.cpacm.log.asm.LogAnnotation
import com.cpacm.log.extension.DefaultContent
import com.cpacm.log.extension.LogExtension
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
    private val className: String,
    private val logExtension: LogExtension
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
            log.key = logExtension.defaultContent.lifeLogKey
            log.content = logExtension.defaultContent.lifeEndContent
            log.level = logExtension.defaultContent.lifeLevel
            annotationMap.put("LifeLogEnd", log)
        } else if (desc.equals("Lcom/cpacm/annotations/LifeLogStart;")) {
            log = LogAnnotation("LifeLogStart")
            log.key = logExtension.defaultContent.lifeLogKey
            log.content = logExtension.defaultContent.lifeStartContent
            log.level = logExtension.defaultContent.lifeLevel
            annotationMap.put("LifeLogStart", log)
        } else if (desc.equals("Lcom/cpacm/annotations/MLog;")) {
            log = LogAnnotation("MLog")
            log.key = logExtension.defaultContent.mlogKey
            log.content = logExtension.defaultContent.mlogContent
            log.level = logExtension.defaultContent.mlogLevel
            annotationMap.put("MLog", log)
        } else if (desc.equals("Lcom/cpacm/annotations/TLog;")) {
            log = LogAnnotation("TLog")
            log.key = logExtension.defaultContent.tlogKey
            log.content = logExtension.defaultContent.tlogContent
            log.level = logExtension.defaultContent.tlogLevel
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
        val logKey = formatContentStr(lifelog!!.key, className, name)
        val logContent: String
        val logContentSuffix: String
        val level = lifelog.level
        val debug = lifelog.debug
        val special = lifelog.special ?: ""
        val hasParams: Boolean
        println("----- @LifeLog-$className:Method<$name> -----")
        var status = 1
        if (annotationMap.containsKey("LifeLogStart")) {
            status = 0
            val log = annotationMap["LifeLogStart"]
            val strPair = splitParamStr(log!!.content, className, name)
            logContent = strPair.first
            if (strPair.second == null) {
                logContentSuffix = ""
                hasParams = false
            } else {
                logContentSuffix = strPair.second!!
                hasParams = true
            }
        } else if (annotationMap.containsKey("LifeLogEnd")) {
            status = 2
            val log = annotationMap["LifeLogEnd"]
            val strPair = splitParamStr(log!!.content, className, name)
            logContent = strPair.first
            if (strPair.second == null) {
                logContentSuffix = ""
                hasParams = false
            } else {
                logContentSuffix = strPair.second!!
                hasParams = true
            }
        } else {
            val strPair = splitParamStr(lifelog.content, className, name)
            logContent = strPair.first
            if (strPair.second == null) {
                logContentSuffix = ""
                hasParams = false
            } else {
                logContentSuffix = strPair.second!!
                hasParams = true
            }
        }

        generateLifeLogger(
            status,
            logKey,
            logContent,
            logContentSuffix,
            level,
            debug,
            special,
            hasParams
        )
    }

    private fun formatContentStr(
        source: String?,
        className: String = "",
        methodName: String = ""
    ): String {
        if (source == null) return className
        val result = source.replace(DefaultContent.CLASS_NAME, className)
            .replace(DefaultContent.METHOD_NAME, methodName)
            .replace(DefaultContent.PARAMS_NAME, "")
            .replace(DefaultContent.TIME_NAME, "")
        return result
    }

    private fun splitParamStr(
        source: String?,
        className: String = "",
        methodName: String = ""
    ): Pair<String, String?> {
        if (source == null) return Pair("", null)
        val result = source.replace(DefaultContent.CLASS_NAME, className)
            .replace(DefaultContent.METHOD_NAME, methodName)
            .replace(DefaultContent.TIME_NAME, "")
        val list = result.split(DefaultContent.PARAMS_NAME, limit = 2)
        return Pair(if (list.isNotEmpty()) list[0] else "", if (list.size > 1) list[1] else null)
    }

    private fun splitTimeStr(
        source: String?,
        className: String = "",
        methodName: String = ""
    ): Pair<String, String?> {
        if (source == null) return Pair("", null)
        val result = source.replace(DefaultContent.CLASS_NAME, className)
            .replace(DefaultContent.METHOD_NAME, methodName)
            .replace(DefaultContent.PARAMS_NAME, "")
        val list = result.split(DefaultContent.TIME_NAME, limit = 2)
        return Pair(if (list.isNotEmpty()) list[0] else "", if (list.size > 1) list[1] else null)
    }


    private fun executeMLog() {
        val log = annotationMap["MLog"] ?: return
        println("----- @MLog-$className:Method<$name> -----")
        val logKey = formatContentStr(log.key, className, name)
        val strPair = splitParamStr(log.content, className, name)
        val logContent = strPair.first
        val logContentSuffix: String
        val hasParams: Boolean
        if (strPair.second == null) {
            logContentSuffix = ""
            hasParams = false
        } else {
            logContentSuffix = strPair.second!!
            hasParams = true
        }
        val level = log.level
        val debug = log.debug
        val special = log.special ?: ""

        generateLogger(logKey, logContent, logContentSuffix, level, debug, special, hasParams)
    }

    private fun executeCLog() {
        //MLog的注解优先
        if (annotationMap.containsKey("MLog")) {
            return
        }
        val logKey = formatContentStr(clog!!.key, className, name)
        val strPair = splitParamStr(clog.content, className, name)
        val logContent = strPair.first
        val logContentSuffix: String
        val hasParams: Boolean
        if (strPair.second == null) {
            logContentSuffix = ""
            hasParams = false
        } else {
            logContentSuffix = strPair.second!!
            hasParams = true
        }
        val level = clog.level
        val debug = clog.debug
        val special = clog.special ?: ""

        generateLogger(logKey, logContent, logContentSuffix, level, debug, special, hasParams)
    }

    private fun executeTLogEnter() {
        val log = annotationMap["TLog"] ?: return
        println("----- @TLog-$className:Method<$name> -----")
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

        val logKey = formatContentStr(log.key, className, name)
        val strPair = splitTimeStr(log.content, className, name)
        val logContent = strPair.first
        val logContentSuffix: String?
        if (strPair.second == null) {
            logContentSuffix = null
        } else {
            logContentSuffix = strPair.second!!
        }
        val level = log.level
        val debug = log.debug
        val special = log.special ?: ""

        generateTLogger(logKey, logContent, logContentSuffix, level, debug, special)
    }

    private fun generateLifeLogger(
        status: Int,
        logKey: String,
        logContent: String,
        logContentSuffix: String,
        level: String,
        debug: Boolean,
        special: String,
        hasParams: Boolean = true
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
        if (hasParams) {
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
            mv.visitLdcInsn(logContentSuffix)
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
        logContentSuffix: String,
        level: String,
        debug: Boolean,
        special: String,
        hasParams: Boolean = true
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
        if (hasParams) {
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
            mv.visitLdcInsn(logContentSuffix);
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
            "logger",
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;)V",
            false
        )

    }

    private fun generateTLogger(
        logKey: String,
        logContent: String,
        logContentSuffix: String?,
        level: String,
        debug: Boolean,
        special: String
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

        mv.visitVarInsn(Opcodes.LLOAD, LSTORE_RESULT)
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(J)Ljava/lang/StringBuilder;",
            false
        )

        if (logContentSuffix != null) {
            mv.visitLdcInsn(logContentSuffix);
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
        var isArray = false
        var isObject = false
        for (a in desc) {
            if (a == '(') {
                result.clear()
                continue
            }
            if (a == ')') {
                break
            }

            if (a == '[') {
                isArray = true
                isObjectStr += a
                continue
            }
            if (a == 'L') {
                isObject = true
                isObjectStr += a
                continue
            }
            if (isObject) {
                isObjectStr += a
                if (a == ';') {
                    result.add(isObjectStr)
                    isObjectStr = ""
                    isObject = false
                }
                continue
            }
            if (isArray) {
                isObjectStr += a
                result.add(isObjectStr)
                isObjectStr = ""
                isArray = false
                continue
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
        if (str.startsWith("[")) {
            return "Ljava/lang/Object;"
        }
        return str
    }

    companion object {
        const val LSTORE_PARAM = 10998
        const val LSTORE_RESULT = 10999
    }
}

package com.cpacm.log.asm

import org.objectweb.asm.AnnotationVisitor


/**
 * <p>
 *     获取类上面的注解值
 * @author cpacm 2019-11-01
 */
class LogAnnotationVisitor(api: Int, av: AnnotationVisitor, private val log: LogAnnotation) :
    AnnotationVisitor(api, av) {

    override fun visit(name: String?, value: Any?) {
        super.visit(name, value)
        if (name.equals("key")) {
            log.key = value.toString()
        }
        if (name.equals("debug")) {
            log.debug = value as Boolean
        }
        if (name.equals("special")) {
            log.special = value.toString()
        }
        if (name.equals("content")) {
            log.content = value.toString()
        }
    }

    override fun visitEnum(name: String?, desc: String?, value: String?) {
        super.visitEnum(name, desc, value)
        if (name.equals("level")) {
            log.level = value.toString()
        }
    }


}
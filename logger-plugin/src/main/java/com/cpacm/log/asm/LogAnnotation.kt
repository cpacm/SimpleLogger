package com.cpacm.log.asm

/**
 * <p>
 *     log注解数据
 * @author cpacm 2019-10-31
 */
class LogAnnotation(val name: String) {

    var key: String? = null
    var content: String? = null
    var level: String = "UNDEFINED"
    var debug: Boolean = true
    var special: String? = null
}
package com.cpacm.log.asm

/**
 * <p>
 *
 * @author cpacm 2019-10-30
 */
object SkipMethodUtils {

    val _DOLLAR_ = "$"
    val _INIT_ = "<init>"
    val _CLINIT_ = "<clinit>"


    fun shallSkipMethod(methodName: String?): Boolean {
        if (methodName == null) return true
        if (methodName.contains(_DOLLAR_))
            return true
        if (methodName.equals(_INIT_) || methodName.equals(_CLINIT_))
            return true

        return false
    }
}
package com.cpacm.aspectjx

import org.junit.Test

import org.junit.Assert.*
import java.lang.StringBuilder

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun annotation_test() {
        val test = com.cpacm.aspectjx.Test()
        test.doSomeThing("cpacm", 1, 44F, StringBuilder("generate cpacm"))
    }
}

package com.cpacm.log.transform

/**
 * <p>
 *
 * @author cpacm 2019-11-05
 */
class LogConfig {

    var logConfigStr: String?=null
    /**
     * 用来保存上次编译中jar包的名称和输出路径对比，从而找出改变的jar
     */
    var jarMap: HashMap<String, String> = hashMapOf()
}
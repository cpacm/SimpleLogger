package com.cpacm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 在方法中插入耗时统计
 *
 * <pre><code>
 * public class Test(){
 *     @TLog(key = "key",content = "cost:")
 *     public void testMethod(int a){
 *         return a;
 *     }
 * }
 *          || ams
 *         vvvv
 * public class Test(){
 *     public void testMethod(int a){
 *         long _logger_time_mills = System.currentTimeMillis();
 *         Logger.d("key", "cost:" + (System.currentTimeMillis() - _logger_time_mills)+ "");
 *         return a;
 *     }
 * }
 * </code></pre>
 *
 * @author cpacm 2019-10-25
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface TLog {

    LoggerLevel level() default LoggerLevel.UNDEFINED;//log级别

    String key() default "";//不定义则默认方法名

    String content() default "";//不定义则默认为时间值

    boolean debug() default true;//是否只在debug中显示

    String special() default "";//是否保存至额外文件

}

package com.cpacm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 在方法中插入log
 *
 * <pre><code>
 * public class Test(){
 *     @MLog
 *     public void testMethod(int a){
 *         return a;
 *     }
 * }
 *          ||
 *         vvvv
 * public class Test(){
 *     public void testMethod(int a){
 *         Logger.v("<testMethod>","a="+a);
 *         return a;
 *     }
 * }
 * </code></pre>
 *
 * @author cpacm 2019-10-25
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface MLog {

    LoggerLevel level() default LoggerLevel.UNDEFINED;//log级别

    String key() default "";//不定义则默认方法名

    String content() default "";//不定义则默认为参数+参数值

    boolean debug() default true;//是否只在debug中显示

    String special() default "";//是否保存至额外文件

}

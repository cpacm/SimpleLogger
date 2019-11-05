package com.cpacm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 对类下的所有方法插入log
 *
 * <pre><code>
 * @CLog
 * public class Test(){
 *     public void testMethod(int a){
 *         return a;
 *     }
 * }
 *          ||
 *         vvvv
 * public class Test(){
 *     public void testMethod(int a){
 *         Logger.v("Test","a="+a);
 *         return a;
 *     }
 * }
 * </code></pre>
 *
 * @author cpacm 2019-10-25
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface CLog {

    LoggerLevel level() default LoggerLevel.UNDEFINED;//log级别

    String key() default "";//log关键词，不定义则为默认类名

    boolean debug() default true;//是否只在debug中显示

    String special() default "";//是否保存至额外文件
}

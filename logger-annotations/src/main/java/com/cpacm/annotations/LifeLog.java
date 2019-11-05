package com.cpacm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 记录生命周期的类,搭配{@link LifeLogStart}和{@link LifeLogEnd}使用
 *
 * <pre><code>
 * @LifeLog
 * public class MainActivity(){
 *     @LifeStartLog
 *     public void onCreate(){
 *
 *     }
 *
 *     @LifeEndLog
 *     public void onDestroy(){
 *
 *     }
 * }
 *          ||
 *         vvvv
 * public class MainActivity(){
 *     public void onCreate(){
 *         Logger.lifeStartLog("key","life start at <onCreate>")
 *     }
 *
 *     public void onStart(){
 *         Logger.lifeLog("key","life running at <onStart>")
 *     }
 *
 *     public void onDestroy(){
 *         Logger.lifeEndLog("key","life end at <onDestroy>")
 *     }
 * }
 * </code></pre>
 *
 * @author cpacm 2019-10-25
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface LifeLog {

    LoggerLevel level() default LoggerLevel.UNDEFINED;//log级别

    String key() default "";//log关键词，不定义则为默认类名

    boolean debug() default true;//是否只在debug中显示

    String special() default "";//是否保存至额外文件
}

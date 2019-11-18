package com.cpacm.sample;

import androidx.annotation.NonNull;

import com.cpacm.annotations.LoggerLevel;
import com.cpacm.annotations.*;
import com.cpacm.logger.SimpleLogger;

import java.util.Arrays;

/**
 * <p>
 *
 * @author cpacm 2019-10-29
 */
@LifeLog(special = "test")
public class Test {

    @LifeLogStart
    public int onLifeStart(int k) {
        int s = 5;
        if (k != 7) {
            s = 10;
            return 100;
        }
        return s;
    }

    @LifeLogEnd
    public int onLifeEnd() {
        return 100;
    }

    @NoLog
    public String lifeLog() {
        SimpleLogger.lifeLogger("name@" + Integer.toHexString(hashCode()), 0, "ERROR", "test", "", true, "");
        return "";
    }


    @MLog(level = LoggerLevel.ERROR)
    public void doSomeThing(String var1, boolean var2, int var3, double var4, char var6, float var7, long var8, Object var10, Integer var11) {
        //SimpleLogger.logger("ERROR", "test", "<doSomeThing>:(" + var1 + "," + var2 + "," + var3 + "," + var4 + "," + var6 + "," + var7 + "," + var8 + "," + var10 + "," + var11 + ")", true, "");
        //s = "cpacm";
        //boolean result = false;
    }


    @MLog(level = LoggerLevel.ERROR)
    public void arrayTest(int[] as,float[][] f,double[][][] var,String[] asd){
    }

    @TLog
    public long getTime(int k) {

        int i = k * 10;
        if (i > 10) {
            return 999L;
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return 0L;
    }

    public static void fuck(String s) {

    }

    @NonNull
    @Override
    public String toString() {
        return "这是测试类啦";
    }
}

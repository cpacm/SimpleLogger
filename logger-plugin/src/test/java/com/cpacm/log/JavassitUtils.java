package com.cpacm.log;

import com.netflix.rewrite.ast.Tr;
import com.netflix.rewrite.parse.OracleJdkParser;
import com.netflix.rewrite.parse.Parser;

import java.util.List;

import javassist.ClassClassPath;
import javassist.ClassPool;

/**
 * <p>
 *     目标为class
 *     只能针对某一个类进行修改，不能针对摸个特征进行搜索
 * @author cpacm 2019-10-25
 */
public class JavassitUtils {



    public static void main(String[] args) {
        renameMethod();
    }


    public static void renameMethod() {
        ClassPool cp = ClassPool.getDefault();

    }
}


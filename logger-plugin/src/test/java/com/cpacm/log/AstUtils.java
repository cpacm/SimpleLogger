package com.cpacm.log;

import com.netflix.rewrite.ast.Tr;
import com.netflix.rewrite.parse.OracleJdkParser;
import com.netflix.rewrite.parse.Parser;
import com.netflix.rewrite.refactor.Refactor;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 *
 * @author cpacm 2019-10-25
 */
public class AstUtils {

    private static Parser parser = new OracleJdkParser();

    public static void main(String[] args) {
        renameMethod();
    }


    public static void renameMethod() {
        Tr.CompilationUnit cu = parser.parse(getClassStr());

        Tr.ClassDecl a = cu.firstClass();

        Tr.CompilationUnit fixed = cu.refactor()
                .addField(a, List.class, "list", "new ArrayList<>()")
                .addImport(java.util.ArrayList.class).fix();

        System.out.println(fixed.print());
    }

    public static String getClassStr() {
        return "public class LogUtils {\n" +
                "    int ff = 10;\n" +
                "\n" +
                "    public void d(String tag, String msg) {\n" +
                "    }\n" +
                "\n" +
                "    public void v(String tag, String msg) {\n" +
                "    }\n" +
                "\n" +
                "    public void e(String tag, String msg) {\n" +
                "    }\n" +
                "\n" +
                "    public void key(String tag, String msg) {\n" +
                "    }\n" +
                "\n" +
                "    public void i(String tag, String msg) {\n" +
                "    }\n" +
                "}";
    }
}


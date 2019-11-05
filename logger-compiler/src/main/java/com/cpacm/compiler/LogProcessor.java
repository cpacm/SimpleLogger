package com.cpacm.compiler;

import com.cpacm.annotations.CLog;
import com.cpacm.annotations.MLog;
import com.cpacm.annotations.TLog;
import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.Pretty;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

/**
 * <p>
 * Deprecated 废弃
 * 生成类的功能正常，但修改AST后却无法反馈到编译中，故无法在源代码中插入代码
 * ps: 看log,明明已经在代码树节点中插入相应的代码了，但编译后却还是原来的代码，怀疑是gradle新版本的原因，
 * 毕竟apt本身也就只建议添加新类而不是修改源码。
 * ps: 换种方式，使用 gradle 插件的transform功能。
 *
 * @author cpacm 2019-10-25
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)//java版本支持
public class LogProcessor extends AbstractProcessor {

    private Trees trees;
    private TreeMaker make;
    private Name.Table names;
    private Context context;

    private Writer writer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        trees = Trees.instance(processingEnvironment);
        context = ((JavacProcessingEnvironment)
                processingEnvironment).getContext();
        make = TreeMaker.instance(context);
        names = Names.instance(context).table;//Name.Table.instance(context);


        try {
            JavaFileObject filerSourceFile = processingEnv.getFiler().createSourceFile("PrettyLog");
            writer = filerSourceFile.openWriter();
            Pretty pretty = new Pretty(writer, true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(TLog.class.getCanonicalName());
        annotations.add(CLog.class.getCanonicalName());
        annotations.add(MLog.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        //processTestClass(roundEnvironment);
        processGlobalLogClass(roundEnvironment);

        return false;
    }

    private void processGlobalLogClass(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(CLog.class);
        for (Element element : elements) {
            if (element.getKind() == ElementKind.CLASS) {
                Symbol.ClassSymbol symbol = (Symbol.ClassSymbol) element;
                String className = symbol.getSimpleName().toString();
                String fullName = symbol.getQualifiedName().toString();
                String packageName;
                if (className.equals(fullName)) {
                    packageName = "";
                } else {
                    packageName = fullName.substring(0, fullName.length() - className.length() - 1);
                }
                JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) trees.getTree(element);
                if (classDecl != null) {
                    try {
                        writer.write("//class in " + className + "\n");
                        writer.write("//class " + fullName + "\n");
                        writer.write("//class uri is  " + symbol.sourcefile.toUri() + "\n");
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    GlobalLogTranslator logTranslator = new GlobalLogTranslator(trees, make, names, context, className);
                    logTranslator.setWriter(writer);
                    classDecl.accept(logTranslator);
                }
            }
        }
    }
}

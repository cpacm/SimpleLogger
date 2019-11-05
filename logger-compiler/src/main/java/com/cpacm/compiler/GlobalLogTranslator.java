package com.cpacm.compiler;

import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.lang.model.type.TypeKind;

/**
 * <p>
 *
 * @author cpacm 2019-10-28
 */
public class GlobalLogTranslator extends TreeTranslator {

    private Trees trees;
    private TreeMaker make;
    private Name.Table names;
    private Context context;
    private String className;
    private Symtab syms;
    private AtomicInteger index = new AtomicInteger(0);

    private Writer writer;

    public GlobalLogTranslator(Trees trees, TreeMaker make, Name.Table names, Context context, String className) {
        this.trees = trees;
        this.make = make;
        this.names = names;
        this.context = context;
        this.className = className;
        syms = Symtab.instance(context);
    }


    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void visitAssert(JCTree.JCAssert tree) {
        super.visitAssert(tree);
        JCTree.JCStatement newNode = makeIfThrowException(tree);
        try {
            if (writer != null) {
                writer.write("//JCAssert:" + tree.toString() + "\n");
                writer.write("/**" + newNode.toString() + "**/\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        result = newNode;
    }

    private JCTree.JCStatement makeIfThrowException(JCTree.JCAssert node) {
        // make: if (!(condition) throw new AssertionError(detail);
        List<JCTree.JCExpression> args = node.getDetail() == null
                ? List.<JCTree.JCExpression>nil()
                : List.of(node.detail);
        JCTree.JCExpression expr = make.NewClass(
                null,
                null,
                make.Ident(names.fromString("AssertionError")),
                args,
                null);
        return make.If(
                make.Unary(JCTree.Tag.NOT, node.cond),
                make.Throw(expr),
                null);
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
        JCTree.JCBlock body = jcMethodDecl.getBody();
        if (body == null) {
            return;
        }
        List<JCTree.JCVariableDecl> parameters = jcMethodDecl.getParameters();
        String methodName = jcMethodDecl.getName().toString();
        StringBuilder sb = new StringBuilder();
        if (!parameters.isEmpty()) {
            for (JCTree.JCVariableDecl parameter : parameters) {
                if (sb.length() != 0) {
                    sb.append(",");
                }
                sb.append(parameter.getName().toString());
            }
        }
        String argList = sb.toString();

        ArrayList<JCTree.JCStatement> st = new ArrayList<JCTree.JCStatement>();
//        if (!parameters.isEmpty()) {
//            st.add(buildArgLog(methodName, argList, parameters));
//        } else {
//            st.add(buildEmptyArgLog(methodName));
//        }
        //JCTree.JCExpression returnType = (JCTree.JCExpression) jcMethodDecl.getReturnType();
        //JCTree.JCBlock newBody = processBlock(methodName, returnType, body);
        JCTree.JCExpressionStatement exec = make.Exec(
                make.Assign(
                        make.Ident(names.fromString("xiao")), make.Literal("assignment test")
                ));
        JCTree.JCVariableDecl var = make.VarDef(make.Modifiers(0), names.fromString("name"), memberAccess("java.lang.String"), make.Literal("methodName"));

        st.add(exec);
        st.add(var);
        st.addAll(body.getStatements());
        List<JCTree.JCStatement> of = List.from(st);
        JCTree.JCTry tryFinally = make.Try(make.Block(0, of), List.nil(), buildFinallyBlock(methodName));
        jcMethodDecl.body = make.Block(0, List.of(tryFinally));

        this.result = make.MethodDef(jcMethodDecl.sym, jcMethodDecl.body);
        //jcMethodDecl.body.stats = st;
        //super.visitMethodDef(jcMethodDecl);

        try {
            if (writer != null) {
                writer.write("// methodName:" + methodName + " \n");
                writer.write("/**" + this.result.toString() + "**/\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //result = jcMethodDecl;
    }

    private JCTree.JCExpression memberAccess(String components) {
        String[] componentArray = components.split("\\.");
        JCTree.JCExpression expr = make.Ident(names.fromString(componentArray[0]));
        for (int i = 1; i < componentArray.length; i++) {
            expr = make.Select(expr, names.fromString(componentArray[i]));
        }
        return expr;
    }

    private JCTree.JCBlock buildFinallyBlock(String methodName) {
        JCTree.JCFieldAccess access = buildContextHandler();
        access.setType(new Type.JCVoidType());
        JCTree.JCExpression[] array = new JCTree.JCExpression[2];
        array[0] = make.Literal("fuck");
        array[1] = make.Literal("cpacm");
        //array[2] = make.Literal("finally");
        //array[3] = make.Literal(index.incrementAndGet());
        //array[4] = make.Literal("finally");
        //array[5] = make.Literal("finally");
        List<JCTree.JCExpression> nil = List.from(array);
        JCTree.JCMethodInvocation app = make.App(access, nil);
        JCTree.JCExpressionStatement exec = make.Exec(app);
        return make.Block(0, List.of(exec));
    }

    private JCTree.JCBlock processBlock(String methodName, JCTree.JCExpression returnType, JCTree.JCBlock block) {
        if (block == null) {
            return null;
        }
        ArrayList<JCTree.JCStatement> st = new ArrayList<>();
        List<JCTree.JCStatement> statements = block.getStatements();
        for (JCTree.JCStatement statement : statements) {
            if (statement.getKind() == Tree.Kind.TRY) {
                JCTree.JCTry tryStatement = (JCTree.JCTry) statement;
                st.add(processTry(methodName, returnType, tryStatement));
            } else if (statement.getKind() == Tree.Kind.IF) {
                JCTree.JCIf ifStatement = (JCTree.JCIf) statement;
                st.add(processIf(methodName, returnType, ifStatement));
            } else if (statement.getKind() == Tree.Kind.FOR_LOOP) {
                JCTree.JCForLoop forStatement = (JCTree.JCForLoop) statement;
                st.add(processFor(methodName, returnType, forStatement));
            } else if (statement.getKind() == Tree.Kind.WHILE_LOOP) {
                JCTree.JCWhileLoop whileStatement = (JCTree.JCWhileLoop) statement;
                st.add(processWhile(methodName, returnType, whileStatement));
            } else if (statement.getKind() == Tree.Kind.DO_WHILE_LOOP) {
                JCTree.JCDoWhileLoop doWhileStatement = (JCTree.JCDoWhileLoop) statement;
                st.add(processDOWhile(methodName, returnType, doWhileStatement));
            } else if (statement.getKind() == Tree.Kind.BLOCK) {
                JCTree.JCBlock blockStatement = (JCTree.JCBlock) statement;
                st.add(processBlock(methodName, returnType, blockStatement));
            } else if (statement.getKind() == Tree.Kind.RETURN) {
                st.addAll(processRetrun(methodName, returnType, (JCTree.JCReturn) statement));
            } else if (statement.getKind() == Tree.Kind.VARIABLE) {
                st.addAll(processLocalVariable(methodName, (JCTree.JCVariableDecl) statement));
            } else {
                st.add(statement);
            }
        }
        List<JCTree.JCStatement> of = List.from(st);
        return make.Block(0, of);
    }

    private ArrayList<JCTree.JCStatement> processLocalVariable(String methodName, JCTree.JCVariableDecl statement) {
        ArrayList<JCTree.JCStatement> statements = new ArrayList<>();
        statements.add(statement);
//        if (DebugConfig.isProcessLocalVaribale() && statement.getInitializer() != null) {
//            statements.add(buildVariableLog(methodName, statement));
//        }
        return statements;
    }

    private JCTree.JCDoWhileLoop processDOWhile(String methodName, JCTree.JCExpression returnType, JCTree.JCDoWhileLoop doWhileStatement) {
        JCTree.JCStatement body = doWhileStatement.body;
        if (body != null && body instanceof JCTree.JCBlock) {
            JCTree.JCBlock jcBlock = processBlock(methodName, returnType, (JCTree.JCBlock) body);
            doWhileStatement.body = jcBlock;
        }
        return doWhileStatement;
    }

    private JCTree.JCWhileLoop processWhile(String methodName, JCTree.JCExpression returnType, JCTree.JCWhileLoop whileStatement) {
        JCTree.JCStatement body = whileStatement.body;
        if (body != null && body instanceof JCTree.JCBlock) {
            JCTree.JCBlock jcBlock = processBlock(methodName, returnType, (JCTree.JCBlock) body);
            whileStatement.body = jcBlock;
        }
        return whileStatement;
    }

    private JCTree.JCForLoop processFor(String methodName, JCTree.JCExpression returnType, JCTree.JCForLoop forStatement) {
        JCTree.JCStatement body = forStatement.body;
        if (body != null && body instanceof JCTree.JCBlock) {
            JCTree.JCBlock jcBlock = processBlock(methodName, returnType, (JCTree.JCBlock) body);
            forStatement.body = jcBlock;
        }
        return forStatement;
    }

    private JCTree.JCIf processIf(String methodName, JCTree.JCExpression returnType, JCTree.JCIf ifStatement) {
        JCTree.JCExpression condition = ifStatement.getCondition();
        JCTree.JCStatement elseStatement = ifStatement.getElseStatement();
        JCTree.JCStatement thenStatement = ifStatement.getThenStatement();
        if (thenStatement instanceof JCTree.JCBlock) {
            JCTree.JCBlock thenBlock = (JCTree.JCBlock) thenStatement;
            ArrayList<JCTree.JCStatement> st = new ArrayList<>();
            st.add(buildIfLog(methodName, condition.toString(), true));
            thenBlock = processBlock(methodName, returnType, thenBlock);
            st.addAll(thenBlock.stats);
            thenBlock.stats = List.from(st);
            thenStatement = thenBlock;
        } else if (thenStatement instanceof JCTree.JCReturn) {
            JCTree.JCReturn returnStatement = (JCTree.JCReturn) thenStatement;
            ArrayList<JCTree.JCStatement> jcStatements = processRetrun(methodName, returnType, returnStatement);
            jcStatements.add(0, buildIfLog(methodName, condition.toString(), true));
            thenStatement = make.Block(0, List.from(jcStatements));
        }


        if (elseStatement != null) {
            ArrayList<JCTree.JCStatement> elseList = new ArrayList<>();
            if (elseStatement instanceof JCTree.JCBlock) {
                elseList.add(buildIfLog(methodName, condition.toString(), false));
                JCTree.JCBlock elseBlock = (JCTree.JCBlock) elseStatement;
                elseBlock = processBlock(methodName, returnType, elseBlock);
                elseList.addAll(elseBlock.stats);
                elseBlock.stats = List.from(elseList);
                elseStatement = elseBlock;
            } else if (elseStatement instanceof JCTree.JCIf) {
                elseStatement = processIf(methodName, returnType, (JCTree.JCIf) elseStatement);
            } else if (elseStatement instanceof JCTree.JCReturn) {
                JCTree.JCReturn returnStatement = (JCTree.JCReturn) elseStatement;
                elseList.add(buildIfLog(methodName, condition.toString(), false));
                elseList.addAll(processRetrun(methodName, returnType, returnStatement));
                elseStatement = make.Block(0, List.from(elseList));
            } else {
                elseList.add(buildIfLog(methodName, condition.toString(), false));
                elseList.add(elseStatement);
                elseStatement = make.Block(0, List.from(elseList));
            }
        }
        return make.If(condition, thenStatement, elseStatement);
    }

    private JCTree.JCTry processTry(String methodName, JCTree.JCExpression returnType, JCTree.JCTry tryStatement) {
        tryStatement.body = processBlock(methodName, returnType, tryStatement.body);
        List<JCTree.JCCatch> catches = tryStatement.getCatches();
        if (catches != null) {
            for (JCTree.JCCatch jcCatch : catches) {
                JCTree.JCVariableDecl parameter = jcCatch.getParameter();
                JCTree type = parameter.getType();
                JCTree.JCStatement statement = buildCatchLog(methodName, type.toString(), parameter.getName());
                JCTree.JCBlock block = jcCatch.getBlock();
                JCTree.JCBlock jcBlock = processBlock(methodName, returnType, block);
                ArrayList<JCTree.JCStatement> list = new ArrayList<>();
                list.add(statement);
                list.addAll(jcBlock.getStatements());
                List<JCTree.JCStatement> of = List.from(list);
                jcCatch.body = make.Block(0, of);
            }
        }
        tryStatement.finalizer = processBlock(methodName, returnType, tryStatement.finalizer);
        return tryStatement;
    }

    private ArrayList<JCTree.JCStatement> processRetrun(String methodName, JCTree.JCExpression returnType, JCTree.JCReturn statement) {
        boolean add = true;
        ArrayList<JCTree.JCStatement> returnStatement = new ArrayList<>();
        if (returnType == null) {
            add = false;
        } else if (returnType instanceof JCTree.JCPrimitiveTypeTree) {
            JCTree.JCPrimitiveTypeTree primitiveType = (JCTree.JCPrimitiveTypeTree) returnType;
            if (primitiveType.getPrimitiveTypeKind() == TypeKind.VOID) {
                add = false;
            }
        }
        JCTree.JCReturn returnStetment = statement;

        if (add) {
            if (returnStetment.getExpression().getKind() != Tree.Kind.IDENTIFIER) {
                JCTree.JCVariableDecl jcVariableDecl = buildLocalVar(returnStetment.getExpression(), returnType);
                returnStatement.add(jcVariableDecl);
                JCTree.JCReturn jcReturn = buildReturn(jcVariableDecl);
                returnStatement.add(buildReturnLog(methodName, "return", jcReturn, returnType));
                returnStatement.add(jcReturn);
            } else {
                returnStatement.add(buildReturnLog(methodName, "return", returnStetment, returnType));
                returnStatement.add(returnStetment);
            }
        } else {
            returnStatement.add(returnStetment);
        }
        return returnStatement;
    }

    private JCTree.JCReturn buildReturn(JCTree.JCVariableDecl jcVariableDecl) {
        return make.Return(make.Ident(jcVariableDecl.getName()));
    }

    private JCTree.JCStatement buildCatchLog(String methodName, String argList, Name name) {
        JCTree.JCFieldAccess access = buildContextHandler();
        access.setType(new Type.JCVoidType());
        JCTree.JCExpression[] array = new JCTree.JCExpression[6];
        array[0] = make.Literal(className);
        array[1] = make.Literal(methodName);
        array[2] = make.Literal("catch");
        array[3] = make.Literal(index.incrementAndGet());

        array[4] = make.Literal(argList);
        array[5] = make.Ident(name);
        List<JCTree.JCExpression> nil = List.from(array);
        JCTree.JCMethodInvocation app = make.App(access, nil);
        JCTree.JCExpressionStatement exec = make.Exec(app);
        return exec;
    }

    private JCTree.JCStatement buildVariableLog(String methodName, JCTree.JCVariableDecl variable) {
        JCTree.JCFieldAccess access = buildContextHandler();
        access.setType(new Type.JCVoidType());
        JCTree.JCExpression[] array = new JCTree.JCExpression[6];
        array[0] = make.Literal(className);
        array[1] = make.Literal(methodName);
        array[2] = make.Literal("localVariable");
        array[3] = make.Literal(index.incrementAndGet());

        array[4] = make.Literal(variable.getName().toString());
        array[5] = make.Ident(variable.getName());
        List<JCTree.JCExpression> nil = List.from(array);
        JCTree.JCMethodInvocation app = make.App(access, nil);
        JCTree.JCExpressionStatement exec = make.Exec(app);
        return exec;
    }

    private JCTree.JCStatement buildReturnLog(String methodName, String argList, JCTree.JCReturn statement, JCTree.JCExpression returnType) {
        JCTree.JCFieldAccess access = buildContextHandler();
        access.setType(new Type.JCVoidType());
        JCTree.JCExpression[] array = new JCTree.JCExpression[6];
        array[0] = make.Literal(className);
        array[1] = make.Literal(methodName);
        array[2] = make.Literal("return");
        array[3] = make.Literal(index.incrementAndGet());

        array[4] = make.Literal(argList);
        array[5] = statement.getExpression();
        List<JCTree.JCExpression> nil = List.from(array);
        JCTree.JCMethodInvocation app = make.App(access, nil);
        JCTree.JCExpressionStatement exec = make.Exec(app);
        return exec;
    }

    private JCTree.JCVariableDecl buildLocalVar(JCTree.JCExpression statement, JCTree.JCExpression returnType) {
        return make.VarDef(make.Modifiers(Flags.FINAL), names.fromString("local" + UUID.randomUUID().toString().replaceAll("-", "")), returnType, statement);
    }

    private void show(JCTree tree) {
        if (tree == null) {
            System.out.println("null");
            return;
        }
        System.out.println(tree.getKind() + ":" + tree.getClass() + ":" + tree);
    }

    private JCTree.JCStatement buildIfLog(String methodName, String argList, boolean express) {
        JCTree.JCFieldAccess access = buildContextHandler();
        access.setType(new Type.JCVoidType());
        JCTree.JCExpression[] array = new JCTree.JCExpression[6];
        array[0] = make.Literal(className);
        array[1] = make.Literal(methodName);
        array[2] = make.Literal("if");
        array[3] = make.Literal(index.incrementAndGet());
        array[4] = make.Literal(argList);
        array[5] = make.Literal(express);
        List<JCTree.JCExpression> nil = List.from(array);
        JCTree.JCMethodInvocation app = make.App(access, nil);
        JCTree.JCExpressionStatement exec = make.Exec(app);
        return exec;
    }

    private JCTree.JCStatement buildEmptyArgLog(String methodName) {
        JCTree.JCFieldAccess access = buildContextHandler();
        access.setType(new Type.JCVoidType());
        JCTree.JCExpression[] array = new JCTree.JCExpression[6];
        array[0] = make.Literal(className);
        array[1] = make.Literal(methodName);
        array[2] = make.Literal("arg");
        array[3] = make.Literal(index.incrementAndGet());
        array[4] = make.Literal("noArgInvoke");
        array[5] = make.Literal("");
        List<JCTree.JCExpression> nil = List.from(array);
        JCTree.JCMethodInvocation app = make.App(access, nil);
        JCTree.JCExpressionStatement exec = make.Exec(app);
        return exec;
    }

    private JCTree.JCStatement buildArgLog(String methodName, String argList, List<JCTree.JCVariableDecl> parameters) {
        JCTree.JCFieldAccess access = buildContextHandler();
        access.setType(new Type.JCVoidType());
        JCTree.JCExpression[] array = new JCTree.JCExpression[parameters.size() + 5];
        array[0] = make.Literal(className);
        array[1] = make.Literal(methodName);
        array[2] = make.Literal("arg");
        array[3] = make.Literal(index.incrementAndGet());
        array[4] = make.Literal(argList);

        int i = 5;
        for (JCTree.JCVariableDecl arg : parameters) {
            array[i] = make.Ident(arg.getName());
            i++;
        }
        List<JCTree.JCExpression> nil = List.from(array);
        JCTree.JCMethodInvocation app = make.App(access, nil);
        JCTree.JCExpressionStatement exec = make.Exec(app);
        return exec;
    }

    private JCTree.JCFieldAccess buildContextHandler() {
        JCTree.JCIdent ident = make.Ident(names.fromString("android"));
        JCTree.JCFieldAccess jcFieldAccess = make.Select(ident, names.fromString("util"));
        jcFieldAccess = make.Select(jcFieldAccess, names.fromString("Logger"));
        jcFieldAccess = make.Select(jcFieldAccess, names.fromString("d"));
        //jcFieldAccess = make.Select(jcFieldAccess, names.fromString("touch"));
        return jcFieldAccess;
    }

}

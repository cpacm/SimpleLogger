package com.cpacm.log.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * <p>
 *
 * @author cpacm 2019-10-30
 */
public class AsmMethodVisitor extends AdviceAdapter {

    private String methodName;
    private String methodDesc;

    /**
     * Creates a new {@link AdviceAdapter}.
     *
     * @param api    the ASM API version implemented by this visitor. Must be one
     *               of {@link Opcodes#ASM4}, {@link Opcodes#ASM5} or {@link Opcodes#ASM6}.
     * @param mv     the method visitor to which this adapter delegates calls.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param name   the method's name.
     * @param desc   the method's descriptor (see {@link Type Type}).
     */
    protected AsmMethodVisitor(int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc);
        this.methodName = name;
        this.methodDesc = desc;
    }

    @Override
    protected void onMethodEnter() {
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (this.methodName.toLowerCase().contains("test")) {
            mv.visitLdcInsn("cpacm");
            mv.visitLdcInsn("asm");
            mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);
            mv.visitInsn(POP);
        }
    }

    /**
     * 会检索类上的注解
     */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        System.out.println("AsmMethodVisitor:  " + methodName + desc + visible);
        return super.visitAnnotation(desc, visible);
    }
}

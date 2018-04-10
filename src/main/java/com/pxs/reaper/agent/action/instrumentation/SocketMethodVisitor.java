package com.pxs.reaper.agent.action.instrumentation;

import org.objectweb.asm.*;

@SuppressWarnings("unused")
public class SocketMethodVisitor extends MethodVisitor {

    @SuppressWarnings("FieldCanBeLocal")
    private final String collectorMethodName = "capture";

    private final Type methodParameterType = Type.getType(Object.class);
    private final String collectorClassName = Type.getInternalName(NetworkTrafficCollector.class);
    private final String collectorMethodDescription = Type.getMethodDescriptor(Type.VOID_TYPE, methodParameterType);

    private String methodName;
    private String methodDescription;

    SocketMethodVisitor(final MethodVisitor methodVisitor, final String methodName, final String methodDescription) {
        super(Opcodes.ASM5, methodVisitor);
        this.methodName = methodName;
        this.methodDescription = methodDescription;
    }

    @Override
    public void visitParameter(String name, int access) {
        System.out.println("visitParameter : " + name + ":" + access);
        super.visitParameter(name, access);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.ARETURN) {
            // This is the return code, so pop the return value on the stack again
            insertInstruction(collectorClassName, collectorMethodName, collectorMethodDescription);
            System.out.println("        visitInsn : " + opcode);
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        // System.out.println("visitVarInsn : " + opcode + ":" + var);
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        // System.out.println("visitTypeInsn : " + opcode + ":" + type);
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        // System.out.println("visitMethodInsn : " + opcode + ":" + owner + ":" + name + ":" + desc + ":" + itf);
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        // System.out.println("visitInvokeDynamicInsn : " + name + ":" + desc + ":" + bsm + ":" + Arrays.toString(bsmArgs));
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        // System.out.println("visitJumpInsn : " + opcode + ":" + label);
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        // System.out.println("visitTryCatchBlock : " + start + ":" + end);
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        // System.out.println("visitLocalVariable : " + name + ":" + desc);
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    private void insertInstruction(final String collectorClassName, final String collectorMethodName, final String collectorMethodDescription) {
        // mv.visitInsn(Opcodes.DUP); // Return object
        // mv.visitMethodInsn(Opcodes.INVOKESTATIC, "abc/xyz/CatchError", "getReturnObject", "(Ljava/lang/Object)V", false);

        // super.visitInsn(Opcodes.DUP); // Return object?
        // super.visitMethodInsn(Opcodes.INVOKESTATIC, collectorClassName, collectorMethodName, collectorMethodDescription, Boolean.FALSE);

        super.visitInsn(Opcodes.DUP); // Return object?
        super.visitMethodInsn(Opcodes.INVOKESTATIC, collectorClassName, collectorMethodName, collectorMethodDescription, Boolean.FALSE);

        super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", Type.getObjectType("java/io/PrintStream").getDescriptor());
        super.visitLdcInsn("Invoking : " + methodName + " : " + methodDescription);
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

}

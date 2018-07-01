package com.pxs.reaper.agent.action.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@SuppressWarnings("WeakerAccess")
public class SocketStreamClassVisitor extends AClassVisitor /* ClassVisitor, ClassNode */ {

    SocketStreamClassVisitor(final ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(
            final int access,
            final String methodName,
            final String methodDescription,
            final String methodSignature,
            final String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, methodName, methodDescription, methodSignature, exceptions);
        if (methodName.equals("socketRead") || methodName.equals("socketWrite")) {
            if (isAbstract(access)) {
                return methodVisitor;
            }
            System.out.println("   Method name : " + methodName + " : " + methodDescription);
            return new SocketStreamMethodVisitor(methodVisitor, methodName, methodDescription);
        }
        return methodVisitor;
    }

}
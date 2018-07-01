package com.pxs.reaper.agent.action.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@SuppressWarnings("WeakerAccess")
public class SocketDispatcherClassVisitor extends AClassVisitor /* ClassVisitor, ClassNode */ {

    SocketDispatcherClassVisitor(final ClassVisitor classVisitor) {
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
        if (methodName.startsWith("read") || methodName.startsWith("write")) {
            if (isAbstract(access)) {
                return methodVisitor;
            }
            System.out.println("   Method name : " + methodName + " : " + methodDescription);
            return new SocketDispatcherMethodVisitor(methodVisitor, methodName, methodDescription);
        }
        return methodVisitor;
    }


}
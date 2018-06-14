package com.pxs.reaper.agent.action.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@SuppressWarnings("WeakerAccess")
public class SocketClassVisitor extends ClassVisitor /* ClassVisitor, ClassNode */ {

    SocketClassVisitor(final ClassVisitor classVisitor) {
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
        if (!methodName.equals("socketRead") && !methodName.equals("socketWrite")) {
            return methodVisitor;
        }
        System.out.println("        Method name : " + methodName + " : " + methodDescription);
        // We test for interfaces and abstract classes, of course these methods do
        // not have bodies so we can't add instructions to these methods or the Jvm
        // will not like it, class format exceptions
        switch (access) {
            case Opcodes.ACC_ABSTRACT:
            case Opcodes.ACC_ABSTRACT + Opcodes.ACC_PUBLIC:
            case Opcodes.ACC_ABSTRACT + Opcodes.ACC_PRIVATE:
            case Opcodes.ACC_ABSTRACT + Opcodes.ACC_PROTECTED:
            case Opcodes.ACC_INTERFACE:
            case Opcodes.ACC_INTERFACE + Opcodes.ACC_PUBLIC:
            case Opcodes.ACC_INTERFACE + Opcodes.ACC_PRIVATE:
            case Opcodes.ACC_INTERFACE + Opcodes.ACC_PROTECTED:
                return methodVisitor;
            default:
                return new SocketMethodVisitor(methodVisitor, methodName, methodDescription);
        }
    }

}
package com.pxs.reaper.agent.action.instrumentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.net.Socket;

/**
 * Note to self: The base classes from java cannot be modified by adding fields/method, i.e. the schema of the class can not
 * be changed dynamically, for several reasons, not the least of which is that there is a protection domain error, then a schema
 * re-definition error.
 */
@SuppressWarnings({"unused", "WeakerAccess", "FieldCanBeLocal"})
public class SocketMethodVisitor extends MethodVisitor {

    private final Type integer = Type.getType(int.class);
    private final Type bytes = Type.getType(byte[].class);
    private final Type socket = Type.getType(Socket.class);

    private final String collectorClassName = Type.getInternalName(NetworkTrafficCollector.class);

    private final String collectOutputTraffic = "collectOutputTraffic";
    private final String collectInputTraffic = "collectInputTraffic";

    private final String collectorOutputMethodDescription = Type.getMethodDescriptor(Type.VOID_TYPE, socket, integer);
    private final String collectorInputMethodDescription = Type.getMethodDescriptor(Type.VOID_TYPE, socket, bytes, integer, integer);

    private String methodName;
    private String methodDescription;

    SocketMethodVisitor(final MethodVisitor methodVisitor, final String methodName, final String methodDescription) {
        super(Opcodes.ASM5, methodVisitor);
        this.methodName = methodName;
        this.methodDescription = methodDescription;
    }

    @Override
    public void visitInsn(int opcode) {
        // System.out.println("        visitInsn : " + opcode);
        if (methodName.equals("socketRead")) {
            insertInputCollectInstructions(collectorClassName, collectInputTraffic, collectorInputMethodDescription);
        } else if (methodName.equals("socketWrite") && opcode == Opcodes.IADD) {
            // First instruction for output
            insertOutputCollectInstructions(collectorClassName, collectOutputTraffic, collectorOutputMethodDescription);
        }
        super.visitInsn(opcode);
    }

    private void insertOutputCollectInstructions(final String collectorClassName, final String collectorMethodName, final String collectorMethodDescription) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, "java/net/SocketOutputStream", "socket", "Ljava/net/Socket;");
        mv.visitVarInsn(Opcodes.ILOAD, 3);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, collectorClassName, collectorMethodName, collectorMethodDescription, false);
    }

    private void insertInputCollectInstructions(final String collectorClassName, final String collectorMethodName, final String collectorMethodDescription) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, "java/net/SocketInputStream", "socket", "Ljava/net/Socket;");
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitVarInsn(Opcodes.ILOAD, 3);
        mv.visitVarInsn(Opcodes.ILOAD, 4);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, collectorClassName, collectorMethodName, collectorMethodDescription, false);
    }

}
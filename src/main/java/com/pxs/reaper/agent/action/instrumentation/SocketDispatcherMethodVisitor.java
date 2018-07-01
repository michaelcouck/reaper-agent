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
public class SocketDispatcherMethodVisitor extends MethodVisitor {

    private final Type integer = Type.getType(int.class);
    private final Type bytes = Type.getType(byte[].class);
    private final Type socket = Type.getType(Socket.class);
    private final String collectorClassName = Type.getInternalName(NetworkTrafficCollector.class);
    private final String collectorMethodDescription = Type.getMethodDescriptor(Type.VOID_TYPE, socket, integer);

    private final String methodName;
    private final String methodDescription;
    private final String collectTraffic = "collectTraffic";

    SocketDispatcherMethodVisitor(final MethodVisitor methodVisitor, final String methodName, final String methodDescription) {
        super(Opcodes.ASM5, methodVisitor);
        this.methodName = methodName;
        this.methodDescription = methodDescription;
    }

    @Override
    public void visitInsn(int opcode) {
        if (methodName.startsWith("read")) {
            insertInputCollectInstructions(collectorClassName, collectTraffic, collectorMethodDescription);
        } else if (methodName.startsWith("write") && opcode == Opcodes.IADD) {
            insertOutputCollectInstructions(collectorClassName, collectTraffic, collectorMethodDescription);
        }
        super.visitInsn(opcode);
    }

    private void insertInputCollectInstructions(final String collectorClassName, final String collectorMethodName, final String collectorMethodDescription) {
        // TODO: Add a method to NetworkTrafficCollector
        // TODO: Add instructions for the file descriptor and the length to the traffic collector
    }

    private void insertOutputCollectInstructions(final String collectorClassName, final String collectorMethodName, final String collectorMethodDescription) {
        // TODO: Add a method to NetworkTrafficCollector
        // TODO: Add instructions for the file descriptor and the length to the traffic collector
    }

}
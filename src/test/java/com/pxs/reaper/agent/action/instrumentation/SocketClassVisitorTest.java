package com.pxs.reaper.agent.action.instrumentation;

import com.pxs.reaper.agent.toolkit.FILE;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Logger;

public class SocketClassVisitorTest {

    @SuppressWarnings("unused")
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @SuppressWarnings("ConstantConditions")
    @Test
    public void getClassVisitor() throws IOException {
        Class clazz = Socket.class;
        String className = clazz.getName();
        String classAsPath = className.replace('.', '/') + ".class";
        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(classAsPath);
        byte[] classFileBytes = IOUtils.toByteArray(stream);
        logger.info("Class byte size : " + classFileBytes.length);
        ClassWriter classWriter = VisitorFactory.getClassVisitor(classFileBytes);
        byte[] redefinedClassFileBytes = classWriter.toByteArray();
        logger.info("Redefined class byte size : " + redefinedClassFileBytes.length);

        File file = FILE.findFileRecursively(new File("."), "Socket.class");
        IOUtils.write(redefinedClassFileBytes, new FileOutputStream(file));

        // TODO: Check the byte code with the reader
        ClassReader classReader = new ClassReader(redefinedClassFileBytes);
        ClassVisitor classVisitor = new ClassVisitorVerifier();
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
    }

    @SuppressWarnings("WeakerAccess")
    class ClassVisitorVerifier extends ClassVisitor /* ClassNode */ {

        public ClassVisitorVerifier() {
            super(Opcodes.ASM5);
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
            return new MethodVisitorVerifier(methodVisitor);
        }

    }

    @SuppressWarnings("WeakerAccess")
    class MethodVisitorVerifier extends MethodVisitor {

        public MethodVisitorVerifier(final MethodVisitor methodVisitor) {
            super(Opcodes.ASM5, methodVisitor);
        }

        @Override
        public void visitInsn(int opcode) {
            System.out.println("Method instruction : " + opcode);
            super.visitInsn(opcode);
        }
    }

    @Test
    public void redefineClass() {
        System.out.println(Type.getInternalName(NetworkTrafficCollector.class));
        // Attach to the local jvm
        // Open a socket to some where
        // Check that the network traffic collector is called
    }

}

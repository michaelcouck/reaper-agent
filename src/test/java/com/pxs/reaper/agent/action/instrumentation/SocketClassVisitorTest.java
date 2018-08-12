package com.pxs.reaper.agent.action.instrumentation;

import com.pxs.reaper.agent.toolkit.FILE;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class SocketClassVisitorTest {

    @SuppressWarnings("unused")
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Test
    public void getClassVisitor() throws IOException {
        Class clazz = Socket.class;
        String className = clazz.getName();
        String classAsPath = className.replace('.', '/') + ".class";
        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(classAsPath);
        byte[] classFileBytes = IOUtils.toByteArray(stream);
        logger.info("Class byte size : " + classFileBytes.length);
        ClassWriter classWriter = VisitorFactory.getSocketStreamClassVisitor(classFileBytes);
        byte[] redefinedClassFileBytes = classWriter.toByteArray();
        logger.info("Redefined class byte size : " + redefinedClassFileBytes.length);

        File file = FILE.getOrCreateFile(new File("./target", "Socket.class"));
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
            super.visitInsn(opcode);
        }
    }

    @Test
    public void redefineClass() {
        // System.out.println(Type.getInternalName(NetworkTrafficCollector.class));
        // Attach to the local jvm
        // Open a socket to some where
        // Check that the network traffic collector is called
    }

    @Test
    @Ignore
    public void inspectByteCode() throws Exception {
        File file = FILE.findFileRecursively(new File("."), SocketOutputStream.class.getSimpleName() + ".class");
        assert file != null;
        ASMifier.main(new String[]{FILE.cleanFilePath(file.getAbsolutePath())});

        String className = "java.net.SocketOutputStream";
        String classAsPath = className.replace('.', '/') + ".class";
        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(classAsPath);
        byte[] classFileBytes = IOUtils.toByteArray(stream);

        ClassReader reader = new ClassReader(classFileBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM5, writer) {
        };
        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(visitor, new PrintWriter(System.out));
        reader.accept(traceClassVisitor, ClassReader.EXPAND_FRAMES);
    }

}

class SocketOutputStream extends ByteArrayOutputStream {

    private Socket socket;

    SocketOutputStream() throws IOException {
        socket = new Socket("ikube.be", 80);
    }

    public void socketWrite(final byte[] b, final int off, final int len) {
        NetworkTrafficCollector.collectTraffic(socket, len);
    }
}

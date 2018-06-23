package com.pxs.reaper.agent.action.instrumentation;

import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;
import java.util.TreeSet;

public class SocketClassFileTransformer implements ClassFileTransformer {

    private static final Set<String> redefinedClasses = new TreeSet<>();

    @Override
    public byte[] transform(
            final ClassLoader loader,
            final String className,
            final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain,
            final byte[] classfileBuffer) throws IllegalClassFormatException {
        synchronized (redefinedClasses) {
            // TODO: Need to intercept the nio classes, including but probably not limited to NativeDispatcher/SocketDispatcher
            if ((className.equals("java/net/SocketInputStream") || className.equals("java/net/SocketOutputStream"))
                    && !redefinedClasses.contains(className)) {
                System.out.println("   Transforming class : " + className + ", with loader : " + loader);
                redefinedClasses.add(className);
                ClassWriter classWriter = VisitorFactory.getClassVisitor(classfileBuffer);
                return classWriter.toByteArray();
            }
        }
        return classfileBuffer;
    }

}
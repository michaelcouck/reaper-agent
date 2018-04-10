package com.pxs.reaper.agent.action.instrumentation;

import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class SocketClassFileTransformer implements ClassFileTransformer {

    /*private Set<Class<?>> redefinedClasses = new TreeSet<>();*/

    @Override
    public byte[] transform(
            final ClassLoader loader,
            final String className,
            final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain,
            final byte[] classfileBuffer) throws IllegalClassFormatException {
        /*System.out.println(
                "        Is socket : " + Socket.class.isAssignableFrom(classBeingRedefined) +  "\n" +
                "        Is URL connection : " + URLConnection.class.isAssignableFrom(classBeingRedefined) + "\n" +
                "        Is already re-defined : " + redefinedClasses.contains(classBeingRedefined));*/
        if (className.equals("java/net/Socket") || className.equals("java/net/SocketImpl")) {
            System.out.println("        Transformer : " + loader + ":" + className);
            ClassWriter classWriter = VisitorFactory.getClassVisitor(classfileBuffer);
            return classWriter.toByteArray();
        }
        /*if (!redefinedClasses.contains(classBeingRedefined)) {
            redefinedClasses.add(classBeingRedefined);
        }*/
        return classfileBuffer;
    }

}
package com.pxs.reaper.agent.action.instrumentation;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * This class instantiates visitors for classes, methods, field, signature and annotations.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 07-04-2018
 */
@SuppressWarnings("WeakerAccess")
public class VisitorFactory {

    /**
     * ...
     *
     * @param classBytes the byte array of the byte code
     * @return the class visitor/writer
     */
    public static ClassWriter getClassVisitor(final byte[] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = new SocketClassVisitor(writer);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return writer;
    }

}
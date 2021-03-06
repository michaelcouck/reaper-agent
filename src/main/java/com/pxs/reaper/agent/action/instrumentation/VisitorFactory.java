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
     * Convenience method to instantiate the visitors in their particular order to visit the byte code in the target class
     * tha potentially to add instructions, particularly to the stream classes that write out of the machine, intercepting traffic and
     * collecting the network usage of the components.
     *
     * @param classBytes the byte array of the byte code
     * @return the class visitor/writer
     */
    public static ClassWriter getSocketStreamClassVisitor(final byte[] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = new SocketStreamClassVisitor(writer);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return writer;
    }

    /**
     * Convenience method to instantiate the visitors in their particular order to visit the byte code in the target class
     * tha potentially to add instructions, particularly to the stream classes that write out of the machine, intercepting traffic and
     * collecting the network usage of the components.
     *
     * @param classBytes the byte array of the byte code
     * @return the class visitor/writer
     */
    public static ClassWriter getSocketDispatcherClassVisitor(final byte[] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = new SocketDispatcherClassVisitor(writer);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return writer;
    }

}
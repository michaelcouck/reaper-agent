package com.pxs.reaper.agent.action.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

abstract class AClassVisitor extends ClassVisitor {

    AClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    boolean isAbstract(final int access) {
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
                return Boolean.TRUE;
            default:
                return Boolean.FALSE;
        }
    }

}

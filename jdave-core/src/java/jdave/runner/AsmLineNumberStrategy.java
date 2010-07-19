/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdave.runner;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.asm.AnnotationVisitor;
import net.sf.cglib.asm.Attribute;
import net.sf.cglib.asm.ClassReader;
import net.sf.cglib.asm.ClassVisitor;
import net.sf.cglib.asm.FieldVisitor;
import net.sf.cglib.asm.MethodVisitor;
import net.sf.cglib.asm.Label;

/**
 * @author Esko Luontola
 * @since 14.2.2008
 */
public class AsmLineNumberStrategy implements LineNumberStrategy {

    private final Map<Class<?>, LineNumberClassVisitor> cache = new HashMap<Class<?>, LineNumberClassVisitor>();

    public int firstLineNumber(Class<?> clazz, int defaultValue) {
        int line = analyze(clazz).firstClassLineNumber();
        return line < Integer.MAX_VALUE ? line : defaultValue;
    }

    public int firstLineNumber(Method method, int defaultValue) {
        Integer line = analyze(method.getDeclaringClass()).firstMethodLineNumber(method);
        return line != null ? line : defaultValue;
    }

    private LineNumberClassVisitor analyze(Class<?> clazz) {
        LineNumberClassVisitor visitor = cache.get(clazz);
        if (visitor != null) {
            return visitor;
        }
        try {
            InputStream classAsStream = toStream(clazz);
            ClassReader reader = new ClassReader(classAsStream);
            visitor = new LineNumberClassVisitor();
            reader.accept(visitor, 0);
            classAsStream.close();
            cache.put(clazz, visitor);
            return visitor;
        } catch (IOException e) {
            throw new RuntimeException("Error reading class: " + clazz, e);
        }
    }

    private static InputStream toStream(Class<?> clazz) {
        // WORKAROUND: org.objectweb.asm.ClassReader(java.lang.String) has a bug which causes it to
        // not find the class file in some situations
        String name = clazz.getName();
        int i = name.lastIndexOf('.');
        if (i > 0) {
            name = name.substring(i + 1);
        }
        return clazz.getResourceAsStream(name + ".class");
    }

    private static class LineNumberClassVisitor extends NullClassVisitor {

        private final LineNumberCodeVisitor codeVisitor = new LineNumberCodeVisitor(this);
        private final Map<String, Integer> methodLines = new HashMap<String, Integer>();
        private int minLine = Integer.MAX_VALUE;
        private String nextMethod;

        @Override
        public MethodVisitor visitMethod(int access, String name, String arg2, String arg3,
                String[] exceptions) {
            nextMethod = name;
            return codeVisitor;
        }

        public void visitLineNumber(int line) {
            if (nextMethod != null) {
                minLine = Math.min(minLine, line);
                methodLines.put(nextMethod, line);
                nextMethod = null;
            }
        }

        public Integer firstMethodLineNumber(Method method) {
            // TODO: does not handle overloaded methods properly - ignores method parameters
            return methodLines.get(method.getName());
        }

        public int firstClassLineNumber() {
            return minLine;
        }
    }

    private static class LineNumberCodeVisitor extends NullCodeVisitor {

        private final LineNumberClassVisitor classVisitor;

        public LineNumberCodeVisitor(LineNumberClassVisitor classVisitor) {
            this.classVisitor = classVisitor;
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            classVisitor.visitLineNumber(line);
        }
    }

    private static class NullClassVisitor implements ClassVisitor {

        public void visitInnerClass(String name, String outerName, String innerName, int access) {
        }

        public void visitAttribute(Attribute attr) {
        }

        public void visitEnd() {
        }

        public void visit(int arg0, int arg1, String arg2, String arg3, String arg4, String[] arg5) {
        }

        public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
            return null;
        }

        public FieldVisitor visitField(int arg0, String arg1, String arg2, String arg3, Object arg4) {
            return null;
        }

        public MethodVisitor visitMethod(int arg0, String arg1, String arg2, String arg3,
                String[] arg4) {
            return null;
        }

        public void visitOuterClass(String arg0, String arg1, String arg2) {
        }

        public void visitSource(String arg0, String arg1) {
        }
    }

    private static class NullCodeVisitor implements MethodVisitor {

        public void visitInsn(int opcode) {
        }

        public void visitIntInsn(int opcode, int operand) {
        }

        public void visitVarInsn(int opcode, int var) {
        }

        public void visitTypeInsn(int opcode, String desc) {
        }

        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        }

        public void visitJumpInsn(int opcode, Label label) {
        }

        public void visitLabel(Label label) {
        }

        public void visitLdcInsn(Object cst) {
        }

        public void visitIincInsn(int var, int increment) {
        }

        public void visitTableSwitchInsn(int min, int max, Label dflt, Label labels[]) {
        }

        public void visitLookupSwitchInsn(Label dflt, int keys[], Label labels[]) {
        }

        public void visitMultiANewArrayInsn(String desc, int dims) {
        }

        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        }

        public void visitMaxs(int maxStack, int maxLocals) {
        }

        public void visitLineNumber(int line, Label start) {
        }

        public void visitAttribute(Attribute attr) {
        }

        public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
            return null;
        }

        public AnnotationVisitor visitAnnotationDefault() {
            return null;
        }

        public void visitCode() {
        }

        public void visitEnd() {
        }

        public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3, Object[] arg4) {
        }

        public void visitLocalVariable(String arg0, String arg1, String arg2, Label arg3,
                Label arg4, int arg5) {
        }

        public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
            return null;
        }
    }
}

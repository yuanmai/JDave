/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdave.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;

import jdave.Group;

/**
 * @author Joni Freeman
 */
public abstract class AnnotatedSpecScanner {
    private Scanner scanner;

    public AnnotatedSpecScanner(String path) {
        scanner = new Scanner(path);
    }

    public void forEach(final IAnnotatedSpecHandler annotatedSpecHandler) {
        scanner.forEach("class", new IFileHandler() {
            public void handle(final File file) {
                Class<?> clazz;
                try {
                    clazz = loadClass(file);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("File not found: " + file);
                }
                Group groupAnnotation = clazz.getAnnotation(Group.class);
                String[] groups = groupAnnotation != null ? groupAnnotation.value() : new String[0];
                if (groups.length == 0) {
                    if (isInDefaultGroup(clazz.getName(), clazz.getAnnotations())) {
                        groups = new String[] { Groups.DEFAULT };
                    }
                }
                if (groups.length > 0) {
                    annotatedSpecHandler.handle(clazz.getName(), groups);
                }
            }

            private Class<?> loadClass(File file) throws FileNotFoundException {
                try {
                    String cleanedPath = cleanup(file.getPath());
                    return Class.forName(cleanedPath);
                } catch (Throwable t) {
                    String path = file.getPath();
                    int idx = path.indexOf(File.separatorChar);
                    if (idx == -1) {
                        throw new FileNotFoundException();
                    }
                    path = path.substring(idx + 1);
                    return loadClass(new File(path));
                }
            }

            private String cleanup(String path) {
                return path.replace(".class", "").replace(File.separatorChar, '.');
            }
        });
    }
    
    public abstract boolean isInDefaultGroup(String classname, Annotation... annotations);
}

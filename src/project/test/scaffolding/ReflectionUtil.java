package project.test.scaffolding;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReflectionUtil {
    public static Set<Class<?>> classes(String packageName) {
        final String resourceName = packageName.replace('.', '/');
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final URL root = classLoader.getResource(resourceName);
        
        if(root == null) {
            return new Set<>();
        }

        final Stream<String> stream = switch (root.getProtocol()) {
            case "file" -> {
                File[] files = new File(root.getFile()).listFiles((dir, name) -> name.endsWith(".class"));
                yield Arrays.stream(files).map(f -> packageName + "." + f.getName());
            }
            case "jar" -> {
                try {
                    final var jar = ((JarURLConnection) root.openConnection()).getJarFile();
                    yield Collections.list(jar.entries()).stream().map(e -> e.getName())
                            .filter(n -> n.startsWith(resourceName));
                } catch (Exception e) {
                    yield Stream.empty();
                }
            }
            default -> Stream.empty();
        };
        return classesFrom(stream);
    }

    public static Set<Class<?>> allClasses(String packageName) {
        final String resourceName = packageName.replace('.', '/');
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final URL root = classLoader.getResource(resourceName);

        if ("file".equals(root.getProtocol())) {
            final var files = visitAllFiles(new File(root.getFile()), null, new FileVisitor<String>() {
                private String state;

                @Override
                public boolean visit(String state, File file) {
                    final var filename = file.getName();
                    this.state = state == null ? "" : (state + "." + filename);
                    return file.isFile();
                }

                @Override
                public String state() {
                    return this.state;
                }
            }).filter(f -> f.endsWith(".class")).map(f -> packageName + f);

            return classesFrom(files);
        }
        return new HashSet<>();
    }

    public static <T> T unchecked(Callable<T> supplier) {
        try {
            return supplier.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<Class<?>> classesFrom(Stream<String> names) {
        return names
                .map(name -> name.replaceAll(".class$", ""))
                .map(fullName -> unchecked(() -> Class.forName(fullName)))
                .collect(Collectors.toSet());
    }

    private static <T> Stream<T> visitAllFiles(File file, T state, FileVisitor<T> visitor) {
        final var terminal = visitor.visit(state, file);
        final var s = visitor.state();
        if (terminal) {
            return Stream.of(s);
        }
        return Arrays.stream(file.listFiles()).flatMap(f -> visitAllFiles(f, s, visitor));
    }

    private static interface FileVisitor<T> {
        public boolean visit(T state, File file);

        public T state();
    }
}

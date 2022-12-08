package project.test.scaffolding;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import java.util.stream.Collectors;

public class ReflectionUtil {
    public static Set<Class<?>> classes(String packageName) {
        final String resourceName = packageName.replace('.', '/');
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final URL root = classLoader.getResource(resourceName);

        if ("file".equals(root.getProtocol())) {
            File[] files = new File(root.getFile()).listFiles((dir, name) -> name.endsWith(".class"));
            return Arrays.asList(files).stream()
                    .map(file -> file.getName())
                    .map(name -> name.replaceAll(".class$", ""))
                    .map(name -> packageName + "." + name)
                    .map(fullName -> unchecked(() -> Class.forName(fullName)))
                    .collect(Collectors.toSet());
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
}

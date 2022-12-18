package project.test.scaffolding.testing;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import project.scaffolding.debug.AnsiColor;
import project.test.scaffolding.ReflectionUtil;

public class TestCollector {
    public static TestSuite collect(String packageName) {
        final List<TestAgent> agents = ReflectionUtil.allClasses(packageName).stream()
                .filter(cls -> cls.getAnnotation(TestAnnotation.class) != null).map(cls -> {
                    final var a = cls.getAnnotation(TestAnnotation.class);
                    if (a.enabled()) {
                        return TestCollector.collect(cls);
                    } else {
                        return new DisabledAgent(cls.getName());
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new TestSuite(packageName, agents);
    }

    // collect test agents defined in class
    public static TestSuite collect(Class<?> cls) {
        Constructor<?> ctor = null;
        try {
            ctor = cls.getDeclaredConstructor();
            ctor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            return null;
        }

        final var methods = cls.getDeclaredMethods();
        if (methods.length == 0) {
            return null;
        }
        var hasTest = false;
        for (var i = 0; i < methods.length; ++i) {
            final var method = methods[i];
            if (method.getAnnotation(TestAnnotation.class) != null) {
                if (method.getParameterCount() == 0) {
                    hasTest = true;
                    method.setAccessible(true);
                    continue;
                }
            }
            methods[i] = null;
        }

        if (!hasTest) {
            return null;
        }

        Object tmp = null;
        try {
            tmp = ctor.newInstance();
        } catch (Exception e) {
            return null;
        }
        final var instance = tmp;
        final List<TestAgent> agents = Stream.of(methods).filter(Objects::nonNull).sorted(TestCollector::methodComparer)
                .map(m -> {
                    final var a = m.getAnnotation(TestAnnotation.class);
                    if (a.enabled()) {
                        return new MethodAgent(m, instance);
                    } else {
                        return new DisabledAgent(m.getName());
                    }
                })
                .collect(Collectors.toList());
        final var suite = new TestSuite(cls.getName(), agents);
        return suite;
    }

    private static int methodComparer(Method l, Method r) {
        final var left = l.getAnnotation(TestAnnotation.class);
        final var right = r.getAnnotation(TestAnnotation.class);

        if (left.order() == right.order()) {
            return 0;
        }
        if (left.order() == -1) {
            return 1;
        }
        if (right.order() == -1) {
            return -1;
        }
        return Integer.compare(left.order(), right.order());
    }

    private static final class DisabledAgent extends TestAgent {
        private static final String message = AnsiColor.fgMagenta + "disabled" + AnsiColor.reset;

        public DisabledAgent(String domain) {
            super(domain);
        }

        @Override
        public TestSummary execute() {
            return TestSummary.withInformation(domain, null, null, message);
        }

    }

    private static final class MethodAgent extends TestAgent {
        public MethodAgent(Method method, Object instance) {
            super(method.getName());
            this.method = method;
            this.instance = instance;// if method is static instance will be ignored
        }

        private final Object instance;
        private final Method method;

        @Override
        public TestSummary execute() {
            final var defaultOut = System.out;
            final var defaultErr = System.err;
            final var stdoutBase = new ByteArrayOutputStream();
            final var stderrBase = new ByteArrayOutputStream();
            try {
                final var stdout = new PrintStream(stdoutBase, true);
                final var stderr = new PrintStream(stderrBase, true);
                System.setOut(stdout);
                System.setErr(stderr);
                this.method.invoke(this.instance);
                return TestSummary.succeeded(this.domain, stdoutBase.toString(), stderrBase.toString());
            } catch (InvocationTargetException e) {
                return TestSummary.withException(this.domain, stdoutBase.toString(), stderrBase.toString(),
                        e.getCause());
            } catch (Exception e) {
                return TestSummary.withException(this.domain, stdoutBase.toString(), stderrBase.toString(), e);
            } finally {
                System.setOut(defaultOut);
                System.setErr(defaultErr);
            }
        }

    }
}

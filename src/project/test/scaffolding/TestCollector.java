package project.test.scaffolding;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestCollector {
    public static TestSuite collect(String packageName) {
        final List<TestAgent> agents = ReflectionUtil.allClasses("project").stream().map(TestCollector::collect)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new TestSuite(packageName, null, agents);
    }

    // collect test agents defined in class
    public static TestSuite collect(Class<?> cls) {
        final var annotation = cls.getAnnotation(TestAnnotation.class);
        if (annotation == null) {
            return null;
        }
        Constructor<?> ctor = null;
        try {
            ctor = cls.getConstructor();
        } catch (NoSuchMethodException e) {
            return null;
        }

        final var methods = cls.getMethods();
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
        final List<TestAgent> agents = Stream.of(methods).filter(Objects::nonNull)
                .map(m -> new MethodAgent(m, instance))
                .collect(Collectors.toList());
        final var suite = new TestSuite(cls.getName(), annotation.description(), agents);
        return suite;
    }

    private static final class MethodAgent extends TestAgent {
        public MethodAgent(Method method, Object instance) {
            super(method.getName(), method.getAnnotation(TestAnnotation.class).description());
            this.method = method;
            this.instance = instance;// if method is static instance will be ignored
        }

        private final Object instance;
        private final Method method;

        @Override
        public TestSummary execute() {
            try {
                this.method.invoke(this.instance);
                return TestSummary.succeeded(this.domain, this.description);
            } catch (Exception e) {
                return TestSummary.withException(this.domain, this.description, e);
            }
        }

    }
}

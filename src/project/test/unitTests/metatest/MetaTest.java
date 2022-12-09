package project.test.unitTests.metatest;

import project.test.scaffolding.TestAnnotation;

@TestAnnotation
public class MetaTest {
    @TestAnnotation
    void test_no_exception() {
        System.out.println("test");
    }

    @TestAnnotation
    void test_with_exception() {
        assert false : "exception";
    }

    @TestAnnotation
    void test_invalid(String val) {
        throw new IllegalStateException("this function must not be called");
    }
}

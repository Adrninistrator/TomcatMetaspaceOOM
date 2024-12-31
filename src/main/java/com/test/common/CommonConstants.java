package com.test.common;

/**
 * @author adrninistrator
 * @date 2024/12/17
 * @description:
 */
public class CommonConstants {

    public static final String PATH_CLASS_TEST = "test";

    public static final String PATH_METHOD_TEST_INT_VALUE = "test_int_value";
    public static final String PATH_METHOD_GET_JMX_INFO = "get_jmx_info";
    public static final String PATH_METHOD_JSON_TEST = "json_test";
    public static final String PATH_METHOD_LOAD_CLASS = "load_class";
    public static final String PATH_METHOD_LOAD_PACKAGES_CLASSES = "load_packages_classes";
    public static final String PATH_METHOD_GET_ALL_CLASS_LOAD_TIMES = "get_all_class_load_times";
    public static final String PATH_METHOD_GEN_DYN_FIXED = "gen_dyn_fixed";
    public static final String PATH_METHOD_NOT_EXISTS = "not_exists";

    public static final String ARG_NAME_CLASS_NAME = "className";
    public static final String ARG_NAME_VALUE1 = "value1";
    public static final String ARG_NAME_PACKAGES = "packages";
    public static final String ARG_NAME_OTHER_LIB_PATH = "otherLibPath";

    public static final String DYN_CLASS_NAME_PREFIX = "com.test.dyn.DynamicClass";
    public static final String FIELD_NAME_PREFIX = "data";
    public static final String FIELD_VALUE_PREFIX = "test_";

    public static final String FIXED_CLASS_NAME_PREFIX = "FixedTestClass";

    public static final String PACKAGE_NAME_COM_TEST = "com.test";
    public static final String PACKAGE_NAME_COM_JAR_TEST_REFLECT = "com.jar.test.reflect";

    private CommonConstants() {
        throw new IllegalStateException("illegal");
    }
}

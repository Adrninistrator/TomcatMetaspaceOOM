package com.test.javaagent;

/**
 * @author adrninistrator
 * @date 2024/12/13
 * @description:
 */
public class JavaAgentConstants {

    public static final String RMI_URI = "rmi://localhost:13637/tmso_javaagent_rmi";
    public static final int RMI_PORT = 13637;

    public static final String CLASS_PATH_PREFIX_JAR_TEST_DTO = "com/jar/test/dto/TestData";
    public static final String CLASS_PATH_PREFIX_JAR_TEST_REFLECT = "com/jar/test/reflect/TestReflect";
    public static final String CLASS_PATH_PREFIX_DYNAMIC_CLASS = "com/test/dyn/DynamicClass";
    public static final String CLASS_PATH_PREFIX_GENERATED_METHOD_ACCESSOR = "sun/reflect/GeneratedMethodAccessor";
    public static final String CLASS_PATH_PREFIX_GENERATED_CONSTRUCTOR_ACCESSOR = "sun/reflect/GeneratedConstructorAccessor";
    public static final String CLASS_PATH_PREFIX_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR = "sun/reflect/GeneratedSerializationConstructorAccessor";
    public static final String CLASS_PATH_PREFIX_GENERATED_METHOD_ACCESSOR_HIGH = "jdk/internal/reflect/GeneratedMethodAccessor";
    public static final String CLASS_PATH_PREFIX_GENERATED_CONSTRUCTOR_ACCESSOR_HIGH = "jdk/internal/reflect/GeneratedConstructorAccessor";
    public static final String CLASS_PATH_PREFIX_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR_HIGH = "jdk/internal/reflect/GeneratedSerializationConstructorAccessor";

    public static final String CLASS_NAME_WILDCARD_JAR_TEST_DTO = "com.jar.test.dto.TestData*";
    public static final String CLASS_NAME_WILDCARD_JAR_TEST_REFLECT = "com.jar.test.reflect.TestReflect*";
    public static final String CLASS_NAME_WILDCARD_DYNAMIC_CLASS = "com.test.dyn.DynamicClass*";
    public static final String CLASS_NAME_WILDCARD_GENERATED_METHOD_ACCESSOR = "sun.reflect.GeneratedMethodAccessor*";
    public static final String CLASS_NAME_WILDCARD_GENERATED_CONSTRUCTOR_ACCESSOR = "sun.reflect.GeneratedConstructorAccessor*";
    public static final String CLASS_NAME_WILDCARD_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR = "sun.reflect.GeneratedSerializationConstructorAccessor*";
    public static final String CLASS_NAME_WILDCARD_GENERATED_METHOD_ACCESSOR_HIGH = "jdk.internal.reflect.GeneratedMethodAccessor*";
    public static final String CLASS_NAME_WILDCARD_GENERATED_CONSTRUCTOR_ACCESSOR_HIGH = "jdk.internal.reflect.GeneratedConstructorAccessor*";
    public static final String CLASS_NAME_WILDCARD_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR_HIGH = "jdk.internal.reflect.GeneratedSerializationConstructorAccessor*";

    private JavaAgentConstants() {
        throw new IllegalStateException("illegal");
    }
}

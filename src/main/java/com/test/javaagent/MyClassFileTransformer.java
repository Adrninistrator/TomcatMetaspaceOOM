package com.test.javaagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author adrninistrator
 * @date 2024/12/12
 * @description:
 */
public class MyClassFileTransformer implements ClassFileTransformer {

    private final AgentDataImpl agentData;

    public MyClassFileTransformer(AgentDataImpl agentData) {
        this.agentData = agentData;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
//        System.out.println("### load class " + className);
        if (className == null || (!className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_JAR_TEST_DTO) &&
                !className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_JAR_TEST_REFLECT) &&
                !className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_DYNAMIC_CLASS) &&
                !className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_GENERATED_METHOD_ACCESSOR) &&
                !className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_GENERATED_CONSTRUCTOR_ACCESSOR) &&
                !className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR)&&
                !className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_GENERATED_METHOD_ACCESSOR_HIGH) &&
                !className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_GENERATED_CONSTRUCTOR_ACCESSOR_HIGH) &&
                !className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR_HIGH)
        )) {
            return null;
        }
        agentData.addClassLoadTimes(className);

        // Returning null means we don't modify the class file
        return null;
    }
}

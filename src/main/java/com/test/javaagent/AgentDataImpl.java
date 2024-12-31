package com.test.javaagent;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author adrninistrator
 * @date 2024/12/13
 * @description:
 */
public class AgentDataImpl extends UnicastRemoteObject implements AgentDataInterface {

    private final Map<String, AtomicInteger> classLoadTimesMap = new ConcurrentHashMap<>();

    protected AgentDataImpl() throws RemoteException {
    }

    @Override
    public int getClassLoadTimes(String className) {
        AtomicInteger atomicInteger = classLoadTimesMap.get(className);
        if (atomicInteger == null) {
            return 0;
        }
        return atomicInteger.get();
    }

    @Override
    public String getAllClassLoadTimes() {
        List<String> classNameList = new ArrayList<>(classLoadTimesMap.keySet());
        Collections.sort(classNameList);
        StringBuilder stringBuilder = new StringBuilder();
        for (String className : classNameList) {
            AtomicInteger atomicInteger = classLoadTimesMap.get(className);
            stringBuilder.append(className).append("=").append(atomicInteger.get()).append("<br>\n");
        }
        if (stringBuilder.length() == 0) {
            return "empty";
        }
        return stringBuilder.toString();
    }

    public void addClassLoadTimes(String className) {
        String usedClassName;
        if (className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_JAR_TEST_DTO)) {
            usedClassName = JavaAgentConstants.CLASS_NAME_WILDCARD_JAR_TEST_DTO;
        } else if (className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_JAR_TEST_REFLECT)) {
            usedClassName = JavaAgentConstants.CLASS_NAME_WILDCARD_JAR_TEST_REFLECT;
        } else if (className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_DYNAMIC_CLASS)) {
            usedClassName = JavaAgentConstants.CLASS_NAME_WILDCARD_DYNAMIC_CLASS;
        } else if (className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_GENERATED_METHOD_ACCESSOR)) {
            usedClassName = JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_METHOD_ACCESSOR;
        } else if (className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_GENERATED_CONSTRUCTOR_ACCESSOR)) {
            usedClassName = JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_CONSTRUCTOR_ACCESSOR;
        } else if (className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR)) {
            usedClassName = JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR;
        } else if (className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_GENERATED_METHOD_ACCESSOR_HIGH)) {
            usedClassName = JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_METHOD_ACCESSOR_HIGH;
        } else if (className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_GENERATED_CONSTRUCTOR_ACCESSOR_HIGH)) {
            usedClassName = JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_CONSTRUCTOR_ACCESSOR_HIGH;
        } else if (className.startsWith(JavaAgentConstants.CLASS_PATH_PREFIX_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR_HIGH)) {
            usedClassName = JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR_HIGH;
        }else {
            return;
        }

        AtomicInteger atomicInteger = classLoadTimesMap.computeIfAbsent(usedClassName, k -> new AtomicInteger(0));
        atomicInteger.addAndGet(1);
    }
}

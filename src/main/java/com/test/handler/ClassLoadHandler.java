package com.test.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.classloader.PublicClassLoader;
import com.test.common.CommonConstants;
import com.test.javaagent.AgentDataInterface;
import com.test.javaagent.JavaAgentConstants;
import com.test.thread.ThreadFactory4TPE;
import com.test.util.CommonUtil;
import com.test.util.JMXUtil;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author adrninistrator
 * @date 2024/12/12
 * @description:
 */
@SuppressWarnings("deprecation")
public class ClassLoadHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClassLoadHandler.class);

    private final ObjectMapper objectMapper;

    private final Map<Integer, String> fieldNumJsonStrMap = new ConcurrentHashMap<>();

    private final ThreadPoolExecutor threadPoolExecutor;

    private final AgentDataInterface agentData;

    private final AtomicInteger fixedClassSeq = new AtomicInteger(0);
    private final AtomicInteger dynClassSeq = new AtomicInteger(0);

    public ClassLoadHandler() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        threadPoolExecutor = new ThreadPoolExecutor(50, 50, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000000), new ThreadFactory4TPE("test_tpe"));
        threadPoolExecutor.allowCoreThreadTimeOut(true);

        agentData = (AgentDataInterface) Naming.lookup(JavaAgentConstants.RMI_URI);
    }

    public String runJsonDeserialize(int startSeq, int classNum, int runTimes, boolean concurrent, boolean dyn, int fieldNum) {
        BigDecimal metaspaceUsedKBBefore = JMXUtil.getMetaspaceUsedKB();
        long ygcTimesBefore = JMXUtil.getYGCTimes();
        long fgcTimesBefore = JMXUtil.getFGCTimes();
        int loadTimesJarTestDtoBefore = getClassLoadTimes(JavaAgentConstants.CLASS_NAME_WILDCARD_JAR_TEST_DTO);
        int loadTimesDynClassBefore = getClassLoadTimes(JavaAgentConstants.CLASS_NAME_WILDCARD_DYNAMIC_CLASS);
        int loadTimesGeneratedMethodAccessorBefore = getClassLoadTimes(JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_METHOD_ACCESSOR);
        int loadTimesGeneratedConstructorAccessorBefore = getClassLoadTimes(JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_CONSTRUCTOR_ACCESSOR);
        int loadTimesGeneratedSerializationConstructorAccessorBefore = getClassLoadTimes(JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR);
        int loadTimesGeneratedMethodAccessorHighBefore = getClassLoadTimes(JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_METHOD_ACCESSOR_HIGH);
        int loadTimesGeneratedConstructorAccessorHighBefore = getClassLoadTimes(JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_CONSTRUCTOR_ACCESSOR_HIGH);
        int loadTimesGeneratedSerializationConstructorAccessorHighBefore =
                getClassLoadTimes(JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR_HIGH);

        long startTime = System.currentTimeMillis();
        int usedStartSeq = dyn ? 1 : startSeq;
        int usedEndSeq = dyn ? classNum : classNum + startSeq - 1;
        for (int i = usedStartSeq; i <= usedEndSeq; i++) {
            Class<?> clazz = chooseClass(dyn, i, fieldNum);
            for (int j = 1; j <= runTimes; j++) {
                Runnable runnable = () -> jsonDeserialize(clazz, fieldNum);
                if (concurrent) {
                    threadPoolExecutor.execute(runnable);
                } else {
                    runnable.run();
                }
            }
        }
        if (concurrent) {
            // 等待直到线程池中的任务执行完毕
            while (threadPoolExecutor.getActiveCount() > 0 || !threadPoolExecutor.getQueue().isEmpty()) {
                CommonUtil.sleep(100L);
            }
        }

        long spendTime = CommonUtil.getSpendTime(startTime);

        BigDecimal metaspaceUsedKBAfter = JMXUtil.getMetaspaceUsedKB();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("\nrunJsonDeserialize runTimes: %s classNum: %s concurrent: %s dyn: %s fieldNum: %s spendTime: %d\n",
                runTimes, classNum, concurrent, dyn, fieldNum, spendTime));

        addMetaspaceUsedKBLog(stringBuilder, metaspaceUsedKBBefore, metaspaceUsedKBAfter);

        addGCTimesLog(stringBuilder, true, ygcTimesBefore);
        addGCTimesLog(stringBuilder, false, fgcTimesBefore);

        addLoadTimesLog(stringBuilder, loadTimesJarTestDtoBefore, JavaAgentConstants.CLASS_NAME_WILDCARD_JAR_TEST_DTO);
        addLoadTimesLog(stringBuilder, loadTimesDynClassBefore, JavaAgentConstants.CLASS_NAME_WILDCARD_DYNAMIC_CLASS);

        addLoadTimesLog(stringBuilder, loadTimesGeneratedMethodAccessorBefore, JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_METHOD_ACCESSOR);
        addLoadTimesLog(stringBuilder, loadTimesGeneratedConstructorAccessorBefore, JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_CONSTRUCTOR_ACCESSOR);
        addLoadTimesLog(stringBuilder, loadTimesGeneratedSerializationConstructorAccessorBefore,
                JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR);
        addLoadTimesLog(stringBuilder, loadTimesGeneratedMethodAccessorHighBefore, JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_METHOD_ACCESSOR_HIGH);
        addLoadTimesLog(stringBuilder, loadTimesGeneratedConstructorAccessorHighBefore, JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_CONSTRUCTOR_ACCESSOR_HIGH);
        addLoadTimesLog(stringBuilder, loadTimesGeneratedSerializationConstructorAccessorHighBefore,
                JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR_HIGH);

        String logContent = stringBuilder.toString();
        logger.info("{}", logContent);
        return replaceWithBr(logContent);
    }

    public String runReflect(int startSeq, int classNum, int runTimes) {
        BigDecimal metaspaceUsedKBBefore = JMXUtil.getMetaspaceUsedKB();
        long ygcTimesBefore = JMXUtil.getYGCTimes();
        long fgcTimesBefore = JMXUtil.getFGCTimes();
        int loadTimesJarTestReflectBefore = getClassLoadTimes(JavaAgentConstants.CLASS_NAME_WILDCARD_JAR_TEST_REFLECT);
        int loadTimesGeneratedMethodAccessorBefore = getClassLoadTimes(JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_METHOD_ACCESSOR);
        int loadTimesGeneratedConstructorAccessorBefore = getClassLoadTimes(JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_CONSTRUCTOR_ACCESSOR);
        int loadTimesGeneratedSerializationConstructorAccessorBefore = getClassLoadTimes(JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR);
        int loadTimesGeneratedMethodAccessorHighBefore = getClassLoadTimes(JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_METHOD_ACCESSOR_HIGH);
        int loadTimesGeneratedConstructorAccessorHighBefore = getClassLoadTimes(JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_CONSTRUCTOR_ACCESSOR_HIGH);
        int loadTimesGeneratedSerializationConstructorAccessorHighBefore =
                getClassLoadTimes(JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR_HIGH);

        long startTime = System.currentTimeMillis();
        for (int i = startSeq; i <= classNum + startSeq - 1; i++) {
            for (int j = 1; j <= runTimes; j++) {
                invokeMethodByReflect(i);
            }
        }

        long spendTime = CommonUtil.getSpendTime(startTime);

        BigDecimal metaspaceUsedKBAfter = JMXUtil.getMetaspaceUsedKB();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("\nrunReflect runTimes: %s classNum: %s spendTime: %d\n", runTimes, classNum, spendTime));

        addMetaspaceUsedKBLog(stringBuilder, metaspaceUsedKBBefore, metaspaceUsedKBAfter);

        addGCTimesLog(stringBuilder, true, ygcTimesBefore);
        addGCTimesLog(stringBuilder, false, fgcTimesBefore);

        addLoadTimesLog(stringBuilder, loadTimesJarTestReflectBefore, JavaAgentConstants.CLASS_NAME_WILDCARD_JAR_TEST_REFLECT);

        addLoadTimesLog(stringBuilder, loadTimesGeneratedMethodAccessorBefore, JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_METHOD_ACCESSOR);
        addLoadTimesLog(stringBuilder, loadTimesGeneratedConstructorAccessorBefore, JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_CONSTRUCTOR_ACCESSOR);
        addLoadTimesLog(stringBuilder, loadTimesGeneratedSerializationConstructorAccessorBefore,
                JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR);
        addLoadTimesLog(stringBuilder, loadTimesGeneratedMethodAccessorHighBefore, JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_METHOD_ACCESSOR_HIGH);
        addLoadTimesLog(stringBuilder, loadTimesGeneratedConstructorAccessorHighBefore, JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_CONSTRUCTOR_ACCESSOR_HIGH);
        addLoadTimesLog(stringBuilder, loadTimesGeneratedSerializationConstructorAccessorHighBefore,
                JavaAgentConstants.CLASS_NAME_WILDCARD_GENERATED_SERIALIZATION_CONSTRUCTOR_ACCESSOR_HIGH);

        String logContent = stringBuilder.toString();
        logger.info("{}", logContent);
        return replaceWithBr(logContent);
    }

    private void addMetaspaceUsedKBLog(StringBuilder logContent, BigDecimal metaspaceUsedKBBefore, BigDecimal metaspaceUsedKBAfter) {
        BigDecimal metaspaceUsedKBAdd = CommonUtil.minus2HalfUp(metaspaceUsedKBAfter, metaspaceUsedKBBefore);
        logContent.append(String.format("metaspaceUsedKBBefore: %s metaspaceUsedKBAfter: %s metaspaceUsedKBAdd: %s\n",
                metaspaceUsedKBBefore.toPlainString(), metaspaceUsedKBAfter.toPlainString(), metaspaceUsedKBAdd.toPlainString()));
    }

    private void addGCTimesLog(StringBuilder logContent, boolean ygc, long gcTimesBefore) {
        String gcName;
        long gcTimesAfter;
        if (ygc) {
            gcName = "ygc";
            gcTimesAfter = JMXUtil.getYGCTimes();
        } else {
            gcName = "fgc";
            gcTimesAfter = JMXUtil.getFGCTimes();
        }

        long gcTimesAdd = gcTimesAfter - gcTimesBefore;
        if (gcTimesAdd > 0) {
            logContent.append(String.format("%sTimesBefore: %s %sTimesAfter: %s %sTimesAdd: %s\n",
                    gcName, gcTimesAfter, gcName, gcTimesBefore, gcName, gcTimesAdd));
        }
    }

    private void addLoadTimesLog(StringBuilder logContent, int loadTimesBefore, String className) {
        int loadTimesAfter = getClassLoadTimes(className);
        int loadTimesAdd = loadTimesAfter - loadTimesBefore;
        if (loadTimesAdd != 0) {
            logContent.append(String.format("%s loadTimesBefore: %s loadTimesAfter: %s loadTimesAdd: %s\n",
                    className, loadTimesBefore, loadTimesAfter, loadTimesAdd));
        }
    }

    private int getClassLoadTimes(String className) {
        try {
            return agentData.getClassLoadTimes(className);
        } catch (Exception e) {
            logger.error("error ", e);
            return 0;
        }
    }

    public String jsonSerialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("error ", e);
            return "";
        }
    }

    public <T> void jsonDeserialize(Class<T> clazz, int fieldNum) {
        String jsonStr = fieldNumJsonStrMap.get(fieldNum);
        if (jsonStr == null) {
            jsonStr = genJsonStr(fieldNum);
            fieldNumJsonStrMap.put(fieldNum, jsonStr);
        }

        T object = jsonDeserialize(jsonStr, clazz);
//        logger.info("jsonStr: {} object type: {} value: {}", jsonStr, clazz.getName(), jsonSerialize(object));
        if (object == null) {
            logger.error("object is null");
            throw new RuntimeException();
        }
    }

    public <T> T jsonDeserialize(String jsonStr, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonStr, clazz);
        } catch (Exception e) {
            logger.error("error ", e);
            return null;
        }
    }

    public Class<?> chooseClass(boolean dyn, int classSeq, int fieldNum) {
        if (dyn) {
            return genDynClass(CommonConstants.DYN_CLASS_NAME_PREFIX, fieldNum);
        }
        return getTestJarDataClass(classSeq);
    }

    private String genClassName(String classNamePrefix, int classSeq) {
        return String.format("%s%06d", classNamePrefix, classSeq);
    }

    private Class<?> getTestJarDataClass(int classSeq) {
        String className = genClassName("com.jar.test.dto.TestData", classSeq);
        return JMXUtil.loadClass(className);
    }

    public String loadClass(String className) {
        BigDecimal metaspaceUsedKBBefore = JMXUtil.getMetaspaceUsedKB();
        long ygcTimesBefore = JMXUtil.getYGCTimes();
        long fgcTimesBefore = JMXUtil.getFGCTimes();
        JMXUtil.loadClass(className);

        BigDecimal metaspaceUsedKBAfter = JMXUtil.getMetaspaceUsedKB();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("\nload class: %s\n", className));

        addMetaspaceUsedKBLog(stringBuilder, metaspaceUsedKBBefore, metaspaceUsedKBAfter);

        addGCTimesLog(stringBuilder, true, ygcTimesBefore);
        addGCTimesLog(stringBuilder, false, fgcTimesBefore);

        String logContent = stringBuilder.toString();
        logger.info("{}", logContent);
        return replaceWithBr(logContent);
    }

    @SuppressWarnings("deprecation")
    public String loadPackagesClasses(String otherLibPath, String packages) {
        long startTime = System.currentTimeMillis();
        PublicClassLoader publicClassLoader = null;
        try {
            if (otherLibPath != null) {
                File otherLibDir = new File(otherLibPath);
                if (!otherLibDir.exists() || !otherLibDir.isDirectory()) {
                    logger.error("指定的目录不存在或不是目录 {}", otherLibPath);
                    return "指定的目录不存在或不是目录 " + otherLibPath;
                }
                File[] files = otherLibDir.listFiles();
                if (ArrayUtils.isEmpty(files)) {
                    logger.error("指定的目录为空 {}", otherLibPath);
                    return "指定的目录为空 " + otherLibPath;
                }

                ClassLoader parent = Thread.currentThread().getContextClassLoader();
                publicClassLoader = new PublicClassLoader(new URL[]{}, parent);

                for (File file : files) {
                    if (!file.getName().endsWith(".jar")) {
                        continue;
                    }
                    logger.info("add jar to class loader {}", file.getAbsolutePath());
                    URL jarUrl = file.toURI().toURL();
                    publicClassLoader.addURL(jarUrl);
                }
                Thread.currentThread().setContextClassLoader(publicClassLoader);
            }
        } catch (Exception e) {
            logger.error("出现异常 ", e);
            return "出现异常 " + e.getMessage();
        }

        BigDecimal metaspaceUsedKBBefore = JMXUtil.getMetaspaceUsedKB();
        long ygcTimesBefore = JMXUtil.getYGCTimes();
        long fgcTimesBefore = JMXUtil.getFGCTimes();
        int loadClassNum = 0;
        String[] packageArray = StringUtils.split(packages, ",");
        for (String packageName : packageArray) {
            logger.info("load class in package: {}", packageName);
            Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
            Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
            List<Class<?>> classList = new ArrayList<>(classes);
            classList.sort(Comparator.comparing(Class::getName));
            for (Class<?> clazz : classList) {
                loadClassNum++;
                String className = clazz.getName();
                try {
                    logger.debug("load class: {}", className);
                    if (publicClassLoader == null) {
                        Class.forName(className);
                    } else {
                        Class.forName(className, false, publicClassLoader);
                    }
                } catch (Throwable e) {
                    logger.error("load class error {} ", className, e);
                }
            }
        }

        long spendTime = CommonUtil.getSpendTime(startTime);
        BigDecimal metaspaceUsedKBAfter = JMXUtil.getMetaspaceUsedKB();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("\nload classes in packages: %s load class num: %d spendTime: %d\n", packages, loadClassNum, spendTime));

        addMetaspaceUsedKBLog(stringBuilder, metaspaceUsedKBBefore, metaspaceUsedKBAfter);

        addGCTimesLog(stringBuilder, true, ygcTimesBefore);
        addGCTimesLog(stringBuilder, false, fgcTimesBefore);

        String logContent = stringBuilder.toString();
        logger.info("{}", logContent);
        return replaceWithBr(logContent);
    }

    public Class<?> genDynClass(String classNamePrefix, int fieldNum) {
        try {
            ClassPool pool = ClassPool.getDefault();

            AtomicInteger atomicInteger = CommonConstants.FIXED_CLASS_NAME_PREFIX.equals(classNamePrefix) ? fixedClassSeq : dynClassSeq;
            String className = genClassName(classNamePrefix, atomicInteger.addAndGet(1));
            CtClass ctClass = pool.makeClass(className);

            for (int i = 1; i <= fieldNum; i++) {
                String fieldName = genFieldName(i);
                CtField data1Field = new CtField(pool.get("java.lang.String"), fieldName, ctClass);
                ctClass.addField(data1Field);

                String fieldNameFirstUpper = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                ctClass.addMethod(CtNewMethod.getter("get" + fieldNameFirstUpper, data1Field));
                ctClass.addMethod(CtNewMethod.setter("set" + fieldNameFirstUpper, data1Field));
            }

            return ctClass.toClass();
        } catch (Exception e) {
            logger.error("error ", e);
            throw new RuntimeException();
        }
    }

    private String genJsonStr(int fieldNum) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        for (int i = 1; i <= fieldNum; i++) {
            String fieldName = genFieldName(i);
            String keyValue = String.format("\"%s\":\"%s\"", fieldName, CommonConstants.FIELD_VALUE_PREFIX + fieldName);
            stringBuilder.append(keyValue);
            if (i < fieldNum) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    private String genFieldName(int fieldSeq) {
        return String.format("%s%04d", CommonConstants.FIELD_NAME_PREFIX, fieldSeq);
    }

    public String getAllClassLoadTimes() throws RemoteException {
        return agentData.getAllClassLoadTimes();
    }

    private void invokeMethodByReflect(int classSeq) {
        Class<?> clazz = JMXUtil.loadClass(genClassName(CommonConstants.PACKAGE_NAME_COM_JAR_TEST_REFLECT + ".TestReflect", classSeq));
        int returnValue;
        try {
            Method method = clazz.getMethod("method4Reflect");
            returnValue = (int) method.invoke(null);
//            logger.info("### invokeMethodByReflect {} {} {}", clazz.getName(), method.getName(), returnValue);
        } catch (Exception e) {
            logger.error("error ", e);
            throw new RuntimeException();
        }
        if (returnValue != 1) {
            logger.error("方法返回值与预期不同 {}", returnValue);
            throw new RuntimeException("方法返回值与预期不同");
        }
    }

    private String replaceWithBr(String content) {
        return StringUtils.replace(content, "\n", "<br>");
    }
}

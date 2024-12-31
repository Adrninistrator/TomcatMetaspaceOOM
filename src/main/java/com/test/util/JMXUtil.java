package com.test.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author adrninistrator
 * @date 2024/12/12
 * @description:
 */
public class JMXUtil {

    private static final Logger logger = LoggerFactory.getLogger(JMXUtil.class);

    private static boolean PRINT_FLAG = false;

    public static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            logger.error("ClassNotFoundException {}", className);
            throw new RuntimeException();
        }
    }

    public static BigDecimal getMetaspaceUsedKB() {
        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : memoryPools) {
            if (StringUtils.equalsIgnoreCase(pool.getName(), "Metaspace")) {
                return CommonUtil.calcKBFromByte(pool.getUsage().getUsed());
            }
        }
        return BigDecimal.ZERO;
    }

    public static String getJMXInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : memoryPools) {
            if (!PRINT_FLAG) {
                logger.info("pool.getName() {}", pool.getName());
            }
            if (StringUtils.equalsIgnoreCase(pool.getName(), "Metaspace")) {
                BigDecimal init = CommonUtil.calcKBFromByte(pool.getUsage().getInit());
                BigDecimal max = CommonUtil.calcKBFromByte(pool.getUsage().getMax());
                BigDecimal committed = CommonUtil.calcKBFromByte(pool.getUsage().getCommitted());
                BigDecimal used = CommonUtil.calcKBFromByte(pool.getUsage().getUsed());
                stringBuilder.append("Metaspace init: ").append(init.toPlainString()).append("<br>\n");
                stringBuilder.append("Metaspace max: ").append(max.toPlainString()).append("<br>\n");
                stringBuilder.append("Metaspace committed: ").append(committed.toPlainString()).append("<br>\n");
                stringBuilder.append("Metaspace used: ").append(used.toPlainString()).append("<br>\n");
            }
        }

        List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        long ygcTimes = 0;
        long fgcTimes = 0;
        for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
            if (!PRINT_FLAG) {
                logger.info("gcMXBean.getName() {}", gcMXBean.getName());
            }
            if (checkJdk8YGC(gcMXBean.getName())) {
                ygcTimes = gcMXBean.getCollectionCount();
            } else if (checkJdk8FGC(gcMXBean.getName())) {
                fgcTimes = gcMXBean.getCollectionCount();
            }
        }
        stringBuilder.append("ygcTimes: ").append(ygcTimes).append("<br>\n");
        stringBuilder.append("fgcTimes: ").append(fgcTimes);

        if (!PRINT_FLAG) {
            PRINT_FLAG = true;
        }
        return stringBuilder.toString();
    }

    public static long getFGCTimes() {
        List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
            if (checkJdk8FGC(gcMXBean.getName())) {
                return gcMXBean.getCollectionCount();
            }
        }
        return 0;
    }

    public static long getYGCTimes() {
        List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
            if (checkJdk8YGC(gcMXBean.getName())) {
                return gcMXBean.getCollectionCount();
            }
        }
        return 0;
    }

    private static boolean checkJdk8YGC(String gcMXBeanName) {
        return StringUtils.equalsAny(gcMXBeanName, "PS Scavenge", "ParNew");
    }

    private static boolean checkJdk8FGC(String gcMXBeanName) {
        return StringUtils.equalsAny(gcMXBeanName, "PS MarkSweep", "ConcurrentMarkSweep");
    }

    private JMXUtil() {
        throw new IllegalStateException("illegal");
    }
}

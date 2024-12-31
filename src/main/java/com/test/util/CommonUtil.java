package com.test.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author adrninistrator
 * @date 2024/12/11
 * @description:
 */
public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.error("error ", e);
            Thread.currentThread().interrupt();
        }
    }

    public static BigDecimal minus2HalfUp(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        return bigDecimal1.subtract(bigDecimal2).setScale(1, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcKBFromByte(long value) {
        return BigDecimal.valueOf(value).divide(BigDecimal.valueOf(1024L), 4, RoundingMode.HALF_UP);
    }

    public static long getSpendTime(long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    private CommonUtil() {
        throw new IllegalStateException("illegal");
    }
}

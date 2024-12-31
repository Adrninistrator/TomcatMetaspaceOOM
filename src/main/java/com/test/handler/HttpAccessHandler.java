package com.test.handler;

import com.test.common.CommonConstants;
import com.test.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.util.Set;

/**
 * @author adrninistrator
 * @date 2024/12/17
 * @description:
 */
public class HttpAccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpAccessHandler.class);

    private final RestTemplate restTemplate = new RestTemplate();

    public void access() {
        try {
            int port = 0;
            String path = null;
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

            ObjectName name4Connector = new ObjectName("Catalina:type=Connector,*");
            Set<ObjectName> objectNameSet4Connector = mbeanServer.queryNames(name4Connector, null);
            for (ObjectName objectName4Connector : objectNameSet4Connector) {
                port = (int) mbeanServer.getAttribute(objectName4Connector, "port");
                logger.info("Connector {} port {}", objectName4Connector, port);
                break;
            }

            ObjectName name4WebModule = new ObjectName("Catalina:j2eeType=WebModule,*");
            Set<ObjectName> objectNameSet4WebModule = mbeanServer.queryNames(name4WebModule, null);
            for (ObjectName objectName4WebModule : objectNameSet4WebModule) {
                path = (String) mbeanServer.getAttribute(objectName4WebModule, "path");
                logger.info("WebModule {} port {}", objectName4WebModule, port);
                break;
            }

            if (port == 0 || path == null) {
                logger.error("未获取到当前Tomcat的监听端口或上下文路径 [{}] [{}]", port, path);
                return;
            }

            ObjectName name4Server = new ObjectName("Catalina:type=Server");
            while (true) {
                String state = (String) mbeanServer.getAttribute(name4Server, "stateName");
                logger.info("等待Tomcat启动 {}", state);
                CommonUtil.sleep(2000L);
                if ("STARTED".equals(state)) {
                    break;
                }
            }
            String urlHeader = "http://localhost:" + port + path + "/";

            doAccess(urlHeader + CommonConstants.PATH_CLASS_TEST + "/" + CommonConstants.PATH_METHOD_TEST_INT_VALUE + "?" + CommonConstants.ARG_NAME_VALUE1 + "=1");
            doAccess(urlHeader + CommonConstants.PATH_CLASS_TEST + "/" + CommonConstants.PATH_METHOD_TEST_INT_VALUE + "?" + CommonConstants.ARG_NAME_VALUE1 + "=30000");
            doAccess(urlHeader + CommonConstants.PATH_CLASS_TEST + "/" + CommonConstants.PATH_METHOD_GET_JMX_INFO);
            doAccess(urlHeader + CommonConstants.PATH_CLASS_TEST + "/" + CommonConstants.PATH_METHOD_JSON_TEST);
            doAccess(urlHeader + CommonConstants.PATH_CLASS_TEST + "/" + CommonConstants.PATH_METHOD_LOAD_CLASS + "?" + CommonConstants.ARG_NAME_CLASS_NAME + "=" + BigDecimal.class.getName());
            doAccess(urlHeader + CommonConstants.PATH_CLASS_TEST + "/" + CommonConstants.PATH_METHOD_LOAD_CLASS + "?" + CommonConstants.ARG_NAME_CLASS_NAME +
                    "=sun.util.resources.TimeZoneNames");
            doAccess(urlHeader + CommonConstants.PATH_CLASS_TEST + "/" + CommonConstants.PATH_METHOD_LOAD_CLASS + "?" + CommonConstants.ARG_NAME_CLASS_NAME +
                    "=sun.util.resources.en.TimeZoneNames_en");
            doAccess(urlHeader + CommonConstants.PATH_CLASS_TEST + "/" + CommonConstants.PATH_METHOD_LOAD_PACKAGES_CLASSES + "?" + CommonConstants.ARG_NAME_PACKAGES + "=" + CommonConstants.PACKAGE_NAME_COM_TEST);
            doAccess(urlHeader + CommonConstants.PATH_CLASS_TEST + "/" + CommonConstants.PATH_METHOD_LOAD_PACKAGES_CLASSES + "?" + CommonConstants.ARG_NAME_PACKAGES + "=" + CommonConstants.PACKAGE_NAME_COM_JAR_TEST_REFLECT);
            doAccess(urlHeader + CommonConstants.PATH_CLASS_TEST + "/" + CommonConstants.PATH_METHOD_GET_ALL_CLASS_LOAD_TIMES);
            doAccess(urlHeader + CommonConstants.PATH_CLASS_TEST + "/" + CommonConstants.PATH_METHOD_GEN_DYN_FIXED);
            doAccess(urlHeader + CommonConstants.PATH_CLASS_TEST + "/" + CommonConstants.PATH_METHOD_NOT_EXISTS, true);
            doAccess(urlHeader + CommonConstants.PATH_CLASS_TEST + "/" + CommonConstants.PATH_METHOD_GET_ALL_CLASS_LOAD_TIMES);

            logger.info("pre load done");
        } catch (Exception e) {
            logger.error("出现异常 ", e);
        }
    }

    private void doAccess(String url) {
        doAccess(url, false);
    }

    private void doAccess(String url, boolean notExists) {
        if (!notExists) {
            try {
                String response = restTemplate.getForObject(url, String.class);
                logger.info("url {} response {}", url, response);
            } catch (Exception e) {
                logger.error("访问失败 {} ", url, e);
            }
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept-Language", "zh-CN,zh;q=0.9");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
            logger.info("notExists url {} response {}", url, response);
        } catch (Exception e) {
            logger.error("出现预期内的异常 {} {}", url, e.getMessage());
        }
    }
}

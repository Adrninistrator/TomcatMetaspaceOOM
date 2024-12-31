package com.test.controller;

import com.test.common.CommonConstants;
import com.test.dto.TestJsonData1;
import com.test.dto.TestJsonData2;
import com.test.dto.TestJsonData3;
import com.test.handler.ClassLoadHandler;
import com.test.util.JMXUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.rmi.RemoteException;

/**
 * @author adrninistrator
 * @date 2024/12/10
 * @description:
 */
@RestController
@RequestMapping(CommonConstants.PATH_CLASS_TEST)
public class TestController implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    private ClassLoadHandler classLoadHandler;

    public void init() throws Exception {
        logger.info("init classLoader {}", this.getClass().getClassLoader());
        classLoadHandler = new ClassLoadHandler();
    }

    @GetMapping("serial")
    public String serial(@RequestParam(name = "startSeq", defaultValue = "1") int startSeq,
                         @RequestParam(name = "classNum", defaultValue = "1") int classNum,
                         @RequestParam(name = "runTimes", defaultValue = "1") int runTimes,
                         @RequestParam(name = "fieldNum", defaultValue = "1") int fieldNum) {
        return classLoadHandler.runJsonDeserialize(startSeq, classNum, runTimes, false, false, fieldNum);
    }

    @GetMapping("concurrent")
    public String concurrent(@RequestParam(name = "startSeq", defaultValue = "1") int startSeq,
                             @RequestParam(name = "classNum", defaultValue = "1") int classNum,
                             @RequestParam(name = "runTimes", defaultValue = "1") int runTimes,
                             @RequestParam(name = "fieldNum", defaultValue = "1") int fieldNum) {
        return classLoadHandler.runJsonDeserialize(startSeq, classNum, runTimes, true, false, fieldNum);
    }

    @GetMapping("dyn_serial")
    public String dynSerial(@RequestParam(name = "classNum", defaultValue = "1") int classNum,
                            @RequestParam(name = "runTimes", defaultValue = "1") int runTimes,
                            @RequestParam(name = "fieldNum", defaultValue = "1") int fieldNum) {
        return classLoadHandler.runJsonDeserialize(0, classNum, runTimes, false, true, fieldNum);
    }

    @GetMapping("dyn_concurrent")
    public String dynConcurrent(@RequestParam(name = "classNum", defaultValue = "1") int classNum,
                                @RequestParam(name = "runTimes", defaultValue = "1") int runTimes,
                                @RequestParam(name = "fieldNum", defaultValue = "1") int fieldNum) {
        return classLoadHandler.runJsonDeserialize(0, classNum, runTimes, true, true, fieldNum);
    }

    @GetMapping("reflect")
    public String reflect(@RequestParam(name = "startSeq", defaultValue = "1") int startSeq,
                          @RequestParam(name = "classNum", defaultValue = "1") int classNum,
                          @RequestParam(name = "runTimes", defaultValue = "1") int runTimes) {
        return classLoadHandler.runReflect(startSeq, classNum, runTimes);
    }

    @GetMapping(CommonConstants.PATH_METHOD_TEST_INT_VALUE)
    public String testIntValue(@RequestParam(name = "value1", defaultValue = "1") int value1,
                               @RequestParam(name = "runTimes", defaultValue = "1") int value2) {
        return value1 + " " + value2;
    }

    @GetMapping(CommonConstants.PATH_METHOD_GET_JMX_INFO)
    public String getJMXInfo() {
        return JMXUtil.getJMXInfo();
    }

    @GetMapping(CommonConstants.PATH_METHOD_JSON_TEST)
    public String jsonTest() {
        TestJsonData1 testJsonData1 = new TestJsonData1();
        testJsonData1.setData1("test");
        testJsonData1.setData2("test");
        testJsonData1.setData3("test");
        String jsonStr = classLoadHandler.jsonSerialize(testJsonData1);
        logger.info("jsonStr: {}", jsonStr);
        TestJsonData1 testJsonData1Tmp = classLoadHandler.jsonDeserialize(jsonStr, TestJsonData1.class);
        logger.info("testJsonData1Tmp: {}", testJsonData1Tmp);
        return jsonStr;
    }

    @GetMapping("json_serialize")
    public String jsonSerialize() {
        TestJsonData3 testJsonData3 = new TestJsonData3();
        testJsonData3.setData1("test");
        String jsonStr = classLoadHandler.jsonSerialize(testJsonData3);
        logger.info("jsonStr: {}", jsonStr);
        return jsonStr;
    }

    @GetMapping(CommonConstants.PATH_METHOD_LOAD_CLASS)
    public String loadClass(@RequestParam(name = CommonConstants.ARG_NAME_CLASS_NAME) String className) {
        return classLoadHandler.loadClass(className);
    }

    @GetMapping(CommonConstants.PATH_METHOD_LOAD_PACKAGES_CLASSES)
    public String loadPackagesClasses(@RequestParam(name = CommonConstants.ARG_NAME_OTHER_LIB_PATH, required = false) String otherLibPath,
                                      @RequestParam(name = CommonConstants.ARG_NAME_PACKAGES) String packages) {
        return classLoadHandler.loadPackagesClasses(otherLibPath, packages);
    }

    @GetMapping(CommonConstants.PATH_METHOD_GET_ALL_CLASS_LOAD_TIMES)
    public String getAllClassLoadTimes() throws RemoteException {
        return classLoadHandler.getAllClassLoadTimes();
    }

    @GetMapping(CommonConstants.PATH_METHOD_GEN_DYN_FIXED)
    public String genDynFixed() {
        classLoadHandler.genDynClass(CommonConstants.FIXED_CLASS_NAME_PREFIX, 1);
        return "ok" + System.currentTimeMillis();
    }

    @GetMapping("copy_properties")
    public String copyProperties() {
        TestJsonData1 testJsonData1 = new TestJsonData1();
        testJsonData1.setData1("test");
        testJsonData1.setData2("test");
        testJsonData1.setData3("test");
        TestJsonData2 testJsonData2 = new TestJsonData2();
        BeanUtils.copyProperties(testJsonData1, testJsonData2);
        return "ok" + System.currentTimeMillis();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
}

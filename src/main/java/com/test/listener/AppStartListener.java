package com.test.listener;

import com.test.handler.HttpAccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * @author adrninistrator
 * @date 2024/12/13
 * @description:
 */
@WebListener
public class AppStartListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppStartListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("contextInitialized");

        Thread thread = new Thread(() -> {
            HttpAccessHandler httpAccessHandler = new HttpAccessHandler();
            httpAccessHandler.access();
        });
        thread.setName("http_access");
        thread.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("contextDestroyed");
    }
}
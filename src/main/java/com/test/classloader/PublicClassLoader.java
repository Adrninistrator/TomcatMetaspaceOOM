package com.test.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author adrninistrator
 * @date 2024/12/30
 * @description:
 */
public class PublicClassLoader extends URLClassLoader {
    public PublicClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}

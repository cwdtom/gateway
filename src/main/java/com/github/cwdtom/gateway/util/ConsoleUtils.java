package com.github.cwdtom.gateway.util;

import com.github.cwdtom.gateway.Application;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * 命令行工具
 *
 * @author chenweidong
 * @since 2.2.1
 */
public class ConsoleUtils {
    /**
     * 获取版本号
     *
     * @return 版本号
     * @throws IOException 读取文件错误
     */
    public static String getVersion() throws IOException {
        JarFile jarFile = new JarFile(Application.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        Manifest manifest = jarFile.getManifest();
        Attributes attribute = manifest.getMainAttributes();
        return attribute.getValue("Version");
    }
}

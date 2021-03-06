package com.genesis.robot.core.config;

import com.genesis.core.config.ConfigBuilder;
import com.genesis.core.template.TemplateService;
import com.typesafe.config.Config;

import java.net.URL;

public class RobotConfig {

    /** 读入配置的Config对象。 */
    private static Config config = ConfigBuilder.buildConfigFromFileName("Robot.conf");


    /**
     * 资源路径
     * @return
     */
    public static String getBaseResourceDir() {
        return config.getString("server.baseResourceDir");
    }

    /**
     * 是否加密
     * @return
     */
    public static boolean isXorLoad() {
        return config.getBoolean("server.isXorLoad");
    }

    /**
     * 获取所有Excel
     * @return
     */
    public static TemplateService getTemplateService() {
        boolean isXorLoad = config.getBoolean("server.isXorLoad");
        final TemplateService templateService =
                new TemplateService(getBaseResourceDir(), isXorLoad);
        return templateService;
    }

    public static URL getTemplatesURL() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResource("templates.xml");
    }
}

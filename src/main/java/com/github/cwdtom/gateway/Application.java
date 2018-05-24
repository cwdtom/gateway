package com.github.cwdtom.gateway;

import com.github.cwdtom.gateway.entity.Constant;
import com.github.cwdtom.gateway.environment.ConfigEnvironment;
import com.github.cwdtom.gateway.environment.ThreadPool;
import com.github.cwdtom.gateway.listener.HttpListener;
import com.github.cwdtom.gateway.listener.HttpsListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

/**
 * 启动类
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class Application {
    /**
     * 启动方法
     *
     * @param args 参数
     */
    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption(Constant.COMMAND_CONFIG, true, "config file path");
        options.addOption(Constant.COMMAND_HELP, false, "help info");
        options.addOption(Constant.COMMAND_VERSION, false, "show version info");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption(Constant.COMMAND_HELP)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ant", options);
            System.exit(0);
        } else if (cmd.hasOption(Constant.COMMAND_VERSION)) {
            System.out.println("version: " + Constant.VERSION);
            System.exit(0);
        }

        if (cmd.hasOption(Constant.COMMAND_CONFIG)) {
            ConfigEnvironment.init(cmd.getOptionValue(Constant.COMMAND_CONFIG));
        } else {
            log.error("config file path arg is not found.");
            System.exit(1);
        }

        // 启动http监听
        HttpListener http = new HttpListener();
        ThreadPool.execute(http);
        // 启动https监听
        HttpsListener https = new HttpsListener();
        ThreadPool.execute(https);

        // 添加销毁事件
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            http.shutdown();
            System.out.println("http listener was shutdown!");
            https.shutdown();
            System.out.println("https listener was shutdown!");
            ThreadPool.shutdown();
            System.out.println("gateway was shutdown!");
        }));
    }
}

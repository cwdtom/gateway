package com.github.cwdtom.gateway;

import com.github.cwdtom.gateway.constant.ConsoleConstant;
import com.github.cwdtom.gateway.environment.ApplicationContext;
import com.github.cwdtom.gateway.limit.TokenProvider;
import com.github.cwdtom.gateway.listener.HttpListener;
import com.github.cwdtom.gateway.listener.HttpsListener;
import com.github.cwdtom.gateway.mapping.SurvivalCheck;
import com.github.cwdtom.gateway.thread.DefaultRejectedExecutionHandler;
import com.github.cwdtom.gateway.thread.DefaultThreadFactory;
import com.github.cwdtom.gateway.thread.ThreadPoolGroup;
import com.github.cwdtom.gateway.util.ConsoleUtils;
import eu.medsea.mimeutil.MimeUtil;
import org.apache.commons.cli.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 启动类
 *
 * @author chenweidong
 * @since 1.0.0
 */
public class Application {
    /**
     * 启动方法
     *
     * @param args 参数
     * @throws Exception 应用运行异常
     */
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(ConsoleConstant.COMMAND_CONFIG, true, "config file path");
        options.addOption(ConsoleConstant.COMMAND_HELP, false, "help info");
        options.addOption(ConsoleConstant.COMMAND_VERSION, false, "show version info");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption(ConsoleConstant.COMMAND_HELP)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ant", options);
            System.exit(0);
        } else if (cmd.hasOption(ConsoleConstant.COMMAND_VERSION)) {
            System.out.println("version: " + ConsoleUtils.getVersion());
            System.exit(0);
        }

        ApplicationContext ac = null;
        if (cmd.hasOption(ConsoleConstant.COMMAND_CONFIG)) {
            // 初始化上下文
            ac = new ApplicationContext(cmd.getOptionValue(ConsoleConstant.COMMAND_CONFIG));
        } else {
            System.out.println("config file path arg is not found.");
            System.exit(1);
        }

        // 初始化服务线程池
        ThreadPoolExecutor serviceThreadPool = new ThreadPoolExecutor(10, 10,
                2000, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(10), new DefaultThreadFactory("service"),
                new DefaultRejectedExecutionHandler());
        // 加载mime资源
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        ThreadPoolGroup tpe = ac.getContext(ThreadPoolGroup.class);
        // 启动令牌生产
        serviceThreadPool.execute(new TokenProvider(ac));
        // 开启生存检查
        serviceThreadPool.execute(new SurvivalCheck(ac));
        // 启动http监听
        HttpListener http = new HttpListener(ac);
        serviceThreadPool.execute(http);
        // 启动https监听
        HttpsListener https = new HttpsListener(ac);
        serviceThreadPool.execute(https);
        System.out.println("gateway is running.");

        // 添加销毁事件
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            http.shutdown();
            System.out.println("http listener was shutdown!");
            https.shutdown();
            System.out.println("https listener was shutdown!");
            tpe.shutdown();
            serviceThreadPool.shutdown();
            System.out.println("gateway was shutdown!");
        }));
    }
}

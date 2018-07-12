package com.github.cwdtom.gateway;

import com.github.cwdtom.gateway.constant.ConsoleConstant;
import com.github.cwdtom.gateway.environment.ApplicationContext;
import com.github.cwdtom.gateway.limit.TokenProvider;
import com.github.cwdtom.gateway.listener.HttpListener;
import com.github.cwdtom.gateway.listener.HttpsListener;
import com.github.cwdtom.gateway.mapping.SurvivalChecker;
import com.github.cwdtom.gateway.thread.DefaultRejectedExecutionHandler;
import com.github.cwdtom.gateway.thread.DefaultThreadFactory;
import com.github.cwdtom.gateway.thread.ThreadPoolGroup;
import com.github.cwdtom.gateway.util.ConsoleUtils;
import eu.medsea.mimeutil.MimeUtil;
import org.apache.commons.cli.*;

import java.io.FileNotFoundException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * starter
 *
 * @author chenweidong
 * @since 1.0.0
 */
public class Application {
    /**
     * main
     *
     * @param args args
     * @throws Exception application exception
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
            return;
        } else if (cmd.hasOption(ConsoleConstant.COMMAND_VERSION)) {
            System.out.println("version: " + ConsoleUtils.getVersion());
            return;
        }

        ApplicationContext ac;
        if (cmd.hasOption(ConsoleConstant.COMMAND_CONFIG)) {
            // initialize application context
            ac = new ApplicationContext(cmd.getOptionValue(ConsoleConstant.COMMAND_CONFIG));
        } else {
            throw new FileNotFoundException("config file path arg is not found.");
        }

        // initialize service thread pool
        ThreadPoolExecutor serviceThreadPool = new ThreadPoolExecutor(10, 10,
                2000, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(10), new DefaultThreadFactory("service"),
                new DefaultRejectedExecutionHandler());
        // load mime resource
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        ThreadPoolGroup tpe = ac.getContext(ThreadPoolGroup.class);
        // run token provider
        serviceThreadPool.execute(new TokenProvider(ac));
        // run survival checker
        serviceThreadPool.execute(new SurvivalChecker(ac));
        // run http listener
        HttpListener http = new HttpListener(ac);
        serviceThreadPool.execute(http);
        // run https listener
        HttpsListener https = new HttpsListener(ac);
        serviceThreadPool.execute(https);
        System.out.println("gateway is running.");

        // add shutdown hook event
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

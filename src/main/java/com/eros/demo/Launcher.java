package com.eros.demo;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import com.eros.demo.utils.Configuration;
import com.eros.demo.utils.PropertiesNames;

public class Launcher {

    private static final String START_COMMAND = "start";
    private static final String STOP_COMMAND = "stop";

    public static void main(String[] args) {
        String command;
        if (args.length > 0) {
            command = args[0].toLowerCase();
        }
        else {
            command = START_COMMAND;
        }
        if (command.equals(START_COMMAND)) {
            start();
        }
        else if (command.equals(STOP_COMMAND)) {
            stop();
        }
        else {
            System.err.println("Unknown command: " + command);
            return;
        }
    }

    public static void start() {
        System.out.println("The server is starting ...");
        try {
            Configuration.loadLog4j("./conf/log4j.xml");
            Configuration.loadProperties("./conf/application.properties");
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        startJetty();
    }

    public static void stop() {
        System.out.println("The server is stopping ...");
        try {
            Configuration.loadProperties("./conf/application.properties");
            int stopPort = Configuration.getInteger(PropertiesNames.SERVER_PORT_STOP);
            Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), stopPort);
            OutputStream out = socket.getOutputStream();
            out.write("\r\n".getBytes());
            out.flush();
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private static void startJetty() {
        int httpPort = Configuration.getInteger(PropertiesNames.SERVER_PORT_HTTP);
        int stopPort = Configuration.getInteger(PropertiesNames.SERVER_PORT_STOP);
        int minThreads = Configuration.getInteger(PropertiesNames.SERVER_THREADS_MIN);
        int maxThreads = Configuration.getInteger(PropertiesNames.SERVER_THREADS_MAX);
        int acceptQueueSize = Configuration.getInteger(PropertiesNames.SERVER_ACCEPT_QUEUE_SIZE);
        int acceptors = Configuration.getInteger(PropertiesNames.SERVER_ACCEPTORS);
        int maxIdleTime = Configuration.getInteger(PropertiesNames.SERVER_MAX_IDLE_TIME);

        JettyServer server = new JettyServer(httpPort, stopPort);
        server.setMinThreads(minThreads)
              .setMaxThreads(maxThreads)
              .setAcceptQueueSize(acceptQueueSize)
              .setAcceptors(acceptors)
              .setMaxIdleTime(maxIdleTime)
              .start();
    }
}

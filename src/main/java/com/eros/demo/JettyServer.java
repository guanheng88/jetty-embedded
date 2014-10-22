package com.eros.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyServer {

    private final int HTTP_PORT;
    private final int STOP_PORT;
    private int minThreads = 10;
    private int maxThreads = 100;
    private int acceptQueueSize = 50;
    private int acceptors = 1;
    private int maxIdleTime = 20000;

    private Server jettyServer;

    public JettyServer(int httpPort, int stopPort) {
        this.HTTP_PORT = httpPort;
        this.STOP_PORT = stopPort;
    }

    public int getMinThreads() {
        return minThreads;
    }

    public JettyServer setMinThreads(int minThreads) {
        this.minThreads = minThreads;
        return this;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public JettyServer setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        return this;
    }

    public int getAcceptQueueSize() {
        return acceptQueueSize;
    }

    public JettyServer setAcceptQueueSize(int acceptQueueSize) {
        this.acceptQueueSize = acceptQueueSize;
        return this;
    }

    public int getAcceptors() {
        return acceptors;
    }

    public JettyServer setAcceptors(int acceptors) {
        this.acceptors = acceptors;
        return this;
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public JettyServer setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
        return this;
    }

    public void start() {
        jettyServer = createJettyServer();
        try {
            jettyServer.start();
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Starts the stop signal monitor thread.
        try {
            new MonitorThread().start();
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Started, the server is running on http://localhost:" + HTTP_PORT);
        // If doesn't "join" here, the end of main thread won't cause program
        // exit.
        // But, we should do.
        try {
            jettyServer.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
    }

    private Server createJettyServer() {
        Server server = new Server();
        // Sets the hook for JVM termination.
        server.setStopAtShutdown(true);

        server.setThreadPool(createThreadPool());
        server.addConnector(createConnection());

        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase("./webapp");
        context.setDescriptor("./webapp/WEB-INF/web.xml");
        context.setConfigurationDiscovered(true);
        context.setParentLoaderPriority(true);
        server.setHandler(context);
        return server;
    }

    private ThreadPool createThreadPool() {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(minThreads);
        threadPool.setMaxThreads(maxThreads);
        return threadPool;
    }

    private Connector createConnection() {
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(HTTP_PORT);
        connector.setAcceptQueueSize(acceptQueueSize);
        connector.setAcceptors(acceptors);
        connector.setMaxIdleTime(maxIdleTime);

        return connector;
    }

    /**
     * Implements a graceful shutdown for jetty. Occupies a port to listen stop
     * signal, then shutdown server.
     */
    private class MonitorThread extends Thread {

        private ServerSocket socket;

        public MonitorThread() {
            setDaemon(true);
            setName("StopMonitor");
            try {
                socket = new ServerSocket(STOP_PORT, 1, InetAddress.getByName("127.0.0.1"));
            }
            catch (Exception e) {
                System.exit(-1);
            }
        }

        @Override
        public void run() {
            Socket accept;
            try {
                accept = socket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
                reader.readLine();
                // Leaves 10 seconds for cleaning up executing requests.
                jettyServer.setGracefulShutdown(10000);
                accept.close();
                reader.close();
                socket.close();
                System.out.println("Server is stopped.");
            }
            catch (Exception e) {
                System.exit(-1);
            }
            System.exit(0);
        }
    }
}

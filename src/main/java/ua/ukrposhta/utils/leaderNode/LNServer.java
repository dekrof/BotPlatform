package ua.ukrposhta.utils.leaderNode;

import ua.ukrposhta.utils.types.LoggerType;
import ua.ukrposhta.utils.Loggers.BotLogger;
import ua.ukrposhta.utils.ShkiMonitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class LNServer extends Thread {
    private BotLogger log = BotLogger.getLogger(LoggerType.MONITOR);
    private ServerSocket server;
    private static LNServer instance;

    private LNServer() {
        int port = Integer.parseInt(System.getProperty("monitor.port"));
        try {
            this.log.info("Trying to start Leader Node server..");
            server = new ServerSocket(port, 10);
            log.info("Leader node server started on port " + port);
        } catch (IOException e) {
            log.error("Failed to start Leader Node server. " + e);
        }
    }

    public static Thread getInstance() {
        if (instance == null) {
            instance = new LNServer();
        }
        return instance;
    }

    public synchronized void run() {
        ShkiMonitor monitor = ShkiMonitor.getInstance();
        Socket connection;
        while (!isInterrupted()) {
            try {
                log.info("Waiting for status request..");
                connection = server.accept();
                log.info(connection.getRemoteSocketAddress().toString() + " requested status.");

                PrintWriter out = new PrintWriter(connection.getOutputStream(), true);

                String status = monitor.isActive() ? "active" : "inactive";
                out.println(status);
                log.info("Sent current status (" + status + ") to " + connection.getRemoteSocketAddress().toString());
                connection.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
        try {
            log.info("Closing leader node server socket");
            server.close();
            log.info("Closed leader node server socket");
        } catch (NullPointerException | IOException nioe) {
            log.error("Could not close leader node server socket.");
            log.error(nioe);
        }
        log.info("Leader node server was stopped.");
    }
}

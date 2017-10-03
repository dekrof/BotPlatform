package ua.ukrposhta.utils.leaderNode;

import ua.ukrposhta.utils.types.LoggerType;
import ua.ukrposhta.utils.Loggers.BotLogger;
import ua.ukrposhta.utils.ShkiMonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class LNClient extends Thread {
    private BotLogger log = BotLogger.getLogger(LoggerType.MONITOR);
    List<String> monitorInstances;
    private static LNClient instance;

    private LNClient() {
        this.monitorInstances = new LinkedList<>(Arrays.asList(System.getProperty("monitor.instances").split(";")));
    }

    public static Thread getInstance() {
        if (instance == null) {
            instance = new LNClient();
        }
        return instance;
    }

    @Override
    public synchronized void run() {
        log.info("Leader node client started.");
        int sleepTime = Integer.parseInt(System.getProperty("monitor.LNScan.period"));
        log.info("Scan period set to " + sleepTime + " seconds.");
        while (!this.isInterrupted()) {
            if (!ShkiMonitor.getInstance().isActive()) {
                boolean foundActive = monitorInstances.stream().anyMatch(instance -> requestStatus(instance).equals("active"));
                if (!foundActive) {
                    ShkiMonitor.getInstance().start();
                }
            }
            try {
                Thread.sleep(sleepTime * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        requestStatus("localhost");
        log.info("Leader node client was stopped.");
    }

    private String requestStatus(String instance) {
        log.info("Requesting status from " + instance);
        String result = "";
        try {
            String host = instance.split(":")[0];
            int port = Integer.parseInt(System.getProperty("monitor.port"));
            try {
                Socket socket = new Socket(host, port);
                BufferedReader input =
                        new BufferedReader(new InputStreamReader(socket.getInputStream()));
                result = input.readLine();
                log.info(instance + " returned status: " + result);
            } catch (ConnectException e) {
                log.error("Instance " + instance + " is not reachable.");
            }

        } catch (IOException e) {
            log.error(e);
        }
        return result;
    }
}

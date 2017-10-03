package ua.ukrposhta.utils;

import ua.ukrposhta.bot.FacebookBot;
import ua.ukrposhta.bot.TelegramBot;
import ua.ukrposhta.bot.ViberBot;
import ua.ukrposhta.dao.MonitorDao;
import ua.ukrposhta.entities.ShkiMonitoring;
import ua.ukrposhta.models.PackageStatus;
import ua.ukrposhta.utils.Loggers.BotLogger;
import ua.ukrposhta.utils.types.BotType;
import ua.ukrposhta.utils.types.LoggerType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShkiMonitor extends Thread {
    private BotLogger log = BotLogger.getLogger(LoggerType.MONITOR);
    private boolean active;
    private static ShkiMonitor instance;
    private Set<String> finalStatuses;

    private ShkiMonitor() {
        active = false;
        finalStatuses = new HashSet<>(Arrays.asList(System.getProperty("package.finalStatuses").split(",")));
    }

    public static ShkiMonitor getInstance() {
        if (instance == null) {
            instance = new ShkiMonitor();
        }
        return instance;
    }

    @Override
    public synchronized void run() {
        active = true;
        log.info("Started shki monitor on port " + System.getProperty("monitor.port"));
        while (!this.isInterrupted()) {
            try {
                MonitorDao monitor = new MonitorDao();
                log.info("Requesting all records from shki_monitoring table.");
                List<ShkiMonitoring> barcodeList = monitor.getAll();
                log.info("Received " + barcodeList.size() + " records.");
                for (ShkiMonitoring barcode : barcodeList) {
                    log.info("Requesting barcode " + barcode.getBarcode() + " status for user " + barcode.getUserId());
                    PackageStatus packageStatus = TelegramBot.getInstance().requestBarcodeStatus(barcode.getBarcode(),
                            barcode.getUserId(), log);

                    if (packageStatus != null) {
                        if (!packageStatus.getEventName().equalsIgnoreCase(barcode.getStatus())) {
                            log.info("Barcode " + barcode.getBarcode() + " status has changed("
                                    + barcode.getStatus() + " -> " + packageStatus.getEventName() + ").");
                            PackageStatus previousPackageStatus = TelegramBot.getInstance().requestBarcodePreviousStatus(barcode.getBarcode(), log);

                            if (!finalStatuses.contains(packageStatus.getEvent())) {
                                log.info("Updating barcode " + barcode.getBarcode() + " status in the shki_monitoring table.");
                                monitor.updateStatus(barcode.getBarcode(), barcode.getUserId(), packageStatus.getEventName());
                            } else {
                                log.info("Removing barcode " + barcode.getBarcode()
                                        + " from the shki_monitoring table as it has the final status: "
                                        + packageStatus.getEvent()
                                        + " - "
                                        + packageStatus.getEventName());
                                monitor.removeBarcode(barcode);
                            }

                            log.info("Sending update on barcode " + barcode.getBarcode() + " to the user " + barcode.getUserId());
                            if (barcode.getBotType().equalsIgnoreCase(BotType.TELEGRAM.getName())) {
                                try {
                                    TelegramBot.getInstance().sendBarcodeUpdate(barcode.getUserId(), packageStatus, previousPackageStatus);
                                } catch (Exception e) {
                                    log.error(e);
                                }
                            }
                            if (barcode.getBotType().equalsIgnoreCase(BotType.FACEBOOK.getName())) {
                                try {
                                    FacebookBot.getInstance().sendBarcodeUpdate(barcode.getUserId(), packageStatus, previousPackageStatus);
                                } catch (Exception e) {
                                    log.error(e);
                                }
                            }
                            if (barcode.getBotType().equalsIgnoreCase(BotType.VIBER.getName())) {
                                try {
                                    ViberBot.getInstance().sendBarcodeUpdate(barcode.getUserId(), packageStatus, previousPackageStatus);
                                } catch (Exception e) {
                                    log.error(e);
                                }
                            }
                        } else {
                            log.info("Barcode " + barcode.getBarcode() + " has not changed for user " + barcode.getUserId());
                        }
                    } else {
                        log.warn("Barcode " + barcode.getBarcode() + " doesn't exist for user " + barcode.getUserId());
                    }
                }
                Thread.sleep(600000);
            } catch (InterruptedException e) {
                log.error(e);
                break;
            }
        }
        this.log.info("Shki monitor was stopped.");
    }

    public boolean isActive() {
        return active;
    }
}

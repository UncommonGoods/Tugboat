// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.service;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import com.uncommongoods.tugboat.bridge.SettingsController;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

public class ScaleService {

    private static final String DEVICE_PREFS_PREFIX = "tugboat.device.";
    private static final Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);

    private static ScaleService instance;
    private final Object portLock = new Object();

    private Timer scaleTimer;
    private String currentScaleWeight = "--";
    private String lastValidWeight = "--";
    private Consumer<String> weightUpdateCallback;

    private ScaleService() {
    }

    public static ScaleService getInstance() {
        if (instance == null) {
            instance = new ScaleService();
        }
        return instance;
    }

    public void startMonitoring(Consumer<String> onWeightUpdate) {
        this.weightUpdateCallback = onWeightUpdate;

        if (scaleTimer != null) {
            scaleTimer.cancel();
        }

        scaleTimer = new Timer(true);
        scaleTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateScaleWeight();
            }
        }, 0, 200);
    }

    public void stopMonitoring() {
        if (scaleTimer != null) {
            scaleTimer.cancel();
            scaleTimer = null;
        }
    }

    public String getCurrentWeight() {
        return currentScaleWeight;
    }

    public boolean isScaleConfigured() {
        return !prefs.get(DEVICE_PREFS_PREFIX + "scale.port", "").isEmpty();
    }

    public String weighOnce() {
        String scalePort = prefs.get(DEVICE_PREFS_PREFIX + "scale.port", "");
        if (scalePort.isEmpty()) {
            return "--";
        }
        return getScaleReading(scalePort);
    }

    public String weighOnce(String portSelection) {
        if (portSelection == null || portSelection.isEmpty()) {
            return "--";
        }
        return getScaleReading(portSelection);
    }

    public void zeroScale(String portSelection) {
        if (portSelection == null || portSelection.isEmpty()) {
            return;
        }
        sendScaleCommand(portSelection, "Z\r".getBytes());
    }

    private void updateScaleWeight() {
        String scalePort = prefs.get(DEVICE_PREFS_PREFIX + "scale.port", "");
        if (!scalePort.isEmpty()) {
            try {
                String reading = getScaleReading(scalePort);
                if (!reading.equals(currentScaleWeight)) {
                    currentScaleWeight = reading;
                    if (weightUpdateCallback != null) {
                        Platform.runLater(() -> weightUpdateCallback.accept(currentScaleWeight));
                    }
                }
            } catch (Exception e) {
                // Silently handle scale reading errors to avoid spam
                if (!currentScaleWeight.equals("--")) {
                    currentScaleWeight = "--";
                    if (weightUpdateCallback != null) {
                        Platform.runLater(() -> weightUpdateCallback.accept(currentScaleWeight));
                    }
                }
            }
        }
    }

    private String getScaleReading(String portSelection) {
        synchronized (portLock) {
            try {
                String portName = portSelection.split(" - ")[0];
                SerialPort scalePort = SerialPort.getCommPort(portName);

                scalePort.setBaudRate(9600);
                scalePort.setNumDataBits(7);
                scalePort.setParity(SerialPort.EVEN_PARITY);
                scalePort.setNumStopBits(SerialPort.ONE_STOP_BIT);
                scalePort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
                scalePort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 300, 0);

                if (scalePort.openPort()) {
                    byte[] command = "W\r".getBytes();
                    int bytesWritten = scalePort.writeBytes(command, command.length);

                    if (bytesWritten > 0) {
                        byte[] readBuffer = new byte[100];
                        int numRead = scalePort.readBytes(readBuffer, readBuffer.length);

                        scalePort.closePort();

                        if (numRead > 0) {
                            String response = new String(readBuffer, 0, numRead).trim();
                            return parseScaleReading(response);
                        }
                    }
                    scalePort.closePort();
                }
            } catch (Exception e) {
                // Return error indicator
            }
            return "--";
        }
    }

    private String parseScaleReading(String response) {
        if (response == null || response.isEmpty()) {
            return lastValidWeight;
        }

        String[] lines = response.trim().split("\\r\\n");
        if (lines.length > 0) {
            String weightLine = lines[0];

            if (weightLine.trim().startsWith("?")) {
                return lastValidWeight;
            }

            String result;
            if (weightLine.contains("lb")) {
                result = weightLine.replace("lb", "").trim();
            } else if (weightLine.contains("kg")) {
                try {
                    double kg = Double.parseDouble(weightLine.replace("kg", "").trim());
                    double lb = kg * 2.20462;
                    result = String.format("%.2f", lb);
                } catch (NumberFormatException e) {
                    result = weightLine.replace("kg", "").trim() + " (kg)";
                }
            } else if (weightLine.contains("oz")) {
                try {
                    double oz = Double.parseDouble(weightLine.replace("oz", "").trim());
                    double lb = oz / 16.0;
                    result = String.format("%.2f", lb);
                } catch (NumberFormatException e) {
                    result = weightLine.replace("oz", "").trim() + " (oz)";
                }
            } else if (weightLine.contains("g")) {
                try {
                    double g = Double.parseDouble(weightLine.replace("g", "").trim());
                    double lb = g / 453.592;
                    result = String.format("%.2f", lb);
                } catch (NumberFormatException e) {
                    result = weightLine.replace("g", "").trim() + " (g)";
                }
            } else {
                result = weightLine.trim();
            }

            // Update last valid weight if this is a valid reading
            if (!result.equals("--") && !result.equals("?")) {
                lastValidWeight = result;
            }

            return result;
        }
        return lastValidWeight;
    }

    private void sendScaleCommand(String portSelection, byte[] command) {
        synchronized (portLock) {
            try {
                String portName = portSelection.split(" - ")[0];
                SerialPort scalePort = SerialPort.getCommPort(portName);

                scalePort.setBaudRate(9600);
                scalePort.setNumDataBits(7);
                scalePort.setParity(SerialPort.EVEN_PARITY);
                scalePort.setNumStopBits(SerialPort.ONE_STOP_BIT);
                scalePort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
                scalePort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 300, 0);

                if (scalePort.openPort()) {
                    scalePort.writeBytes(command, command.length);
                    Thread.sleep(200);
                    scalePort.closePort();
                }
            } catch (Exception e) {
                // fail silently
            }
        }
    }
}

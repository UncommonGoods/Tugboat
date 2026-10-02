// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.service;

import javafx.application.Platform;
import com.uncommongoods.tugboat.engine.Tugboat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ShipmentHistoryStore {

    private static final int MAX_SIZE = 100;

    private final LinkedList<Tugboat> history = new LinkedList<>();
    private final Map<String, Runnable> listeners = new LinkedHashMap<>();

    private ShipmentHistoryStore() {}

    private static final class InstanceHolder {
        private static final ShipmentHistoryStore instance = new ShipmentHistoryStore();
    }

    public static ShipmentHistoryStore getInstance() {
        return InstanceHolder.instance;
    }

    public synchronized void add(Tugboat tugboat) {
        history.removeIf(t -> t.getCargoId().equals(tugboat.getCargoId()));
        history.addFirst(tugboat);
        if (history.size() > MAX_SIZE) {
            history.removeLast();
        }
        notifyListeners();
    }

    public synchronized void addIfAbsent(Tugboat tugboat) {
        boolean exists = history.stream()
                .anyMatch(t -> t.getCargoId().equals(tugboat.getCargoId()));
        if (!exists) {
            history.addFirst(tugboat);
            if (history.size() > MAX_SIZE) {
                history.removeLast();
            }
            notifyListeners();
        }
    }

    public synchronized List<Tugboat> getRecent(int n) {
        return new ArrayList<>(history.subList(0, Math.min(n, history.size())));
    }

    public synchronized List<Tugboat> getAll() {
        return new ArrayList<>(history);
    }

    public synchronized void clear() {
        history.clear();
        notifyListeners();
    }

    public void addListener(String key, Runnable listener) {
        listeners.put(key, listener);
    }

    private void notifyListeners() {
        for (Runnable listener : listeners.values()) {
            Platform.runLater(listener);
        }
    }
}

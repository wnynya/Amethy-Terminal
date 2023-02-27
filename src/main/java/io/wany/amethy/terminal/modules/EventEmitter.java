package io.wany.amethy.terminal.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class EventEmitter {
  private final HashMap<String, List<Consumer<Object[]>>> listeners;
  private final HashMap<String, List<Consumer<Object[]>>> onceListeners;

  public EventEmitter() {
    this.listeners = new HashMap<>();
    this.onceListeners = new HashMap<>();
  }

  public EventEmitter on(String event, Consumer<Object[]> callback) {
    if (!this.listeners.keySet().contains(event)) {
      this.listeners.put(event, new ArrayList<>());
    }
    this.listeners.get(event).add(callback);
    return this;
  }

  public EventEmitter once(String event, Consumer<Object[]> callback) {
    if (!this.onceListeners.keySet().contains(event)) {
      this.onceListeners.put(event, new ArrayList<>());
    }
    this.onceListeners.get(event).add(callback);
    return this;
  }

  public EventEmitter emit(String event, Object... args) {
    if (this.listeners.keySet().contains(event)) {
      this.listeners.get(event).forEach(callback -> {
        callback.accept(args);
      });
    }
    if (this.onceListeners.keySet().contains(event)) {
      this.onceListeners.get(event).forEach(callback -> {
        callback.accept(args);
      });
      this.onceListeners.remove(event);
    }
    return this;
  }

}

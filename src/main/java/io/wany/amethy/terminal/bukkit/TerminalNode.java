package io.wany.amethy.terminal.bukkit;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import io.wany.amethy.terminal.bukkit.panels.console.TerminalConsole;
import io.wany.amethy.terminal.bukkit.panels.dashboard.TerminalDashboard;
import io.wany.amethy.terminal.bukkit.panels.filesystem.TerminalFilesystem;
import io.wany.amethy.terminal.bukkit.panels.players.TerminalPlayers;
import io.wany.amethy.terminal.bukkit.panels.worlds.TerminalWorlds;
import io.wany.amethyst.EventEmitter;
import io.wany.amethyst.Json;
import io.wany.amethyst.network.WebSocketClient;
import io.wany.amethyst.network.WebSocketClientOptions;

public class TerminalNode {

  protected static final String API = "api.wany.io/amethy/terminal/nodes";

  protected static WebSocketClient WEBSOCKET;
  protected static boolean OPENED = false;
  protected static boolean DISABLED = false;

  private static ExecutorService onLoadExecutor = Executors.newFixedThreadPool(1);
  private static ExecutorService onEnableExecutor = Executors.newFixedThreadPool(1);
  private static ExecutorService onDisableExecutor = Executors.newFixedThreadPool(1);
  private static EventEmitter eventEmitter = new EventEmitter();

  protected static void onLoad() {
    onLoadExecutor.submit(() -> {
      TerminalConsole.onLoad();

      loadNode();
    });
  }

  protected static void onEnable() {
    onEnableExecutor.submit(() -> {
      TerminalDashboard.onEnable();
      TerminalFilesystem.onEnable();
      TerminalPlayers.onEnable();
      TerminalWorlds.onEnable();
    });
  }

  protected static void onDisable() {
    onDisableExecutor.submit(() -> {
      DISABLED = true;

      WEBSOCKET.close();
      WEBSOCKET.disable();
      onLoadExecutor.shutdown();

      TerminalDashboard.onDisable();
      TerminalConsole.onDisable();
      TerminalFilesystem.onDisable();
      TerminalPlayers.onDisable();
      TerminalWorlds.onDisable();

      onDisableExecutor.shutdown();
      onEnableExecutor.shutdown();
    });
  }

  protected static void loadNode() {

    if (DISABLED) {
      return;
    }

    // API ?????? ?????? ??????
    if (!TerminalNodeAPI.ping()) {
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException ignored) {
      }
      loadNode();
      return;
    }
    Console.debug("API server ping checked");

    // ????????? UID, KEY ????????????
    AmethyTerminal.UID = AmethyTerminal.CONFIG.getString("uid");
    AmethyTerminal.KEY = AmethyTerminal.CONFIG.getString("key");

    // ?????? ????????? ???????????? ??????
    if (!TerminalNodeAPI.isValidNode()) {
      Console.debug("Node validation failed");
      // ????????? ??? ?????? ???????????? ??? UID, KEY ?????? ??? ??????
      TerminalNodeAPI.newNode();
      Console.debug("Issue new node");
      loadNode();
      return;
    }
    Console.debug("Node validation success");

    // ????????? ?????? ??????
    WebSocketClientOptions options = new WebSocketClientOptions();
    options.AUTO_RECONNECT = true;
    options.HEADERS.put("amethy-terminal-node-nid", AmethyTerminal.UID);
    options.HEADERS.put("amethy-terminal-node-key", AmethyTerminal.KEY);

    try {
      WEBSOCKET = new WebSocketClient(new URI("wss://" + API), options);
    } catch (Exception ignored) {
    }

    // ?????? ??????
    WEBSOCKET.on("open", (args) -> {
      OPENED = true;
      Console.debug("Connection opened");

      TerminalDashboard.sendSystemInfo();
    });

    // JSON ?????????
    WEBSOCKET.on("json", (args) -> {
      String event = args[1].toString();
      Json data = (Json) args[2];
      eventEmitter.emit(event, data);
    });

    // ?????? ??????
    WEBSOCKET.on("close", (args) -> {
      OPENED = false;
      Console.debug("Connection closed");
    });

    // ?????? ??????
    WEBSOCKET.on("failed", (args) -> {
      WEBSOCKET.close();
      WEBSOCKET.disable();
      Console.debug("Connection Failed");
      loadNode();
    });

    try {
      WEBSOCKET.open();
    } catch (Exception ignored) {
    }

  }

  public static void on(String event, BiConsumer<Json, Json> callback) {
    Console.debug("Event listener registered: " + event);
    eventEmitter.on(event, (args) -> {
      Json object = (Json) args[0];
      Json client = object.get("client");
      Json data = object.get("data");
      callback.accept(client, data);
    });
  }

  public static void event(String event, Json client, Json data) {
    if (!isOpened()) {
      return;
    }
    Json obj = new Json();
    obj.set("client", client);
    obj.set("data", data);
    WEBSOCKET.event(event, obj);
  }

  public static void event(String event, Json data) {
    event(event, new Json(), data);
  }

  public static boolean isOpened() {
    return OPENED;
  }

}

package io.wany.amethy.terminal.bukkit;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import io.wany.amethy.terminal.bukkit.panels.console.TerminalConsole;
import io.wany.amethy.terminal.bukkit.panels.dashboard.TerminalDashboard;
import io.wany.amethy.terminal.bukkit.panels.filesystem.TerminalFilesystem;
import io.wany.amethy.terminal.bukkit.modules.EventEmitter;
import io.wany.amethy.terminal.bukkit.modules.Json;
import io.wany.amethy.terminal.bukkit.modules.network.WebSocketClient;
import io.wany.amethy.terminal.bukkit.modules.network.WebSocketClientOptions;

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

      onDisableExecutor.shutdown();
      onEnableExecutor.shutdown();
    });
  }

  protected static void loadNode() {

    if (DISABLED) {
      return;
    }

    // API 연결 상태 확인
    if (!TerminalNodeAPI.ping()) {
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException ignored) {
      }
      loadNode();
      return;
    }
    Console.debug("API server ping checked");

    // 저장된 UID, KEY 가져오기
    AmethyTerminal.UID = AmethyTerminal.CONFIG.getString("uid");
    AmethyTerminal.KEY = AmethyTerminal.CONFIG.getString("key");

    // 사용 가능한 노드인지 확인
    if (!TerminalNodeAPI.isValidNode()) {
      Console.debug("Node validation failed");
      // 사용할 수 없는 노드이면 새 UID, KEY 발급 후 저장
      TerminalNodeAPI.newNode();
      Console.debug("Issue new node");
      loadNode();
      return;
    }
    Console.debug("Node validation success");

    // 웹소켓 연결 설정
    WebSocketClientOptions options = new WebSocketClientOptions();
    options.AUTO_RECONNECT = true;
    options.HEADERS.put("amethy-terminal-node-nid", AmethyTerminal.UID);
    options.HEADERS.put("amethy-terminal-node-key", AmethyTerminal.KEY);

    try {
      WEBSOCKET = new WebSocketClient(new URI("wss://" + API), options);
    } catch (Exception ignored) {
    }

    // 연결 수립
    WEBSOCKET.on("open", (args) -> {
      OPENED = true;
      Console.debug("Connection opened");

      TerminalDashboard.sendSystemInfo();
    });

    // JSON 이벤트
    WEBSOCKET.on("json", (args) -> {
      String event = args[1].toString();
      Json data = (Json) args[2];
      eventEmitter.emit(event, data);
    });

    // 연결 종료
    WEBSOCKET.on("close", (args) -> {
      OPENED = false;
      Console.debug("Connection closed");
    });

    // 연결 실패
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

package io.wany.amethy.terminal.bukkit;

import io.wany.amethy.terminal.bukkit.panels.console.TerminalConsole;
import io.wany.amethy.terminal.bukkit.panels.dashboard.TerminalDashboard;
import io.wany.amethy.terminal.bukkit.panels.filesystem.TerminalFilesystem;
import io.wany.amethy.terminal.bukkit.panels.players.TerminalPlayers;
import io.wany.amethy.terminal.bukkit.panels.worlds.TerminalWorlds;
import io.wany.amethyst.EventEmitter;
import io.wany.amethyst.Json;
import io.wany.amethyst.network.WebSocketClient;
import io.wany.amethyst.network.WebSocketClientOptions;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class TerminalNode {

  protected static final String API = "api.wany.io/amethy/terminal/nodes";

  protected static WebSocketClient WEBSOCKET;
  protected static boolean OPENED = false;
  protected static boolean DISABLED = false;
  protected static boolean FAILED = true;

  private static final ExecutorService onLoadExecutor = Executors.newFixedThreadPool(1);
  private static final Timer onLoadTimer = new Timer();
  private static final ExecutorService onEnableExecutor = Executors.newFixedThreadPool(1);
  private static final ExecutorService onDisableExecutor = Executors.newFixedThreadPool(1);
  private static final EventEmitter eventEmitter = new EventEmitter();

  protected static void onLoad() {
    onLoadExecutor.submit(() -> {
      TerminalConsole.onLoad();

      onLoadTimer.schedule(new TimerTask() {
        @Override
        public void run() {
          if (!OPENED && !DISABLED && FAILED) {
            open();
          }
        }
      }, 0, 2000);
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

  private static void open() {
    if (OPENED || DISABLED) {
      return;
    }

    // API 연결 상태 확인
    if (!TerminalNodeAPI.ping()) {
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException ignored) {
      }
      return;
    }
    console.debug("API server ping checked");

    // 저장된 UID, KEY 가져오기
    AmethyTerminal.UID = AmethyTerminal.CONFIG.getString("uid");
    AmethyTerminal.KEY = AmethyTerminal.CONFIG.getString("key");

    // 사용 가능한 노드인지 확인
    if (!TerminalNodeAPI.isValidNode()) {
      console.debug("Node validation failed");
      // 사용할 수 없는 노드이면 새 UID, KEY 발급 후 저장
      TerminalNodeAPI.newNode();
      console.debug("Issue new node");
      return;
    }
    console.debug("Node validation success");

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
      console.debug("Connection opened");

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
      console.debug("Connection closed");
    });

    // 연결 실패
    WEBSOCKET.on("failed", (args) -> {
      WEBSOCKET.close();
      WEBSOCKET.disable();
      FAILED = true;
      console.debug("Connection Failed");
    });

    try {
      FAILED = false;
      WEBSOCKET.open();
    } catch (Exception ignored) {
    }

  }

  public static void on(String event, BiConsumer<Json, Json> callback) {
    console.debug("Event listener registered: " + event);
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

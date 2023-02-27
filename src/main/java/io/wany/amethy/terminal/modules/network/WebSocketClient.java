package io.wany.amethy.terminal.modules.network;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Builder;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.wany.amethy.terminal.modules.EventEmitter;
import io.wany.amethy.terminal.modules.Json;

public class WebSocketClient extends EventEmitter {

  public static String USER_AGENT = System.getProperty("java.runtime.name");

  private final URI uri;
  private final WebSocketClientOptions opts;

  private WebSocket conn = null;
  private boolean connected = false;
  private boolean closed = true;

  private final ExecutorService pingES = Executors.newFixedThreadPool(1);
  private final Timer pingT = new Timer();

  private final ExecutorService reconES = Executors.newFixedThreadPool(1);
  private final Timer reconT = new Timer();

  public int openFailed = 0;

  public WebSocketClient(URI uri, WebSocketClientOptions opts) {
    super();

    this.uri = uri;
    this.opts = opts;

    if (!this.opts.HEADERS.containsKey("User-Agent")) {
      this.opts.HEADERS.replace("User-Agent", USER_AGENT);
    }

    pingES.submit(() -> pingT.schedule(new TimerTask() {
      @Override
      public void run() {
        if (closed || connected || conn != null) {
          return;
        }
        try {
          conn.sendPing(ByteBuffer.allocate(0));
        } catch (Exception ignored) {
        }
      }
    }, 3000, 1000 * 30));

    reconES.submit(() -> reconT.schedule(new TimerTask() {
      @Override
      public void run() {
        if (!opts.AUTO_RECONNECT || closed || connected || conn != null) {
          return;
        }
        try {
          open();
        } catch (Exception e) {
          openFailed++;
          if (openFailed >= 3) {
            emit("failed", "");
            openFailed = 0;
            close();
          }
        }
      }
    }, 3000, 1000));
  }

  public void open() {
    if (this.conn != null || this.connected) {
      return;
    }
    this.closed = false;

    Builder builder = HttpClient.newHttpClient().newWebSocketBuilder();
    this.opts.HEADERS.forEach((key, value) -> {
      builder.header(key, value);
    });
    builder.connectTimeout(Duration.ofMillis(1000));
    this.conn = builder.buildAsync(this.uri, new WebSocketListener(this)).join();
  }

  private class WebSocketListener implements WebSocket.Listener {

    private final WebSocketClient client;
    private StringBuilder message = new StringBuilder();

    public WebSocketListener(WebSocketClient client) {
      this.client = client;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
      client.connected = true;
      client.conn = webSocket;
      openFailed = 0;
      client.emit("open", client);
      WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence chs, boolean last) {
      if (last) {
        message.append(chs);
        String string = message.toString();
        client.emit("message", webSocket, string);
        try {
          Json object = new Json(string);
          String event = object.getString("event");
          Json data = object.get("data");
          String message = object.getString("message");
          client.emit("json", client, event, data, message);
          client.emit("text", client, string);
        } catch (Exception e) {
          client.emit("text", client, string);
        }
        message = new StringBuilder();
      } else {
        message.append(chs);
      }
      return WebSocket.Listener.super.onText(webSocket, chs, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
      client.connected = false;
      client.conn.sendClose(0, "");
      client.conn.abort();
      client.conn = null;
      client.emit("close", client, statusCode, reason);
      return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
      client.connected = false;
      client.conn.sendClose(0, "");
      client.conn.abort();
      client.conn = null;
      client.emit("close", client, error);
      WebSocket.Listener.super.onError(webSocket, error);
    }

  }

  public void close() {
    this.closed = true;
    if (this.conn == null) {
      return;
    }
    this.conn.sendClose(0, "");
    this.conn.abort();
    this.conn = null;
  }

  public void send(Object object) {
    if (!this.isConnected()) {
      return;
    }
    String string = null;
    if (object instanceof Json) {
      string = object.toString();
    } else {
      string = object.toString();
    }
    try {
      this.conn.sendText(string, true);
    } catch (Exception ignored) {
    }
  }

  public void event(String event, Json data, String message) {
    Json object = new Json();
    object.set("event", event);
    object.set("data", data);
    object.set("message", message);
    this.send(object);
  }

  public void event(String event, Json data) {
    event(event, data, "");
  }

  public void event(String event) {
    event(event, new Json(), "");
  }

  public boolean isConnected() {
    return connected;
  }

  public boolean isClosed() {
    return closed;
  }

  public void disable() {
    this.reconT.cancel();
    this.reconES.shutdown();
  }
}

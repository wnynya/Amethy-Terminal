package io.wany.amethy.terminal.bukkit;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import io.wany.amethyst.Json;
import io.wany.amethyst.network.HTTPRequest;
import io.wany.amethyst.network.HTTPRequestOptions;
import io.wany.amethyst.network.HTTPRequestOptions.Method;
import io.wany.amethyst.network.HTTPRequestOptions.ResponseType;

public class TerminalNodeAPI {

  private static Json syncJsonRequest(Method method, String url, String body)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    HTTPRequestOptions options = new HTTPRequestOptions(method, ResponseType.JSON);
    options.HEADERS.put("amethy-terminal-node-key", AmethyTerminal.KEY);
    options.HEADERS.put("Content-Type", "application/Json");
    return (Json) HTTPRequest.syncRequest(new URL("https://" + TerminalNode.API + "/" + url), options, body);
  }

  private static Json JsonGet(String url)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.GET, url, null);
  }

  private static Json JsonPost(String url, Json body)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.POST, url, body.toString());
  }

  protected static boolean ping() {
    try {
      Json res = JsonGet("ping");
      String pong = res.getJsonElement("message").getAsString();
      if (pong.equals("pong!")) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

  protected static boolean isValidNode() {
    try {
      JsonGet(AmethyTerminal.UID + "/check");
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  protected static boolean newNode() {
    try {
      Json body = new Json();
      body.set("type", "bukkit");
      Json res = JsonPost("", body);
      console.debug(res.getString("data.uid"));
      console.debug(res.getString("data.key"));
      AmethyTerminal.UID = res.getString("data.uid");
      AmethyTerminal.KEY = res.getString("data.key");
      AmethyTerminal.CONFIG.set("uid", AmethyTerminal.UID);
      AmethyTerminal.CONFIG.set("key", AmethyTerminal.KEY);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static boolean grant(String aid) {
    try {
      Json body = new Json();
      body.set("owner", aid);
      JsonPost(AmethyTerminal.UID + "/grant", body);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}

package io.wany.amethy.terminal.modules.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import io.wany.amethy.terminal.modules.EventEmitter;
import io.wany.amethy.terminal.modules.Json;
import io.wany.amethy.terminal.modules.network.HTTPRequestOptions.Method;
import io.wany.amethy.terminal.modules.network.HTTPRequestOptions.ResponseType;

public class HTTPRequest extends EventEmitter {

  public static String USER_AGENT = System.getProperty("java.runtime.name");

  private final URL url;
  private final HTTPRequestOptions opts;
  private final String body;

  private HttpURLConnection req;
  private CompletableFuture<Object> future;

  public HTTPRequest(URL url, HTTPRequestOptions opts, String body) {
    super();

    this.url = url;
    this.opts = opts;
    this.body = body;

    if (!this.opts.HEADERS.containsKey("User-Agent")) {
      this.opts.HEADERS.replace("User-Agent", USER_AGENT);
    }
  }

  public CompletableFuture<Object> future() throws IOException {
    future = new CompletableFuture<Object>();
    return future;
  }

  public void send() throws IOException {
    this.req = (HttpURLConnection) this.url.openConnection();
    this.req.setRequestMethod(this.opts.METHOD.toString());
    this.opts.HEADERS.forEach((key, value) -> {
      if (value != null) {
        this.req.setRequestProperty(key, value);
      }
    });
    this.req.setConnectTimeout(this.opts.TIMEOUT);

    if (List.of(Method.POST, Method.PUT, Method.PATCH, Method.DELETE).contains(this.opts.METHOD)
        && this.body != null && !this.body.isBlank()) {
      this.req.setDoOutput(true);
      DataOutputStream outputStream = new DataOutputStream(this.req.getOutputStream());
      outputStream.writeBytes(this.body);
      outputStream.flush();
      outputStream.close();
    }

    Object response;
    switch (this.opts.RESPONSETYPE) {
      case JSON: {
        response = this.JsonResponse();
        break;
      }
      case STREAM: {
        response = this.streamResponse();
        break;
      }
      case STRING:
      default: {
        response = this.stringResponse();
        break;
      }
    }

    this.req.disconnect();

    future.completeAsync(() -> response);

    this.emit("response", response);
    this.emit("res", response);
    this.emit("r", response);

    int status = this.req.getResponseCode();
    this.emit(String.valueOf(status), response);
    if (100 <= status && status < 200) {
      this.emit("info", response);
      this.emit("i", response);
    } else if (200 <= status && status < 300) {
      this.emit("success", response);
      this.emit("ok", response);
      this.emit("s", response);
      this.emit("o", response);
    } else if (300 <= status && status < 400) {
      this.emit("redirect", response);
      this.emit("redir", response);
      this.emit("d", response);
    } else if (400 <= status && status < 500) {
      this.emit("error", response);
      this.emit("err", response);
      this.emit("e", response);
    } else if (500 <= status && status < 600) {
      this.emit("error", response);
      this.emit("err", response);
      this.emit("e", response);
    } else {
      this.emit("what", response);
    }
  }

  private String stringResponse() throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.req.getInputStream()));
    StringBuffer stringBuffer = new StringBuffer();
    String inputLine;

    while ((inputLine = bufferedReader.readLine()) != null) {
      stringBuffer.append(inputLine);
    }
    bufferedReader.close();

    String response = stringBuffer.toString();
    return response;
  }

  private Json JsonResponse() throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.req.getInputStream()));
    StringBuffer stringBuffer = new StringBuffer();
    String inputLine;

    while ((inputLine = bufferedReader.readLine()) != null) {
      stringBuffer.append(inputLine);
    }
    bufferedReader.close();

    String responseString = stringBuffer.toString();
    Json responseObject = new Json(responseString);
    return responseObject;
  }

  private InputStreamReader streamResponse() throws IOException {
    return new InputStreamReader(this.req.getInputStream());
  }

  public static void consumerRequest(URL url, HTTPRequestOptions options, String body, Consumer<Object[]> callback)
      throws IOException {
    HTTPRequest req = new HTTPRequest(url, options, body);
    req.on("response", (res) -> {
      callback.accept(res);
    });
    req.send();
  }

  public static CompletableFuture<Object> futureRequest(URL url, HTTPRequestOptions options, String body)
      throws IOException {
    HTTPRequest req = new HTTPRequest(url, options, body);
    CompletableFuture<Object> future = req.future();
    req.send();
    return future;
  }

  public static Object syncRequest(URL url, HTTPRequestOptions options, String body)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return futureRequest(url, options, body).get();
  }

  public static String syncStringRequest(Method method, String url, String body, String auth, String ua)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    HTTPRequestOptions options = new HTTPRequestOptions(method, ResponseType.STRING);
    options.HEADERS.put("User-Agent", ua);
    options.HEADERS.put("Authorization", auth);
    return (String) syncRequest(new URL(url), options, body);
  }

  public static Json syncJsonRequest(Method method, String url, String body, String auth, String ua)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    HTTPRequestOptions options = new HTTPRequestOptions(method, ResponseType.JSON);
    options.HEADERS.put("User-Agent", ua);
    options.HEADERS.put("Authorization", auth);
    options.HEADERS.put("Content-Type", "application/Json");
    return (Json) syncRequest(new URL(url), options, body);
  }

  public static InputStreamReader syncStreamRequest(Method method, String url, String body, String auth, String ua)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    HTTPRequestOptions options = new HTTPRequestOptions(method, ResponseType.STREAM);
    options.HEADERS.put("User-Agent", ua);
    options.HEADERS.put("Authorization", auth);
    return (InputStreamReader) syncRequest(new URL(url), options, body);
  }

  public static String get(String url)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncStringRequest(Method.GET, url, null, null, null);
  }

  public static String post(String url, String body)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncStringRequest(Method.POST, url, body, null, null);
  }

  public static Json JsonGet(String url)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.GET, url, null, null, null);
  }

  public static Json JsonGet(String url, String auth)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.GET, url, null, auth, null);
  }

  public static Json JsonPost(String url, String body)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.POST, url, body, null, null);
  }

  public static Json JsonPost(String url, Json body)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.POST, url, body.toString(), null, null);
  }

  public static Json JsonPost(String url, Json body, String auth)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.POST, url, body.toString(), auth, null);
  }

  public static Json JsonPatch(String url, String body)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.PATCH, url, body, null, null);
  }

  public static Json JsonPatch(String url, Json body)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.PATCH, url, body.toString(), null, null);
  }

  public static Json JsonPatch(String url, Json body, String auth)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.PATCH, url, body.toString(), auth, null);
  }

  public static Json JsonPut(String url, String body)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.PUT, url, body, null, null);
  }

  public static Json JsonPut(String url, Json body)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.PUT, url, body.toString(), null, null);
  }

  public static Json JsonPut(String url, Json body, String auth)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.PUT, url, body.toString(), auth, null);
  }

  public static Json JsonDelete(String url, String body)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.DELETE, url, body, null, null);
  }

  public static Json JsonDelete(String url, Json body)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.DELETE, url, body.toString(), null, null);
  }

  public static Json JsonDelete(String url, Json body, String auth)
      throws MalformedURLException, InterruptedException, ExecutionException, IOException {
    return syncJsonRequest(Method.DELETE, url, body.toString(), auth, null);
  }

}

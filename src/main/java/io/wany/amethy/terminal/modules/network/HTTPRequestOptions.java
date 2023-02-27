package io.wany.amethy.terminal.modules.network;

import java.util.HashMap;

public class HTTPRequestOptions {

  public Method METHOD;
  public ResponseType RESPONSETYPE;
  public HashMap<String, String> HEADERS;
  public int TIMEOUT;

  public HTTPRequestOptions() {
    this.METHOD = Method.GET;
    this.HEADERS = new HashMap<>();
    this.TIMEOUT = 2000;
    this.RESPONSETYPE = ResponseType.STRING;
  }

  public HTTPRequestOptions(Method method, ResponseType responseType) {
    this.METHOD = method;
    this.HEADERS = new HashMap<>();
    this.TIMEOUT = 2000;
    this.RESPONSETYPE = responseType;
  }

  public enum Method {
    HEAD,
    OPTIONS,
    GET,
    POST,
    PUT,
    PATCH,
    DELETE
  }

  public enum ResponseType {
    STRING,
    JSON,
    STREAM
  }

}

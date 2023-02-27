package io.wany.amethy.terminal.modules.network;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MySQLResult {

  private final List<HashMap<String, String>> result = new ArrayList<>();
  private int index;

  protected MySQLResult() {
    this.nextIndex();
  }

  protected void set(String key, String value) {
    HashMap<String, String> set = this.result.get(this.index);
    set.put(key, value);
    this.result.set(this.index, set);
  }

  protected void nextIndex() {
    this.result.add(new HashMap<>());
    this.index = this.result.size() - 1;
  }

  protected void close() {
    this.result.remove(this.result.size() - 1);
  }

  public JsonArray toJsonArray() {
    JsonArray array = new JsonArray();
    result.forEach(map -> {
      JsonObject object = new JsonObject();
      map.keySet().forEach(key -> {
        object.addProperty(key, map.get(key));
      });
      array.add(object);
    });
    return array;
  }

  @Override
  public String toString() {
    return this.toJsonArray().toString();
  }

  public int size() {
    return this.result.size();
  }

  public HashMap<String, String> get(int i) {
    try {
      return this.result.get(i);
    } catch (Exception e) {
      return null;
    }
  }

  public String getString(int i, String key) {
    try {
      return this.result.get(i).get(key);
    } catch (Exception e) {
      return null;
    }
  }

  public Integer getInteger(int i, String key) {
    try {
      return Integer.parseInt(this.result.get(i).get(key));
    } catch (Exception e) {
      return null;
    }
  }

  public Float getFloat(int i, String key) {
    try {
      return Float.parseFloat(this.result.get(i).get(key));
    } catch (Exception e) {
      return null;
    }
  }

  public Long getLong(int i, String key) {
    try {
      return Long.parseLong(this.result.get(i).get(key));
    } catch (Exception e) {
      return null;
    }
  }

  public Double getDouble(int i, String key) {
    try {
      return Double.parseDouble(this.result.get(i).get(key));
    } catch (Exception e) {
      return null;
    }
  }

  public Short getShort(int i, String key) {
    try {
      return Short.parseShort(this.result.get(i).get(key));
    } catch (Exception e) {
      return null;
    }
  }

  public Boolean getBoolean(int i, String key) {
    try {
      return Boolean.parseBoolean((String) this.result.get(i).get(key));
    } catch (Exception e) {
      return null;
    }
  }

  public Date getDate(int i, String key) {
    try {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
      return formatter.parse(this.result.get(i).get(key).replace("T", " "));
    } catch (Exception e) {
      return null;
    }
  }

}

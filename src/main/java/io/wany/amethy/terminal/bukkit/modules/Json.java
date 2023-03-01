package io.wany.amethy.terminal.bukkit.modules;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@SuppressWarnings("all")
public class Json {

  JsonObject object = null;
  File file = null;

  public Json() {
    this.object = new JsonObject();
  }

  public Json(String string) {
    try {
      this.object = JsonParser.parseString(string).getAsJsonObject();
    } catch (Exception e) {
      this.object = new JsonObject();
    }
  }

  public Json(JsonObject object) {
    this.object = object;
  }

  public Json(File file) {
    this.file = file;
    this.load(this.file);
    if (this.object == null) {
      this.object = new JsonObject();
    }
  }

  public String toString() {
    return object.toString();
  }

  public Json clone() {
    Json json = new Json();
    json.object = this.object.deepCopy();
    json.file = new File(this.file.getAbsolutePath());
    return json;
  }

  public void load(File file) {
    if (file == null) {
      return;
    }
    if (!file.exists() || !file.canRead()) {
      return;
    }
    try {
      this.object = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
    } catch (Exception e) {
      this.object = new JsonObject();
    }
  }

  public void load() {
    this.load(this.file);
  }

  public void save(File file) {
    try {
      if (file == null) {
        return;
      }
      file = file.getAbsoluteFile();
      file.getParentFile().mkdirs();
      if (!file.exists()) {
        file.createNewFile();
      }
      FileWriter writer = new FileWriter(file);
      writer.write(this.toString());
      writer.close();
    } catch (Exception ignored) {
    }
  }

  public void save() {
    this.save(this.file);
  }

  @SuppressWarnings("all")
  public void set(String path, Object value) {
    String[] keys = path.split("\\.");

    JsonObject object = this.object;

    for (int i = 0; i < keys.length - 1; i++) {
      String key = keys[i];
      if (object.has(key) && object.get(key).isJsonPrimitive()) {
        object.remove(key);
      }
      if (!object.has(key)) {
        object.add(key, new JsonObject());
      }
      object = object.getAsJsonObject(key);
    }

    String setKey = keys[keys.length - 1];
    if (value instanceof String) {
      object.addProperty(setKey, String.valueOf(value));
    } else if (value instanceof Number) {
      object.addProperty(setKey, (Number) value);
    } else if (value instanceof Boolean) {
      object.addProperty(setKey, Boolean.parseBoolean(String.valueOf(value)));
    } else if (value instanceof Json) {
      object.add(setKey, ((Json) value).getJsonObject());
    } else if (value instanceof Object[]) {
      object.add(setKey, parseArray((Object[]) value));
    } else if (value instanceof List) {
      object.add(setKey, parseArray((List<Object>) value));
    } else if (value instanceof JsonElement) {
      object.add(setKey, (JsonElement) value);
    }

    this.save();
  }

  public boolean has(String path) {
    return this.getJsonElement(path) != null;
  }

  public JsonObject getJsonObject() {
    return this.object.deepCopy();
  }

  public JsonElement getJsonElement(String path) {
    String[] keys = path.split("\\.");

    JsonElement element = this.object;

    for (int i = 0; i < keys.length; i++) {
      String key = keys[i];
      if (element.isJsonObject()) {
        JsonObject object = element.getAsJsonObject();
        if (!object.getAsJsonObject().has(key)) {
          return null;
        } else {
          element = object.get(key);
        }
      } else {
        return element;
      }
    }

    return element.isJsonPrimitive() ? element : element.deepCopy();
  }

  public Json get(String path) {
    return this.getJsonElement(path) != null ? new Json(this.getJsonElement(path).toString()) : null;
  }

  public String getString(String path) {
    return this.getJsonElement(path) != null ? this.getJsonElement(path).getAsString() : null;
  }

  public boolean getBoolean(String path) {
    return this.getJsonElement(path) != null ? this.getJsonElement(path).getAsBoolean() : null;
  }

  public int getInt(String path) {
    return this.getJsonElement(path) != null ? this.getJsonElement(path).getAsInt() : null;
  }

  public float getFloat(String path) {
    return this.getJsonElement(path) != null ? this.getJsonElement(path).getAsFloat() : null;
  }

  public double getDouble(String path) {
    return this.getJsonElement(path) != null ? this.getJsonElement(path).getAsDouble() : null;
  }

  public long getLong(String path) {
    return this.getJsonElement(path) != null ? this.getJsonElement(path).getAsLong() : null;
  }

  public JsonArray getJsonArray(String path) {
    return this.getJsonElement(path) != null ? this.getJsonElement(path).getAsJsonArray() : null;
  }

  public List<String> getStringList(String path) {
    JsonArray array = this.getJsonArray(path);
    if (array == null) {
      return null;
    }
    List<String> list = new ArrayList<String>();
    array.forEach((JsonElement element) -> {
      list.add(element.getAsString());
    });
    return list;
  }

  private static JsonArray parseArray(Object[] array) {
    JsonArray object = new JsonArray();
    for (Object value : array) {
      if (value instanceof String) {
        object.add(String.valueOf(value));
      } else if (value instanceof Number) {
        object.add((Number) value);
      } else if (value instanceof Boolean) {
        object.add(Boolean.parseBoolean(String.valueOf(value)));
      } else if (value instanceof Object[]) {
        object.add(parseArray((Object[]) value));
      } else if (value instanceof Json) {
        object.add(JsonParser.parseString(((Json) value).toString()));
      } else if (value instanceof JsonElement) {
        object.add((JsonElement) value);
      }
    }
    return object;
  }

  private static JsonArray parseArray(List<Object> array) {
    JsonArray object = new JsonArray();
    for (Object value : array) {
      if (value instanceof String) {
        object.add(String.valueOf(value));
      } else if (value instanceof Number) {
        object.add((Number) value);
      } else if (value instanceof Boolean) {
        object.add(Boolean.parseBoolean(String.valueOf(value)));
      } else if (value instanceof Object[]) {
        object.add(parseArray((Object[]) value));
      } else if (value instanceof Json) {
        object.add(JsonParser.parseString(((Json) value).toString()));
      } else if (value instanceof JsonElement) {
        object.add((JsonElement) value);
      }
    }
    return object;
  }

}

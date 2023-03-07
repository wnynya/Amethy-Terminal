package io.wany.amethy.terminal.bukkit.panels.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.wany.amethy.terminal.bukkit.TerminalNode;
import io.wany.amethyst.Json;

public class TerminalFilesystem {

  private static List<TerminalFile> readingfiles = new ArrayList<>();
  private static HashMap<String, TerminalFile> writingFiles = new HashMap<>();

  public static void onEnable() {
    addEventListener();
  }

  public static void onDisable() {
    for (TerminalFile file : readingfiles) {
      file.kill();
    }
    for (TerminalFile file : writingFiles.values()) {
      file.kill();
    }
  }

  public static void addEventListener() {
    TerminalNode.on("filesystem/dir-read", (client, data) -> {
      dirRead(client, data.getString("path"));
    });
    TerminalNode.on("filesystem/dir-create", (client, data) -> {
      dirCreate(client, data.getString("path"));
    });
    TerminalNode.on("filesystem/dir-delete", (client, data) -> {
      dirDelete(client, data.getString("path"));
    });
    TerminalNode.on("filesystem/file-read", (client, data) -> {
      fileRead(client, data.getString("path"));
    });
    TerminalNode.on("filesystem/file-create", (client, data) -> {
      fileCreate(client, data.getString("path"));
    });
    TerminalNode.on("filesystem/file-write-open", (client, data) -> {
      fileWriteOpen(client, data.getString("path"), data.getInt("chunks"));
    });
    TerminalNode.on("filesystem/file-write-chunk", (client, data) -> {
      fileWriteChunk(client, data.getString("path"), data.getInt("index"), data.getString("chunk"));
    });
    TerminalNode.on("filesystem/file-delete", (client, data) -> {
      fileDelete(client, data.getString("path"));
    });
  }

  public static void dirRead(Json client, String path) {
    TerminalDir dir = new TerminalDir(new File(path));
    Json obj = new Json();
    obj.set("path", path);
    obj.set("files", dir.files());
    TerminalNode.event("filesystem/dir-read", client, obj);
  }

  public static void dirCreate(Json client, String path) {
    TerminalDir dir = new TerminalDir(new File(path));
    dir.create();
    TerminalNode.event("filesystem/dir-create", client, new Json());
  }

  public static void dirDelete(Json client, String path) {
    TerminalDir dir = new TerminalDir(new File(path));
    dir.delete();
    TerminalNode.event("filesystem/dir-delete", client, new Json());
  }

  public static void fileRead(Json client, String path) {
    ExecutorService ex = Executors.newFixedThreadPool(1);
    ex.submit(() -> {
      TerminalFile file = new TerminalFile(new File(path));
      int chunks = file.chunks();
      Json open = new Json();
      open.set("path", path);
      open.set("name", file.file().getName());
      open.set("chunks", chunks);
      TerminalNode.event("filesystem/file-read-open", client, open);
      file.on("read/close", (arg) -> {
        readingfiles.remove(file);
        Json data = new Json();
        data.set("path", path);
        TerminalNode.event("filesystem/file-read-close", client, data);
        ex.shutdown();
      });
      readingfiles.add(file);
      file.read((index, chunk) -> {
        Json data = new Json();
        data.set("path", path);
        data.set("index", index);
        data.set("chunk", chunk);
        TerminalNode.event("filesystem/file-read-chunk", client, data);
        try {
          TimeUnit.MILLISECONDS.sleep(150);
        } catch (InterruptedException ignored) {
        }
      });
    });
  }

  public static void fileCreate(Json client, String path) {
    TerminalFile file = new TerminalFile(new File(path));
    file.create();
    TerminalNode.event("filesystem/file-create", client, new Json());
  }

  public static void fileWriteOpen(Json client, String path, int chunks) {
    String key = path + "@" + client.getString("id");
    TerminalFile file = new TerminalFile(new File(path), chunks);
    file.on("write/close", (arg) -> {
      writingFiles.remove(key);
      TerminalNode.event("filesystem/file-write-close", client, new Json());
    });
    writingFiles.put(key, file);
    TerminalNode.event("filesystem/file-read-open", client, new Json());
  }

  public static void fileWriteChunk(Json client, String path, int index, String chunk) {
    String key = path + "@" + client.getString("id");
    TerminalFile file = writingFiles.get(key);
    file.write(index, chunk);
    TerminalNode.event("filesystem/file-write-chunk", client, new Json());
  }

  public static void fileDelete(Json client, String path) {
    TerminalFile file = new TerminalFile(new File(path));
    file.delete();
    TerminalNode.event("filesystem/file-delete", client, new Json());
  }

}

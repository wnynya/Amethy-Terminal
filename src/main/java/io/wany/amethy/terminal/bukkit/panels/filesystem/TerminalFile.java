package io.wany.amethy.terminal.bukkit.panels.filesystem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Base64;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.wany.amethy.terminal.bukkit.modules.EventEmitter;
import io.wany.amethy.terminal.bukkit.modules.Json;

public class TerminalFile extends EventEmitter {

  private File file;
  private FileInputStream fis = null;
  private BufferedInputStream bis = null;
  private FileOutputStream fos = null;
  private BufferedOutputStream bos = null;
  private int chunkSize = 75000;
  private int chunkBlockSize = chunkSize * 10;
  private int chunksLength = 0;
  private int chunksIndex = 0;
  private int chunksOffset = 0;
  private String[] waitingChunks;

  private boolean base64 = true;

  public TerminalFile(File file) {
    super();

    this.file = file.getAbsoluteFile();
    ;
  }

  public TerminalFile(File file, int chunks) {
    super();

    this.file = file.getAbsoluteFile();
    this.chunksLength = chunks;
    this.waitingChunks = new String[chunks];
  }

  public File file() {
    return this.file;
  }

  public Json info() {
    Json json = new Json();
    json.set("type", "file");
    json.set("path", this.file.getAbsolutePath());
    json.set("name", this.file.getName());
    json.set("perm.r", this.file.canRead());
    json.set("perm.w", this.file.canWrite());
    json.set("perm.x", this.file.canExecute());
    json.set("size", this.size());
    try {
      BasicFileAttributes attr = Files.readAttributes(this.file.toPath(), BasicFileAttributes.class);
      json.set("creation", attr.creationTime().toMillis());
      json.set("modified", attr.lastModifiedTime().toMillis());
    } catch (Exception e) {
      json.set("creation", 0);
      json.set("modified", 0);
    }
    return json;
  }

  public long size() {
    try {
      return Files.size(this.file.toPath());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return -1;
  }

  public void create() {
    try {
      this.delete();
      File dir = this.file.getParentFile();
      if (dir != null) {
        dir.mkdirs();
      }
      this.file.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void delete() {
    if (this.file.exists()) {
      this.file.delete();
    }
  }

  private void copy(TerminalFile dest, Consumer<Object> end) {
    TerminalFile d = new TerminalFile(dest.file, this.chunks());
    this.read((index, chunk) -> d.write(index, chunk));
    d.on("write/close", (args) -> end.accept(null));
  }

  public void copy(TerminalFile dest) {
    this.copy(dest, (arg) -> {
    });
  }

  public void move(TerminalFile dest) {
    this.copy(dest, (arg) -> this.delete());
  }

  public int chunks() {
    int bytes = Integer.parseInt(this.size() + "");
    if (bytes == -1) {
      return -1;
    }
    return (bytes / this.chunkSize) + (bytes % this.chunkSize > 1 ? 1 : 0);
  }

  public void read(BiConsumer<Integer, String> reader) {
    try {
      // 열기
      this.fis = new FileInputStream(this.file);
      this.bis = new BufferedInputStream(this.fis);
      this.emit("read/open");

      // 읽기
      int index = 0;
      while (this.bis.available() > 0) {
        int size = Math.min(this.bis.available(), this.chunkSize);
        byte[] bytes = new byte[size];
        this.bis.read(bytes, 0, bytes.length);
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
          int v = bytes[j] & 0xFF;
          hexChars[j * 2] = hexArray[v >>> 4];
          hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        String chunk;
        if (base64) {
          chunk = Base64.getEncoder().encodeToString(bytes);
        } else {
          chunk = new String(bytes);
        }
        reader.accept(index, chunk);
        this.emit("read/read", index);
        index++;
      }

      // 닫기
      this.fis.close();
      this.bis.close();
      this.emit("read/close");
    } catch (IOException e) {
      this.emit("error", e);
    }
  }

  public void write(int index, String chunk) {
    // 대기열 추가
    if (index != this.chunksIndex) {
      this.waitingChunks[index] = chunk;
      // 대기열 확인
      if (this.chunksIndex < this.chunksLength && this.waitingChunks[this.chunksIndex] != null) {
        String c = this.waitingChunks[this.chunksIndex];
        this.waitingChunks[this.chunksIndex] = null;
        this.write(this.chunksIndex, c);
      }
      return;
    }
    try {
      // 열기
      if (this.fos == null && this.bos == null) {
        this.create();
        this.fos = new FileOutputStream(this.file);
        this.bos = new BufferedOutputStream(this.fos);
        this.emit("write/open");
      }

      // 쓰기
      byte[] bytes;
      if (base64) {
        bytes = Base64.getDecoder().decode(chunk);
      } else {
        bytes = chunk.getBytes();
      }
      int length = bytes.length;
      this.bos.write(bytes, 0, length);
      this.emit("write/write", this.chunksIndex);
      this.chunksOffset += length;
      if (this.chunksOffset >= chunkBlockSize) {
        this.bos.flush();
        this.chunksOffset = 0;
      }
      this.chunksIndex++;

      // 닫기
      if (this.chunksIndex == this.chunksLength) {
        this.bos.flush();
        this.fos.close();
        this.bos.close();
        this.emit("write/close");
      }

      // 대기열 확인
      else if (this.waitingChunks[this.chunksIndex] != null) {
        String c = this.waitingChunks[this.chunksIndex];
        this.waitingChunks[this.chunksIndex] = null;
        this.write(this.chunksIndex, c);
      }
    } catch (IOException e) {
      this.emit("error", e);
    }
  }

  public void kill() {
    try {
      if (this.fis != null) {
        this.fis.close();
      }
      if (this.bis != null) {
        this.bis.close();
      }
      if (this.fos != null) {
        this.fos.close();
      }
      if (this.bos != null) {
        this.bos.flush();
        this.bos.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void waitRead(long mil) {
    if (this.bis != null) {
      try {
        this.bis.wait(mil);
      } catch (InterruptedException e) {
      }
    }
  }

}

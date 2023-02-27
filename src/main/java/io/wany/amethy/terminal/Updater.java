package io.wany.amethy.terminal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;

import io.wany.amethy.terminal.modules.Json;
import io.wany.amethy.terminal.modules.network.HTTPRequest;

public class Updater {

  private static final String API = "api.wany.io/amethy/repository/Amethy-Terminal";
  public static String CHANNEL = "release";
  public static boolean AUTOMATION = false;

  private static ExecutorService onEnableExecutor = Executors.newFixedThreadPool(1);
  private static Timer onEnableTimer = new Timer();

  protected static void onEnable() {

    if (AmethyTerminal.CONFIG.has("updater.channel")) {
      CHANNEL = AmethyTerminal.CONFIG.getString("updater.channel");
    } else {
      AmethyTerminal.CONFIG.set("updater.channel", CHANNEL);
    }

    if (AmethyTerminal.CONFIG.has("updater.automation")) {
      AUTOMATION = AmethyTerminal.CONFIG.getBoolean("updater.automation");
    } else {
      AmethyTerminal.CONFIG.set("updater.automation", AUTOMATION);
    }

    onEnableExecutor.submit(() -> {
      onEnableTimer.schedule((new TimerTask() {
        @Override
        public void run() {
          if (AUTOMATION) {
            automation();
          }
        }
      }), 5000, 2000);
    });
  }

  protected static void onDisable() {
    onEnableTimer.cancel();
    onEnableExecutor.shutdown();
  }

  public static boolean isLatest() throws Exception {
    return getLatest().equals(AmethyTerminal.VERSION);
  }

  public static String getLatest() throws Exception {
    String version = null;
    Json res = HTTPRequest.JsonGet("https://" + API + "/" + CHANNEL + "/latest");
    version = res.getString("data.version");
    return version;
  }

  public static File download(String version) throws Exception {
    File file = new File(
        AmethyTerminal.PLUGIN.getDataFolder().getParentFile().getAbsolutePath() + "/" + version + ".temp");

    try {
      if (file.exists()) {
        file.delete();
      }
      file.getParentFile().mkdirs();
      file.createNewFile();
    } catch (SecurityException exception) {
      file.delete();
      throw exception;
    } catch (IOException exception) {
      file.delete();
      throw exception;
    }

    try {
      BufferedInputStream bis = new BufferedInputStream(
          new URL("https://" + API + "/" + CHANNEL + "/" + version + "/download").openStream());
      FileOutputStream fis = new FileOutputStream(file);
      byte[] buffer = new byte[1024];
      int count = 0;
      while ((count = bis.read(buffer, 0, 1024)) != -1) {
        fis.write(buffer, 0, count);
      }
      fis.close();
      bis.close();
    } catch (Exception exception) {
      file.delete();
      throw exception;
    }

    return file;
  }

  public static void update(File tempFile, String version) throws Exception {
    String name = AmethyTerminal.PLUGIN.getDescription().getName();
    File newFile = new File(AmethyTerminal.PLUGINS_DIR, name + "-" + version + ".jar");

    byte[] data = new byte[0];
    data = Files.readAllBytes(tempFile.toPath());
    Path path = newFile.toPath();
    tempFile.delete();
    Files.write(path, data);

    Bukkit.getScheduler().runTask(AmethyTerminal.PLUGIN, () -> {
      // Terminal.STATUS = Terminal.Status.UPDATE;
      BukkitPluginLoader.unload();
      if (!newFile.getPath().equals(AmethyTerminal.FILE.getPath())) {
        AmethyTerminal.FILE.delete();
      }
      BukkitPluginLoader.load(newFile);
    });
  }

  public static void automation() {
    try {
      String version = Updater.getLatest();
      if (!AmethyTerminal.VERSION.equals(version)) {
        Console.debug("Found newer version of plugin");
        Console.debug("Updating plugin...");
        File file = Updater.download(version);
        Updater.update(file, version);
      }
    } catch (Exception e) {
      return;
    }
  }

}

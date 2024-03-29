package io.wany.amethy.terminal.bukkit;

import io.wany.amethy.terminal.bukkit.commands.AmethyTerminalCommand;
import io.wany.amethy.terminal.bukkit.commands.AmethyTerminalTabCompleter;
import io.wany.amethy.terminal.bukkit.modules.*;
import io.wany.amethyst.Json;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Amethy Terminal (Bukkit)
 * https://amethy.wany.io
 * https://github.com/wnynya/Amethy-Terminal
 * <p>
 * </p>
 * ©2023 Wany <sung@wany.io> (https://wany.io)
 */

@SuppressWarnings("unused deprecation")
public class AmethyTerminal extends JavaPlugin {

  public static AmethyTerminal PLUGIN;

  public static final String NAME = "아메시 터미널";
  public static final String PREFIX = "§x§d§2§b§0§d§d§l[" + NAME + "]:§r ";
  protected static final String PREFIX_CONSOLE = "[" + NAME + "] ";
  protected static final boolean ISRELOAD = Bukkit.getWorlds().size() != 0;

  public static String VERSION;
  public static File FILE;
  public static File PLUGINS_DIR;
  public static File SERVER_DIR;
  public static Json CONFIG;
  public static boolean DEBUG = false;
  protected static String UID = "";
  protected static String KEY = "";
  private static boolean DISABLED = false;
  public static boolean PAPERAPI;
  public static ServerMessage MESSAGE;

  @Override
  public void onLoad() {

    if (PLUGIN != null) {
      return;
    }

    PLUGIN = this;

    VERSION = PLUGIN.getDescription().getVersion();
    FILE = PLUGIN.getFile().getAbsoluteFile();
    PLUGINS_DIR = FILE.getParentFile();
    SERVER_DIR = PLUGINS_DIR.getParentFile();

    CONFIG = new Json(SERVER_DIR.toPath().resolve("terminal.json").toFile());

    if (CONFIG.has("debug")) {
      DEBUG = CONFIG.getBoolean("debug");
    } else {
      CONFIG.set("debug", false);
    }

    String javaVersion = System.getProperty("java.version");
    if (javaVersion.startsWith("1.")) {
      javaVersion = javaVersion.substring(2, 3);
    } else {
      int dot = javaVersion.indexOf(".");
      if (dot != -1) {
        javaVersion = javaVersion.substring(0, dot);
      }
    }
    int JAVA_VERSION = Integer.parseInt(javaVersion);

    if (JAVA_VERSION < 11) {
      DISABLED = true;
      return;
    }

    String version = Bukkit.getServer().getVersion().toLowerCase();
    PAPERAPI = version.contains("paper") || version.contains("purpur") || version.contains("pufferfish");

    MESSAGE = PAPERAPI ? new PaperMessage() : new SpigotMessage();

    TerminalNode.onLoad();

  }

  @Override
  public void onEnable() {

    if (PLUGIN == null) {
      onLoad();
    }

    if (DISABLED) {
      console.error("Plugin requires Java version >= 11 to run. Disable plugin.");
      PluginLoader.unload(PLUGIN);
      return;
    }

    Updater.onEnable();

    registerCommand("amethyterminal", new AmethyTerminalCommand(), new AmethyTerminalTabCompleter());

    TerminalNode.onEnable();

  }

  @Override
  public void onDisable() {

    if (DISABLED) {
      return;
    }

    TerminalNode.onDisable();

    Updater.onDisable();

  }

  @SuppressWarnings("all")
  protected void registerCommand(String cmd, CommandExecutor exc, TabCompleter tab) {
    PluginCommand pc = this.getCommand(cmd);
    if (pc == null) {
      return;
    }
    pc.setExecutor(exc);
    pc.setTabCompleter(tab);
  }

  protected void registerEvent(Listener l) {
    PluginManager pm = Bukkit.getServer().getPluginManager();
    pm.registerEvents(l, this);
  }

}

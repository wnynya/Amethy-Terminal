package io.wany.amethy.terminal.bukkit;

import java.io.File;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import io.wany.amethy.terminal.bukkit.commands.AmethyTerminalCommand;
import io.wany.amethy.terminal.bukkit.commands.AmethyTerminalTabCompleter;
import io.wany.amethy.terminal.bukkit.modules.Json;

public class AmethyTerminal extends JavaPlugin {

  public static AmethyTerminal PLUGIN;

  public static final String NAME = "Amethy Terminal";
  public static final String PREFIX = "§l§x§d§2§b§0§d§d[" + NAME + "]:§r ";
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

  @Override
  public void onLoad() {

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

    TerminalNode.onLoad();

  }

  @Override
  public void onEnable() {

    registerCommand("amethyterminal", new AmethyTerminalCommand(), new AmethyTerminalTabCompleter());

    TerminalNode.onEnable();

    Updater.onEnable();

  }

  @Override
  public void onDisable() {

    TerminalNode.onDisable();

    Updater.onDisable();

  }

  protected void registerCommand(String cmd, CommandExecutor exc, TabCompleter tab) {
    Map<String, Map<String, Object>> map = PLUGIN.getDescription().getCommands();
    if (map.containsKey(cmd)) {
      PluginCommand pc = this.getCommand(cmd);
      if (pc == null) {
        return;
      }
      pc.setExecutor(exc);
      pc.setTabCompleter(tab);
    }
  }

  protected void registerEvent(Listener l) {
    PluginManager pm = Bukkit.getServer().getPluginManager();
    pm.registerEvents(l, this);
  }

}

package io.wany.amethy.terminal.commands;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.wany.amethy.terminal.AmethyTerminal;
import io.wany.amethy.terminal.Console;
import io.wany.amethy.terminal.Updater;
import io.wany.amethy.terminal.BukkitPluginLoader;

public class AmethyTerminalCommand implements CommandExecutor {

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

    if (args.length == 0) {
      // 오류: args[0] 필요
      return true;
    }

    switch (args[0].toLowerCase()) {

      case "version" -> {
        if (!sender.hasPermission("amethy.terminal.version")) {
          // 오류: 권한 없음
          return true;
        }
        String tail = "";
        try {
          if (Updater.isLatest()) {
            tail = "[LATEST]";
          } else {
            tail = "[OUTDATED]";
          }
        } catch (Exception e) {
          tail = "[VERSION CHECK FAILED]";
        }
        info(sender, AmethyTerminal.NAME + " v" + AmethyTerminal.VERSION + " " + tail);
        return true;
      }

      case "reload" -> {
        if (!sender.hasPermission("amethy.terminal.reload")) {
          // 오류: 권한 없음
          return true;
        }
        info(sender, "Reloading " + AmethyTerminal.NAME + " v" + AmethyTerminal.VERSION);
        long s = System.currentTimeMillis();
        BukkitPluginLoader.unload();
        BukkitPluginLoader.load(AmethyTerminal.FILE);
        long e = System.currentTimeMillis();
        info(sender, "Reload complete (" + (e - s) + "ms)");
        return true;
      }

      case "debug" -> {
        if (!sender.hasPermission("amethy.terminal.debug")) {
          // 오류: 권한 없음
          return true;
        }
        boolean next = AmethyTerminal.DEBUG;
        if (args.length == 1) {
          next = !AmethyTerminal.DEBUG;
        } else if (args.length >= 2) {
          if (args[1].toLowerCase().equals("enable")) {
            next = true;
          } else if (args[1].toLowerCase().equals("disable")) {
            next = false;
          } else {
            // 오류: 올바른 키가 아님
            return true;
          }
        }
        AmethyTerminal.DEBUG = next;
        AmethyTerminal.CONFIG.set("debug", AmethyTerminal.DEBUG);
        info(sender, "Debug " + (next ? "en" : "dis") + "abled");
        return true;
      }

      case "update" -> {
        if (!sender.hasPermission("amethy.terminal.updater.update")) {
          // 오류: 권한 없음
          return true;
        }
        String version;
        try {
          version = Updater.getLatest();
        } catch (Exception e) {
          error(sender, "Version check failed " + e.getMessage());
          return true;
        }
        if (AmethyTerminal.VERSION.equals(version)) {
          if (args.length >= 2 && args[1].toLowerCase().equals("-force")) {

          } else {
            warn(sender, "It's already the latest version");
            warn(sender, "Use -force flag to update force");
            return true;
          }
        }
        info(sender, "Found newer version of plugin");
        info(sender, "  Current: " + AmethyTerminal.NAME + " v" + version);
        info(sender, "  Latest: " + AmethyTerminal.NAME + " v" + AmethyTerminal.VERSION);
        info(sender, "Downloading file...");
        File file;
        try {
          file = Updater.download(version);
        } catch (Exception e) {
          error(sender, "File download failed " + e.getMessage());
          return true;
        }
        info(sender, "Download complete");
        info(sender, "Updating plugin...");
        try {
          Updater.update(file, version);
        } catch (Exception e) {
          error(sender, "Plugin update failed " + e.getMessage());
          return true;
        }
        info(sender, "Update complete");
        return true;
      }

      case "updater" -> {
        if (!sender.hasPermission("amethy.terminal.updater")) {
          // 오류: 권한 없음
          return true;
        }

        if (args.length >= 1) {
          if (args[1].toLowerCase().equals("automation")) {
            boolean next = Updater.AUTOMATION;
            if (args.length >= 3) {
              if (args[2].toLowerCase().equals("enable")) {
                next = true;
              } else if (args[2].toLowerCase().equals("disable")) {
                next = false;
              } else {
                // 오류: 올바른 키가 아님
                return true;
              }
              Updater.AUTOMATION = next;
              AmethyTerminal.CONFIG.set("updater.automation", Updater.AUTOMATION);
              info(sender, "Updater automation " + (next ? "en" : "dis") + "abled");
              return true;
            } else {
              info(sender, "Updater automation is currently " + (next ? "en" : "dis") + "abled");
              return true;
            }
          } else if (args[1].toLowerCase().equals("channel")) {
            String next = Updater.CHANNEL;
            if (args.length >= 3) {
              if (args[2].toLowerCase().equals("release")) {
                next = "release";
              } else if (args[2].toLowerCase().equals("dev")) {
                next = "dev";
              } else {
                // 오류: 알 수 없는 채널
                return true;
              }
              Updater.CHANNEL = next;
              AmethyTerminal.CONFIG.set("updater.channel", Updater.CHANNEL);
              info(sender, "Updater channel changed to " + next);
              return true;
            } else {
              info(sender, "Current updater channel is " + Updater.CHANNEL);
              return true;
            }
          } else {
            // 오류: 알 수 없는 args[1]
            return true;
          }
        } else {
          // 오류: args[1] 필요
          return true;
        }
      }

      default -> {
        // 오류: 알 수 없는 args[0]
        return true;
      }

    }

  }

  public void info(CommandSender sender, String message) {
    if (sender instanceof Player player) {
      player.sendMessage(AmethyTerminal.PREFIX + message);
    } else {
      Console.info(message);
    }
  }

  public void warn(CommandSender sender, String message) {
    if (sender instanceof Player player) {
      player.sendMessage(AmethyTerminal.PREFIX + "§e" + message);
    } else {
      Console.warn(message);
    }
  }

  public void error(CommandSender sender, String message) {
    if (sender instanceof Player player) {
      player.sendMessage(AmethyTerminal.PREFIX + "§c" + message);
    } else {
      Console.error(message);
    }
  }

}
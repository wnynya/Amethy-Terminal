package io.wany.amethy.terminal.bukkit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AmethyTerminalTabCompleter implements TabCompleter {

  private static List<String> autoComplete(List<String> list, String arg) {
    if (!arg.equalsIgnoreCase("")) {
      List<String> filtered = new ArrayList<>();
      for (String value : list) {
        if (value.toLowerCase().contains(arg.toLowerCase())) {
          filtered.add(value);
        }
      }
      return sort(filtered);
    }
    return sort(list);
  }

  private static List<String> sort(List<String> list) {
    Collections.sort(list);
    return list;
  }

  private static List<String> listOf(String... args) {
    List<String> list = new ArrayList<String>();
    Collections.addAll(list, args);
    return list;
  }

  private static void usedFlags(String[] args, int commandArgsLength, List<String> list) {
    int n = 0;
    for (String arg : args) {
      if (n >= commandArgsLength) {
        if (arg.equals("-silent") || arg.equals("-s")) {
          list.remove("-silent");
          list.remove("-s");
        }
        if (arg.equals("-force") || arg.equals("-f")) {
          list.remove("-force");
          list.remove("-f");
        }
        if (arg.equals("-applyPhysics") || arg.equals("-ap")) {
          list.remove("-applyPhysics");
          list.remove("-ap");
        }
      }
      n++;
    }
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    String name = command.getName().toLowerCase();

    // amethyterminal ?
    if (name.equals("amethyterminal")) {

      String permPrefix = "amethy.terminal";
      int agi = 0;

      if (args.length == agi + 1) {
        List<String> list = new ArrayList<>();
        if (sender.hasPermission(permPrefix + ".version")) {
          list.add("version");
        }
        if (sender.hasPermission(permPrefix + ".reload")) {
          list.add("reload");
        }
        if (sender.hasPermission(permPrefix + ".debug")) {
          list.add("debug");
        }
        if (sender.hasPermission(permPrefix + ".updater.update")) {
          list.add("update");
        }
        if (sender.hasPermission(permPrefix + ".updater")) {
          list.add("updater");
        }
        if (sender instanceof ConsoleCommandSender && sender.hasPermission(permPrefix + ".grant")) {
          list.add("grant");
        }
        return autoComplete(list, args[args.length - 1]);
      }

      // ... [agi] ?
      switch (args[agi].toLowerCase()) {

        case "debug": {
          if (!sender.hasPermission("amethy.terminal.debug")) {
            return Collections.emptyList();
          }
          // ... debug ?
          if (args.length == agi + 2) {
            List<String> list = listOf("enable", "disable");
            return autoComplete(list, args[args.length - 1]);
          } else {
            return Collections.emptyList();
          }
        }

        case "update": {
          if (!sender.hasPermission("amethy.terminal.updater.update")) {
            return Collections.emptyList();
          }
          int commandArgsLength = 1;
          // flags
          List<String> flags = listOf("-force");
          if (args.length <= commandArgsLength + flags.size()) {
            List<String> list = new ArrayList<>(flags);
            usedFlags(args, commandArgsLength, list);
            return autoComplete(list, args[args.length - 1]);
          }
          return Collections.emptyList();
        }

        case "updater": {
          if (!sender.hasPermission("amethy.terminal.updater")) {
            return Collections.emptyList();
          }
          // ... updater ?
          if (args.length == agi + 2) {
            List<String> list = listOf("channel", "automation");
            return autoComplete(list, args[args.length - 1]);
          }
          // ... updater [agi + 1] ?
          else if (args.length == agi + 3) {
            // ... updater channel ?
            if (args[agi + 1].equalsIgnoreCase("channel")) {
              List<String> list = listOf("release", "dev");
              return autoComplete(list, args[args.length - 1]);
            }
            // ... updater automation ?
            else if (args[agi + 1].equalsIgnoreCase("automation")) {
              List<String> list = listOf("enable", "disable");
              return autoComplete(list, args[args.length - 1]);
            } else {
              return Collections.emptyList();
            }
          } else {
            return Collections.emptyList();
          }
        }

      }
    }

    return Collections.emptyList();
  }

}

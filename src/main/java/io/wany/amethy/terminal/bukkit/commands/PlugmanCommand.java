package io.wany.amethy.terminal.bukkit.commands;

import io.wany.amethy.terminal.bukkit.AmethyTerminal;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class PlugmanCommand implements CommandExecutor {

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

    Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

    Component message = (Component) AmethyTerminal.MESSAGE.of("§rPlugins (" + plugins.length + "): ");
    for (var i = 0; i < plugins.length; i++) {
      Plugin plugin = plugins[i];
      String name = plugin.getName();
      String coloredName = plugin.isEnabled() ? "§a" + name + (plugin.getDescription().getAPIVersion() == null ? "*": "" ):"§c" + name;
      Component comp = (Component) AmethyTerminal.MESSAGE.of(coloredName);
      comp = comp.hoverEvent((Component) AmethyTerminal.MESSAGE.of("§a" + plugin.getDescription().getVersion()));
      message = message.append(comp);
      if (i < plugins.length - 1) {
        message = message.append((Component) AmethyTerminal.MESSAGE.of("§r, "));
      }
    }

    AmethyTerminal.MESSAGE.send(message);
    return true;
  }

  public void info(CommandSender sender, Object... objects) {
    AmethyTerminal.MESSAGE.info(sender, AmethyTerminal.PREFIX, objects);
  }

  public void warn(CommandSender sender, Object... objects) {
    AmethyTerminal.MESSAGE.warn(sender, AmethyTerminal.PREFIX, objects);
  }

  public void error(CommandSender sender, Object... objects) {
    AmethyTerminal.MESSAGE.error(sender, AmethyTerminal.PREFIX, objects);
  }
}

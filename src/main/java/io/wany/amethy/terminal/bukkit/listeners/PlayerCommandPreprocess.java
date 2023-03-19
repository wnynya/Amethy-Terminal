package io.wany.amethy.terminal.bukkit.listeners;

import io.wany.amethy.terminal.bukkit.AmethyTerminal;
import io.wany.amethy.terminal.bukkit.console;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerCommandPreprocess implements Listener {

  @EventHandler
  public void onEvent(PlayerCommandPreprocessEvent event) {
    if (event.getMessage().equalsIgnoreCase("/plugins") || event.getMessage().equalsIgnoreCase("/pl") || event.getMessage().equalsIgnoreCase("/bukkit:plugins") || event.getMessage().equalsIgnoreCase("/bukkit:pl")) {

      event.setCancelled(true);

      Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
      List<String> names = new ArrayList<>();
      for (Plugin plugin : plugins) {
        names.add(plugin.getName());
      }
      Collections.sort(names);

      Component message = (Component) AmethyTerminal.MESSAGE.of("§rPlugins (" + names.size() + "): ");
      for (var i = 0; i < names.size(); i++) {
        String name = names.get(i);
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        String coloredName = plugin.isEnabled() ? "§a" + name + (plugin.getDescription().getAPIVersion() == null ? "*" : "") : "§c" + name;
        Component comp = (Component) AmethyTerminal.MESSAGE.of(coloredName);
        comp = comp.hoverEvent((Component) AmethyTerminal.MESSAGE.of("§a" + plugin.getDescription().getVersion()));
        comp = comp.clickEvent(ClickEvent.runCommand("/version " + name));
        message = message.append(comp);
        if (i < names.size() - 1) {
          message = message.append((Component) AmethyTerminal.MESSAGE.of("§r, "));
        }
      }

      AmethyTerminal.MESSAGE.send(event.getPlayer(), message);
    }
  }

}

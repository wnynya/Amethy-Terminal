package io.wany.amethy.terminal.bukkit.panels.players;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import io.wany.amethy.terminal.bukkit.TerminalNode;
import io.wany.amethy.terminal.bukkit.modules.Json;

public class TerminalPlayers {

  private static final ExecutorService onEnableExecutor = Executors.newFixedThreadPool(1);
  private static final Timer onEnableTimer1s = new Timer();

  public static void onEnable() {
    addEventListener();

    onEnableExecutor.submit(() -> {
      onEnableTimer1s.schedule(new TimerTask() {
        @Override
        public void run() {
          sendPlayers();
        }
      }, 0, 1000);
    });
  }

  public static void onDisable() {
    onEnableTimer1s.cancel();
    onEnableExecutor.shutdownNow();
  }

  public static void addEventListener() {
    TerminalNode.on("players/player", (client, data) -> {
      sendPlayer(client, data.getString("player"));
    });
  }

  public static Json getPlayers() {
    Json object = new Json();

    List<Json> players = new ArrayList<>();

    for (Player player : Bukkit.getOnlinePlayers()) {
      Json data = new Json();
      data.set("name", player.getName());
      data.set("displayName", player.getDisplayName());
      data.set("uuid", player.getUniqueId().toString());
      data.set("op", player.isOp());
      players.add(data);
    }

    object.set("players", players);
    return object;
  }

  public static Json getPlayer(String name) {
    Json object = new Json();
    Player player = Bukkit.getPlayer(name);
    if (player == null) {
      return object;
    }

    object.set("name", player.getName());
    object.set("displayName", player.getDisplayName());
    object.set("uuid", player.getUniqueId().toString());
    object.set("op", player.isOp());

    object.set("gamemode", player.getGameMode().toString());
    object.set("health", player.getHealth());
    object.set("maxHealth", player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
    object.set("hunger", player.getFoodLevel());
    object.set("saturation", player.getSaturation());
    object.set("armor", player.getAttribute(Attribute.GENERIC_ARMOR).getValue());
    object.set("armorTough", player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue());

    object.set("xp.level", player.getLevel());
    object.set("xp.exp", player.getExp());
    object.set("xp.exptolevel", player.getExpToLevel());
    object.set("xp.totalexp", player.getTotalExperience());

    Location loc = player.getLocation();
    object.set("location.world", loc.getWorld().getName());
    object.set("location.x", loc.getX());
    object.set("location.y", loc.getY());
    object.set("location.z", loc.getZ());
    object.set("location.yaw", loc.getYaw());
    object.set("location.pitch", loc.getPitch());

    Location locs = player.getBedSpawnLocation();
    if (locs == null) {
      locs = player.getWorld().getSpawnLocation();
    } else {
      object.set("spawn.world", locs.getWorld().getName());
      object.set("spawn.x", locs.getX());
      object.set("spawn.y", locs.getY());
      object.set("spawn.z", locs.getZ());
      object.set("spawn.yaw", locs.getYaw());
      object.set("spawn.pitch", locs.getPitch());
    }

    Json connection = new Json();
    InetSocketAddress socketAddress = player.getAddress();
    if (socketAddress != null) {
      connection.set("socket-hostname", socketAddress.getHostName());
      connection.set("socket-host", socketAddress.getHostString());
      connection.set("socket-port", socketAddress.getPort());
      InetAddress address = socketAddress.getAddress();
      if (address != null) {
        connection.set("address-canonical", address.getCanonicalHostName());
        connection.set("address-host", address.getHostAddress());
        connection.set("address-hostname", address.getHostName());
      }
    }
    object.set("connection", connection);

    return object;
  }

  public static void sendPlayers() {
    TerminalNode.event("players/players", getPlayers());
  }

  public static void sendPlayer(Json client, String name) {
    TerminalNode.event("players/player", client, getPlayer(name));
  }

}

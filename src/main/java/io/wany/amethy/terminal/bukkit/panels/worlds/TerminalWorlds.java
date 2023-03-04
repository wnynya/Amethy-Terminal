package io.wany.amethy.terminal.bukkit.panels.worlds;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import io.wany.amethy.terminal.bukkit.AmethyTerminal;
import io.wany.amethy.terminal.bukkit.TerminalNode;
import io.wany.amethy.terminal.bukkit.modules.Json;

public class TerminalWorlds {

  private static final ExecutorService onEnableExecutor = Executors.newFixedThreadPool(1);
  private static final Timer onEnableTimer10s = new Timer();

  public static void onEnable() {
    addEventListener();

    onEnableExecutor.submit(() -> {
      onEnableTimer10s.schedule(new TimerTask() {
        @Override
        public void run() {
          sendWorlds();
        }
      }, 0, 10000);
    });
  }

  public static void onDisable() {
    onEnableTimer10s.cancel();
    onEnableExecutor.shutdownNow();
  }

  public static void addEventListener() {
    TerminalNode.on("worlds/world", (client, data) -> {
      sendWorld(client, data.getString("world"));
    });
    TerminalNode.on("worlds/gamerule", (client, data) -> {
      setGameRule(data.getString("world"), data.getString("gamerule"), data.getString("value"));
    });
  }

  public static Json getWorlds() {
    Json object = new Json();

    List<Json> worlds = new ArrayList<>();

    for (World world : Bukkit.getWorlds()) {
      Json data = new Json();
      data.set("name", world.getName());
      data.set("type", world.getWorldType().getName());
      data.set("environment", world.getEnvironment().name());
      data.set("seed", world.getSeed());
      worlds.add(data);
    }

    object.set("worlds", worlds);
    return object;
  }

  public static Json getWorld(String worldName) {
    Json object = new Json();
    World world = Bukkit.getWorld(worldName);
    if (world == null) {
      return object;
    }

    object.set("name", world.getName());
    object.set("type", world.getWorldType().getName());
    object.set("environment", world.getEnvironment().name());
    object.set("seed", world.getSeed());
    object.set("weather", world.hasStorm() ? world.isThundering() ? "storm" : "rain" : "clear");
    object.set("weather-duration", world.getWeatherDuration());
    object.set("fulltime", world.getFullTime());
    object.set("difficulty", world.getDifficulty().name());

    // 엔티티
    try {
      Bukkit.getScheduler().callSyncMethod(AmethyTerminal.PLUGIN, new Callable<Object>() {
        @Override
        public Integer call() {
          int all = 0;
          int players = 0;
          int animals = 0;
          int monsters = 0;
          int etc = 0;
          for (Entity entity : world.getEntities()) {
            all++;
            if (entity instanceof Player) {
              players++;
            } else if (entity instanceof Animals) {
              animals++;
            } else if (entity instanceof Monster) {
              monsters++;
            } else {
              etc++;
            }
          }
          object.set("entities-all", all);
          object.set("entities-players", players);
          object.set("entities-animals", animals);
          object.set("entities-monsters", monsters);
          object.set("entities-etc", etc);
          return null;
        }
      }).get();
    } catch (Exception ignored) {
    }

    // 청크
    try {
      object.set("chunks-loaded", world.getLoadedChunks().length);
      object.set("chunks-loaded-force", world.getForceLoadedChunks().size());
    } catch (Exception ignored) {
    }

    // 게임룰
    try {
      Json gamerules = new Json();
      for (String gr : world.getGameRules()) {
        GameRule<?> gameRule = GameRule.getByName(gr);
        if (world.getGameRuleDefault(gameRule) instanceof Integer) {
          gamerules.set(gr, (int) world.getGameRuleValue(gameRule));
        } else {
          gamerules.set(gr, (boolean) world.getGameRuleValue(gameRule));
        }
      }
      object.set("gamerules", gamerules);
    } catch (Exception ignored) {
    }

    return object;
  }

  @SuppressWarnings("unchecked")
  public static void setGameRule(String worldName, String gameRuleName, String value) {
    World world = Bukkit.getWorld(worldName);
    if (world == null) {
      return;
    }
    GameRule<?> gameRule = GameRule.getByName(gameRuleName);
    if (gameRule == null) {
      return;
    }

    if (world.getGameRuleDefault(gameRule) instanceof Integer) {
      world.setGameRule((GameRule<Integer>) gameRule, Integer.parseInt(value));
    } else {
      world.setGameRule((GameRule<Boolean>) gameRule, Boolean.valueOf(value));
    }
  }

  public static void sendWorlds() {
    TerminalNode.event("worlds/worlds", getWorlds());
  }

  public static void sendWorld(Json client, String name) {
    TerminalNode.event("worlds/world", client, getWorld(name));
  }

}

package io.wany.amethy.terminal.panels.dashboard;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.management.OperatingSystemMXBean;

import io.wany.amethy.terminal.AmethyTerminal;
import io.wany.amethy.terminal.TerminalNode;
import io.wany.amethy.terminal.modules.Json;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.help.HelpTopic;
import org.bukkit.scheduler.BukkitTask;

public class TerminalDashboard {

  private static int serverCurrentTPS = 0;
  public static int serverLastTPS = 0;

  private static final ExecutorService executorService = Executors.newFixedThreadPool(1);
  private static BukkitTask bukkitTask1t;
  private static final Timer timer1s = new Timer();

  private static Json systemInfo = null;

  public static void onEnable() {
    bukkitTask1t = Bukkit.getScheduler().runTaskTimer(AmethyTerminal.PLUGIN, () -> serverCurrentTPS++, 0L, 1L);
    executorService.submit(() -> {
      timer1s.schedule(new TimerTask() {
        @Override
        public void run() {
          serverLastTPS = serverCurrentTPS;
          serverCurrentTPS = 0;
          sendSystemStatus();
        }
      }, 0, 1000);
    });
  }

  public static void onDisable() {
    bukkitTask1t.cancel();
    timer1s.cancel();
    executorService.shutdownNow();
  }

  public static Json getSystemInfo() {
    if (systemInfo != null) {
      return systemInfo;
    }
    Json object = new Json();

    try {
      Json system = new Json();
      OperatingSystemMXBean osb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
      system.set("name", osb.getName());
      system.set("version", osb.getVersion());
      system.set("arch", osb.getArch());
      system.set("availableProcessors", osb.getAvailableProcessors());
      system.set("totalMemorySize", osb.getTotalMemorySize());
      system.set("committedVirtualMemorySize", osb.getCommittedVirtualMemorySize());
      object.set("system", system);
    } catch (Exception ignored) {
    }

    try {
      Json user = new Json();
      user.set("name", System.getProperty("user.name"));
      user.set("home", System.getProperty("user.home"));
      user.set("dir", System.getProperty("user.dir"));
      object.set("user", user);
    } catch (Exception ignored) {
    }

    try {
      Json os = new Json();
      os.set("version", System.getProperty("os.version"));
      os.set("name", System.getProperty("os.name"));
      os.set("arch", System.getProperty("os.arch"));
      object.set("os", os);
    } catch (Exception ignored) {
    }

    try {
      Json java = new Json();
      java.set("version", System.getProperty("java.vm.version"));
      java.set("runtime", System.getProperty("java.runtime.name"));
      java.set("vendor", System.getProperty("java.vm.vendor"));
      java.set("home", System.getProperty("java.home"));
      object.set("java", java);
    } catch (Exception ignored) {
    }

    try {
      Json server = new Json();
      server.set("name", Bukkit.getServer().getName());
      server.set("ip", Bukkit.getServer().getIp());
      server.set("port", Bukkit.getServer().getPort());
      server.set("maxPlayers", Bukkit.getServer().getMaxPlayers());
      server.set("version", Bukkit.getServer().getVersion());
      server.set("bukkitVersion", Bukkit.getServer().getBukkitVersion());
      server.set("motd", Bukkit.getServer().getMotd());
      server.set("dir", AmethyTerminal.SERVER_DIR.getAbsolutePath().replace("\\", "/"));
      object.set("server", server);
    } catch (Exception ignored) {
    }

    try {
      Json network = new Json();
      String ip = null;
      String hostname = null;
      try {
        ip = InetAddress.getLocalHost().toString();
        hostname = InetAddress.getLocalHost().getHostName();
      } catch (Exception ignored) {
      }
      network.set("ip", ip);
      network.set("hostname", hostname);
      List<Json> netInterfaces = new ArrayList<Json>();
      try {
        Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
        while (nics.hasMoreElements()) {
          NetworkInterface nic = nics.nextElement();
          Enumeration<InetAddress> addrs = nic.getInetAddresses();
          while (addrs.hasMoreElements()) {
            InetAddress addr = addrs.nextElement();
            Json netInterface = new Json();
            netInterface.set("name", nic.getName());
            netInterface.set("address", addr.getHostAddress());
            netInterfaces.add(netInterface);
          }
        }
      } catch (Exception ignored) {
      }
      network.set("interfaces", netInterfaces);
      object.set("network", network);
    } catch (Exception ignored) {
    }

    try {
      List<String> commands = new ArrayList<String>();
      for (HelpTopic topic : Bukkit.getHelpMap().getHelpTopics()) {
        if (!topic.getName().startsWith("/")) {
          continue;
        }
        commands.add(topic.getName().substring(1));
      }
      object.set("commands", commands);
    } catch (Exception ignored) {
    }

    systemInfo = object;
    return systemInfo;
  }

  public static Json getSystemStatus() {
    Json object = new Json();

    try {
      Runtime r = Runtime.getRuntime();
      object.set("memory-free", r.freeMemory());
      object.set("memory-max", r.maxMemory());
      object.set("memory-total", r.totalMemory());
      OperatingSystemMXBean osb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
      object.set("cpu-system-load", osb.getCpuLoad());
      object.set("cpu-process-load", osb.getProcessCpuLoad());
      object.set("tps", serverLastTPS);
    } catch (Exception ignored) {
    }

    try {
      object.set("players-count", Bukkit.getServer().getOnlinePlayers().size());
    } catch (Exception ignored) {
    }

    try {
      int entitiesCount = Bukkit.getScheduler().callSyncMethod(AmethyTerminal.PLUGIN, new Callable<Integer>() {
        @Override
        public Integer call() {
          int entitiesCount = 0;
          for (World world : Bukkit.getWorlds()) {
            entitiesCount += world.getEntities().size();
          }
          return entitiesCount;
        }
      }).get();
      object.set("entities-count", entitiesCount);
    } catch (Exception ignored) {
    }

    try {
      int chunks = 0;
      int forceChunks = 0;
      for (World world : Bukkit.getServer().getWorlds()) {
        chunks += world.getLoadedChunks().length;
        forceChunks += world.getForceLoadedChunks().size();
      }
      object.set("chunks-loaded", chunks);
      object.set("chunks-loaded-force", forceChunks);
    } catch (Exception ignored) {
    }

    try {
      long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
      object.set("uptime", uptime);
    } catch (Exception ignored) {
    }

    return object;
  }

  public static void sendSystemInfo() {
    TerminalNode.event("dashboard/systeminfo", getSystemInfo());
  }

  public static void sendSystemStatus() {
    TerminalNode.event("dashboard/systemstatus", getSystemStatus());
  }

}

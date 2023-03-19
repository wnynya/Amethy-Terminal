package io.wany.amethy.terminal.bukkit.panels.dashboard;

import io.wany.amethy.terminal.bukkit.AmethyTerminal;
import io.wany.amethy.terminal.bukkit.TerminalNode;
import io.wany.amethyst.Json;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.help.HelpTopic;
import org.bukkit.scheduler.BukkitTask;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TerminalDashboard {

  private static int TPS_CURRENT = 0;
  public static int TPS_LAST = 0;

  private static final ExecutorService onEnableExecutor = Executors.newFixedThreadPool(1);
  private static BukkitTask onEnableBukkitTask1t;
  private static final Timer onEnableTimer1s = new Timer();

  private static Json cachedSystemInfo = null;

  public static void onEnable() {
    onEnableBukkitTask1t = Bukkit.getScheduler().runTaskTimer(AmethyTerminal.PLUGIN, () -> {
      TPS_CURRENT++;
    }, 0L, 1L);

    onEnableExecutor.submit(() -> {
      onEnableTimer1s.schedule(new TimerTask() {
        @Override
        public void run() {
          TPS_LAST = TPS_CURRENT;
          TPS_CURRENT = 0;
          sendSystemStatus();
        }
      }, 0, 1000);
    });
  }

  public static void onDisable() {
    onEnableBukkitTask1t.cancel();
    onEnableTimer1s.cancel();
    onEnableExecutor.shutdownNow();
  }

  public static Json getSystemInfo() {
    if (cachedSystemInfo != null) {
      return cachedSystemInfo;
    }

    Json object = new Json();

    // 시스템 정보
    try {
      Json system = new Json();
      java.lang.management.OperatingSystemMXBean osb = ManagementFactory.getOperatingSystemMXBean();
      system.set("name", osb.getName());
      system.set("version", osb.getVersion());
      system.set("arch", osb.getArch());
      system.set("availableProcessors", osb.getAvailableProcessors());
      object.set("system", system);
    }
    catch (Exception ignored) {
    }

    // 사용자 정보
    try {
      Json user = new Json();
      user.set("name", System.getProperty("user.name"));
      user.set("home", System.getProperty("user.home"));
      user.set("dir", System.getProperty("user.dir"));
      object.set("user", user);
    }
    catch (Exception ignored) {
    }

    // OS 정보
    try {
      Json os = new Json();
      os.set("version", System.getProperty("os.version"));
      os.set("name", System.getProperty("os.name"));
      os.set("arch", System.getProperty("os.arch"));
      object.set("os", os);
    }
    catch (Exception ignored) {
    }

    // JVM 정보
    try {
      Json java = new Json();
      java.set("version", System.getProperty("java.vm.version"));
      java.set("runtime", System.getProperty("java.runtime.name"));
      java.set("vendor", System.getProperty("java.vm.vendor"));
      java.set("home", System.getProperty("java.home"));
      object.set("java", java);
    }
    catch (Exception ignored) {
    }

    // 버킷 서버 정보
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
    }
    catch (Exception ignored) {
    }

    // 네트워크 정보
    try {
      Json network = new Json();
      String ip = null;
      String hostname = null;
      try {
        ip = InetAddress.getLocalHost().toString();
        hostname = InetAddress.getLocalHost().getHostName();
      }
      catch (Exception ignored) {
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
      }
      catch (Exception ignored) {
      }
      network.set("interfaces", netInterfaces);
      object.set("network", network);
    }
    catch (Exception ignored) {
    }

    // 명령어 목록
    try {
      List<String> commands = new ArrayList<String>();
      for (HelpTopic topic : Bukkit.getHelpMap().getHelpTopics()) {
        if (!topic.getName().startsWith("/")) {
          continue;
        }
        commands.add(topic.getName().substring(1));
      }
      object.set("commands", commands);
    }
    catch (Exception ignored) {
    }

    cachedSystemInfo = object;
    return cachedSystemInfo;
  }

  @SuppressWarnings("deprecation")
  public static Json getSystemStatus() {
    Json object = new Json();

    // 서버 업타임
    try {
      long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
      object.set("uptime", uptime);
    }
    catch (Exception ignored) {
    }

    // 메모리 상태
    try {
      Runtime r = Runtime.getRuntime();
      object.set("memory-free", r.freeMemory());
      object.set("memory-max", r.maxMemory());
      object.set("memory-total", r.totalMemory());
    }
    catch (Exception ignored) {
    }

    // 프로세서 상태
    try {
      Class<?> sunOsb = Class.forName("com.sun.management.OperatingSystemMXBean");
      boolean cpuLoad = false;
      for (Method m : sunOsb.getDeclaredMethods()) {
        if (m.getName().equals("getCpuLoad")) {
          cpuLoad = true;
          break;
        }
      }
      com.sun.management.OperatingSystemMXBean osb = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
      if (cpuLoad) {
        object.set("cpu-system-load", osb.getCpuLoad());
      }
      else {
        object.set("cpu-system-load", osb.getSystemCpuLoad());
      }
      object.set("cpu-process-load", osb.getProcessCpuLoad());
    }
    catch (Throwable t) {
      java.lang.management.OperatingSystemMXBean osb = ManagementFactory.getOperatingSystemMXBean();
      object.set("cpu-system-load", osb.getSystemLoadAverage());
      object.set("cpu-process-load", osb.getSystemLoadAverage());
    }

    // 현재 TPS
    object.set("tps", TPS_LAST);

    // 플레이어 수
    object.set("players-count", Bukkit.getServer().getOnlinePlayers().size());

    // 엔티티 수
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
    }
    catch (Exception ignored) {
    }

    // 청크 수
    try {
      int chunks = 0;
      int forceChunks = 0;
      for (World world : Bukkit.getServer().getWorlds()) {
        chunks += world.getLoadedChunks().length;
        forceChunks += world.getForceLoadedChunks().size();
      }
      object.set("chunks-loaded", chunks);
      object.set("chunks-loaded-force", forceChunks);
    }
    catch (Exception ignored) {
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

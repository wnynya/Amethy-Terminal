package io.wany.amethy.terminal.bukkit.commands;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.wany.amethy.terminal.bukkit.AmethyTerminal;
import io.wany.amethy.terminal.bukkit.Console;
import io.wany.amethy.terminal.bukkit.Updater;
import io.wany.amethy.terminal.bukkit.panels.filesystem.TerminalFile;
import io.wany.amethy.terminal.bukkit.BukkitPluginLoader;

public class AmethyTerminalCommand implements CommandExecutor {

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

    if (args.length == 0) {
      // 오류: args[0] 필요
      error(sender, "Insufficient arguments");
      info(sender, "Usage: /" + label + " (version|reload|debug|update|updater)");
      return true;
    }

    switch (args[0].toLowerCase()) {

      case "version" -> {
        if (!sender.hasPermission("amethy.terminal.version")) {
          // 오류: 권한 없음
          error(sender, "You don't have permission");
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
        // 정보: 플러그인 버전
        info(sender, AmethyTerminal.NAME + " v" + AmethyTerminal.VERSION + " " + tail);
        if (tail.equals("[OUTDATED]") && sender instanceof Player player) {
        }
        return true;
      }

      case "reload" -> {
        if (!sender.hasPermission("amethy.terminal.reload")) {
          // 오류: 권한 없음
          error(sender, "You don't have permission");
          return true;
        }
        // 정보: 플러그인 리로드 시작
        info(sender, "Reloading " + AmethyTerminal.NAME + " v" + AmethyTerminal.VERSION);
        long s = System.currentTimeMillis();
        BukkitPluginLoader.unload();
        BukkitPluginLoader.load(AmethyTerminal.FILE);
        long e = System.currentTimeMillis();
        // 정보: 플러그인 리로드 완료
        info(sender, "Reload complete (" + (e - s) + "ms)");
        return true;
      }

      case "debug" -> {
        if (!sender.hasPermission("amethy.terminal.debug")) {
          // 오류: 권한 없음
          error(sender, "You don't have permission");
          return true;
        }
        boolean next = AmethyTerminal.DEBUG;
        if (args.length >= 2) {
          if (args[1].toLowerCase().equals("enable")) {
            next = true;
          } else if (args[1].toLowerCase().equals("disable")) {
            next = false;
          } else {
            // 오류: 알 수 없는 args[1]
            error(sender, "Unknown argument");
            info(sender, "Usage: /" + label + " " + args[0] + " (enable|disable)");
            return true;
          }
          AmethyTerminal.DEBUG = next;
          AmethyTerminal.CONFIG.set("debug", AmethyTerminal.DEBUG);
          // 정보: 변경된 디버그 메시지 표시 여부
          info(sender, "Debug " + (next ? "en" : "dis") + "abled");
          return true;
        } else {
          // 정보: 현재 디버그 메시지 표시 여부
          info(sender, "Debug is currently " + (next ? "en" : "dis") + "abled");
          return true;
        }
      }

      case "update" -> {
        if (!sender.hasPermission("amethy.terminal.updater.update")) {
          // 오류: 권한 없음
          error(sender, "You don't have permission");
          return true;
        }
        String version;
        try {
          version = Updater.getLatest();
        } catch (Exception e) {
          // 오류: 버전 확인 실패
          error(sender, "Version check failed " + e.getMessage());
          return true;
        }
        if (AmethyTerminal.VERSION.equals(version)) {
          if (args.length >= 2 && args[1].toLowerCase().equals("-force")) {
          } else {
            // 경고: 이미 최신 버전임
            warn(sender, "It's already the latest version");
            warn(sender, "Use -force flag to update force");
            return true;
          }
        }
        // 정보: 플러그인 버전
        info(sender, "Found newer version of plugin");
        info(sender, "  Current: " + AmethyTerminal.NAME + " v" + AmethyTerminal.VERSION);
        info(sender, "  Latest: " + AmethyTerminal.NAME + " v" + version);
        // 정보: 파일 다운로드 시작
        info(sender, "Downloading file...");
        File file;
        try {
          file = Updater.download(version);
        } catch (Exception e) {
          // 오류: 파일 다운로드 실패
          error(sender, "File download failed " + e.getMessage());
          return true;
        }
        // 정보: 파일 다운로드 완료
        info(sender, "Download complete");
        // 정보: 플러그인 업데이트 시작
        info(sender, "Updating plugin...");
        try {
          Updater.update(file, version);
        } catch (Exception e) {
          // 오류: 업데이트 실패
          error(sender, "Plugin update failed " + e.getMessage());
          return true;
        }
        // 정보: 업데이트 완료
        info(sender, "Update complete");
        return true;
      }

      case "updater" -> {
        if (!sender.hasPermission("amethy.terminal.updater")) {
          // 오류: 권한 없음
          error(sender, "You don't have permission");
          return true;
        }

        if (args.length >= 2) {
          if (args[1].toLowerCase().equals("automation")) {
            boolean next = Updater.AUTOMATION;
            if (args.length >= 3) {
              if (args[2].toLowerCase().equals("enable")) {
                next = true;
              } else if (args[2].toLowerCase().equals("disable")) {
                next = false;
              } else {
                // 오류: 알 수 없는 args[2]
                error(sender, "Unknown argument");
                info(sender, "Usage: /" + label + " " + args[0] + " " + args[1] + " [enable|disable]");
                return true;
              }
              Updater.AUTOMATION = next;
              AmethyTerminal.CONFIG.set("updater.automation", Updater.AUTOMATION);
              // 정보: 변경된 업데이터 자동화 여부
              info(sender, "Updater automation " + (next ? "en" : "dis") + "abled");
              return true;
            } else {
              // 정보: 현재 업데이터 자동화 여부
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
                // 오류: 알 수 없는 args[2]
                error(sender, "Unknown argument");
                info(sender, "Usage: /" + label + " " + args[0] + " " + args[1] + " [release|dev]");
                return true;
              }
              Updater.CHANNEL = next;
              AmethyTerminal.CONFIG.set("updater.channel", Updater.CHANNEL);
              // 정보: 변경된 업데이터 채널
              info(sender, "Updater channel changed to " + next);
              return true;
            } else {
              // 정보: 현재 업데이터 채널
              info(sender, "Current updater channel is " + Updater.CHANNEL);
              return true;
            }
          } else {
            // 오류: 알 수 없는 args[1]
            error(sender, "Unknown argument");
            info(sender, "Usage: /" + label + " " + args[0] + " (channel|automation)");
            return true;
          }
        } else {
          // 오류: args[1] 필요
          error(sender, "Insufficient arguments");
          info(sender, "Usage: /" + label + " " + args[0] + " (channel|automation)");
          return true;
        }
      }

      case "test" -> {
        if (!sender.hasPermission("amethy.terminal.test")) {
          // 오류: 권한 없음
          error(sender, "You don't have permission");
          return true;
        }

        try {
          TerminalFile tfo = new TerminalFile(new File("eula.txt"));
          TerminalFile tfc = new TerminalFile(new File("eula-copy.txt"), tfo.chunks());
          tfo.read((index, buffer) -> {
            Console.debug(index + ": " + buffer);
            tfc.write(index, buffer);
          });
        } catch (Exception e) {
          e.printStackTrace();
        }

        return true;
      }

      case "test2" -> {
        if (!sender.hasPermission("amethy.terminal.test")) {
          // 오류: 권한 없음
          error(sender, "You don't have permission");
          return true;
        }

        try {
          TerminalFile tfc = new TerminalFile(new File("eula-copy.txt"), 7);
          tfc.write(2,
              "7J2067iU6rO864qUCiDri6TrpbgsIO2KueydtO2VnCDrsKnsi53snLzroZwg7J6R64+Z7ZWc64ukLiDrtoDtkogg7JWE656Y7JeQIOyKpO2UhOungeydtCDri6zroKQg7J6I7Ja0IOqwgOyatOuNsCDqtazrqY3snZgg7YGs6riw6rCAIOyekeydgCAxMuyduOy5mOuCmCAxMOyduOy5mOydmCDsnYzrsJjsnYAg67aA7ZKI7J2EIOq3uOuMgOuhnCDriIzrn6wg7ZqM7KCE7YyQ7JeQIOyYrOumtCDsiJgg7J6ICiDri6QuIOuwmOuptCDqsIDsmrTrjbAg6rWs6w==");
          tfc.write(3,
              "qY3snZgg7YGs6riw6rCAIO2BsCA37J247LmYIOuPhOuEmyDtmJXtg5zsnZgg7J2M67CY7J2AIOq4iOyGjSDrtoDtkojsnbQg64iM65+s7KC4IOuTpOyWtOqwgOyngCDslYrqs6Ag6re464yA66GcIOyeiOyWtCDsnbzrsJjsoIHsnbgg7YS07YWM7J2067iU7JeQ7ISc7J2YIDfsnbjsuZgg7Ja0646B7YSwIOyXre2VoOydhCDrjIDsi6DtlZzri6QuIAog7J20IOygnO2SiOydmCDsm5Drnpgg7Lm07Yq466as7KeA64qUIOyWkeuptOyduOuNsCDqsIHqsIEgTA==");
          tfc.write(0,
              "ZXVsYT10cnVlCu2EtO2FjOydtOu4lOydgCDruIzrnbzsmrQgUEMgMyDsoJztkojqs7wg6rCZ7J2AIO2Yle2DnOydtOuLpC4g7ZqM7KCEIOyGjeuPhOuKlCAxNiAvIDMzIC8gNDUgLyA3OCBSUE3snYQg7KeA7JuQ7ZWcCuuLpC4g7Jik66W47Kq9IOybkOuwmCDtmJXtg5zsnZgg66CI67KE66W8IOybgOyngeyXrCDsho3rj4Trpbwg7KGw7KCV7ZWgIOyImCDsnojri6QuIO2GpOyVlOydgCDrs4Trj4TsnZgg7Lmo7JWVIOyhsOyglSDquLDriqXsnYAg7JeG6g==");
          tfc.write(5,
              "67CY7KCB7J24IExQIOyaqSDshLjrnbzrr7kg7Lm07Yq466as7KeA66GcIOqwnOyhsCDrkJwg7IOB7YOc6528IOq4sOuKpeydhCDsoITrtoAg7IKs7Jqp7ZWY6riwIOychO2VtOyEoCDsm5DrnpjrjIDroZzsnZgg67O17JuQ7J20IO2VhOyalO2VmOuLpC4K6rCB6rCBIDEy7J247LmYIExQIC8gMTDsnbjsuZggU1AgLyA37J247LmYIOyLseq4gOydhCDsmKzroKQg65GUIOuqqOyKtS4g642u6rCc66W8IOuNruycvOuptCAxMuyduOy5mCDsnYzrsJjrj4Qg6w==");
          tfc.write(6,
              "gZ3rtoDrtoTsnbQg642u6rCc7JeQIOu2gOuUqu2eiOyngCDslYrqs6Ag7KCV7ZmV7ZWY6rKMIOuTpOyWtOunnuuKlOuLpC4=");
          tfc.write(1,
              "s6AsIO2YhOuMgOydmCDthLTthYzsnbTruJTrs7Tri6Qg67mE6rWQ7KCBIOustOqxsOyatCDsoJXrj4QK7J2064ukLiDtmozsoITtjJDsnZgg7YWM65GQ66as7JeQ64qUIOydjOuwmOydhCDsnqHslYQg7KO86riwIOychO2VnCDqs6DrrLTqsIAg7Jik6rCB7ZiV7J2YIOygkCDtmJXtg5zroZwg67aA7LCp65CY7Ja0IOyeiOuLpC4g6rCA7Jq0642wIOyeiOuKlCDsgrzqsIEg67Cp7IKs7ZiV7J2YIOq4iOyGjSDrtoDtkojsnYAg67O07Ya17J2YIO2EtO2FjA==");
          tfc.write(4,
              "UCDsmYAgU1Ag7J2M67CYIOyerOyDneydhCDsnITtlbQg64uk66W4IOyerOyniOqzvCDtmJXtg5zsnZgg67CU64qY66GcIOq1rOyEseuQmOyWtCDsnojri6QuIOydtCDrsJTripjsnYAg7Yak7JWUIOuBneydmCDsnpHsnYAg64W467iM66W8IOuPjOugpCDqsIHqsIHsnZgg67CU64qY66GcIOuwlOq+uOyWtCDsk7gg7IiYIOyeiOuLpC4gCuyCrOynhOydmCDsoJztkojsnYAg7Lm07Yq466as7KeA6rCAIOydtOyghCDshozsnKDsnpDsl5Ag7J2Y7ZW0IOydvA==");
        } catch (Exception e) {
          e.printStackTrace();
        }

        return true;
      }

      default -> {
        // 오류 알 수 없는 args[0]
        error(sender, "Unknown argument");
        info(sender, "Usage: /" + label + " (version|reload|debug|update|updater)");
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
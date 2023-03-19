package io.wany.amethy.terminal.bukkit.commands;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.wany.amethy.terminal.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AmethyTerminalCommand implements CommandExecutor {

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

    if (args.length == 0) {
      // 오류: args[0] 필요
      error(sender, Message.ERROR.INSUFFICIENT_ARGS);
      info(sender, "사용법: /" + label + " (version|reload|debug|update|updater)");
      return true;
    }

    switch (args[0].toLowerCase()) {

      case "test": {
        try {
          Class<?> c = Class.forName("io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler");
          Field ins = c.getField("INSTANCE");
          ins.setAccessible(true);
          Field f = c.getDeclaredField("storage");
          f.setAccessible(true);
          Object o = f.get(ins.get(null));

          HashMap<?, ?> w = (HashMap<?, ?>) o;

          Class<?> simple = Class.forName("io.papermc.paper.plugin.storage.SimpleProviderStorage");

          Object sto = null;
          List<Object> set = Collections.singletonList(w.entrySet());
          for (int i = 0; i < set.size(); i++) {
            Object value = set.get(i);
            if (value.toString().contains("SimpleProviderStorage")) {
              sto = value;
              break;
            }
          }

          if (sto == null ){
            console.log("sto null");
            return true;
          }

          console.log(sto.getClass().getName());

          Object aaa = simple.cast(sto);

          console.log(aaa.toString());

          sto.getClass().getDeclaredField("providers");

        } catch (Throwable t) {
          t.printStackTrace();
        }
        return true;
      }

      // 플러그인 버전 확인
      case "version": {
        if (!sender.hasPermission("amethy.terminal.version")) {
          // 오류: 권한 없음
          error(sender, Message.ERROR.NO_PERM);
          return true;
        }
        String tail;
        try {
          if (Updater.isLatest()) {
            tail = "[최신 버전]";
          } else {
            tail = "[업데이트 가능]";
          }
        } catch (Exception e) {
          tail = "[버전 확인 실패]";
        }
        // 정보: 플러그인 버전
        info(sender, AmethyTerminal.NAME + " v" + AmethyTerminal.VERSION + " §o" + tail);
        return true;
      }

      // 플러그인 리로드
      case "reload": {
        if (!sender.hasPermission("amethy.terminal.reload")) {
          // 오류: 권한 없음
          error(sender, Message.ERROR.NO_PERM);
          return true;
        }
        // 정보: 플러그인 리로드 시작
        info(sender, AmethyTerminal.NAME + " v" + AmethyTerminal.VERSION + " 플러그인을 리로드합니다.");
        long s = System.currentTimeMillis();
        PluginLoader.unload(AmethyTerminal.PLUGIN);
        PluginLoader.load(AmethyTerminal.FILE);
        long e = System.currentTimeMillis();
        // 정보: 플러그인 리로드 완료
        info(sender, "리로드 완료. (" + (e - s) + "ms)");
        return true;
      }

      // 플러그인 디버그 메시지 설정
      case "debug": {
        if (!sender.hasPermission("amethy.terminal.debug")) {
          // 오류: 권한 없음
          error(sender, Message.ERROR.NO_PERM);
          return true;
        }
        boolean next = AmethyTerminal.DEBUG;
        if (args.length >= 2) {
          if (args[1].equalsIgnoreCase("enable")) {
            next = true;
          } else if (args[1].equalsIgnoreCase("disable")) {
            next = false;
          } else {
            // 오류: 알 수 없는 args[1]
            error(sender, Message.ERROR.UNKNOWN_ARG);
            info(sender, "사용법: /" + label + " " + args[0] + " (enable|disable)");
            return true;
          }
          AmethyTerminal.DEBUG = next;
          AmethyTerminal.CONFIG.set("debug", AmethyTerminal.DEBUG);
          // 정보: 변경된 디버그 메시지 표시 여부
          info(sender, "디버그 메시지 출력이 " + (next ? "" : "비") + "활성화되었습니다.");
        } else {
          // 정보: 현재 디버그 메시지 표시 여부
          info(sender, "현재 디버그 메시지 출력은 " + (next ? "" : "비") + "활성화되어 있습니다.");
        }
        return true;
      }

      // 플러그인 업데이트
      case "update": {
        if (!sender.hasPermission("amethy.terminal.updater.update")) {
          // 오류: 권한 없음
          error(sender, Message.ERROR.NO_PERM);
          return true;
        }
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(() -> {
          String version;
          try {
            version = Updater.getLatest();
          } catch (Exception e) {
            // 오류: 버전 확인 실패
            error(sender, "버전 확인 실패. (" + e.getMessage() + ")");
            executor.shutdown();
            return true;
          }
          if (AmethyTerminal.VERSION.equals(version)) {
            if (!(args.length >= 2 && args[1].equalsIgnoreCase("-force"))) {
              // 경고: 이미 최신 버전임
              warn(sender, "이미 플러그인이 최신 버전입니다.");
              warn(sender, "강제로 업데이트하려면 -force 플래그를 사용하십시오.");
              executor.shutdown();
              return true;
            }
          }
          // 정보: 플러그인 버전
          info(sender, "플러그인의 최신 버전을 발견했습니다.");
          info(sender, "  현재 버전: " + AmethyTerminal.NAME + " v" + AmethyTerminal.VERSION);
          info(sender, "  최신 버전: " + AmethyTerminal.NAME + " v" + version);
          // 정보: 파일 다운로드 시작
          info(sender, "파일 다운로드 중...");
          File file;
          try {
            file = Updater.download(version);
          } catch (Exception e) {
            // 오류: 파일 다운로드 실패
            error(sender, "파일 다운로드 실패. (" + e.getMessage() + ")");
            executor.shutdown();
            return true;
          }
          // 정보: 파일 다운로드 완료
          info(sender, "파일 다운로드 완료.");
          // 정보: 플러그인 업데이트 시작
          info(sender, "플러그인 업데이트 중...");
          try {
            Updater.update(file, version);
          } catch (Exception e) {
            // 오류: 업데이트 실패
            error(sender, "플러그인 업데이트 실패. (" + e.getMessage() + ")");
            executor.shutdown();
            return true;
          }
          // 정보: 업데이트 완료
          info(sender, "업데이트 완료.");
          executor.shutdown();
          return true;
        });

        executor.shutdown();
        return true;
      }

      // 플러그인 업데이터 설정
      case "updater": {
        if (!sender.hasPermission("amethy.terminal.updater")) {
          // 오류: 권한 없음
          error(sender, Message.ERROR.NO_PERM);
          return true;
        }

        if (args.length >= 2) {
          if (args[1].equalsIgnoreCase("automation")) {
            boolean next = Updater.AUTOMATION;
            if (args.length >= 3) {
              if (args[2].equalsIgnoreCase("enable")) {
                next = true;
              } else if (args[2].equalsIgnoreCase("disable")) {
                next = false;
              } else {
                // 오류: 알 수 없는 args[2]
                error(sender, Message.ERROR.UNKNOWN_ARG);
                info(sender, "사용법: /" + label + " " + args[0] + " " + args[1] + " [enable|disable]");
                return true;
              }
              Updater.AUTOMATION = next;
              AmethyTerminal.CONFIG.set("updater.automation", Updater.AUTOMATION);
              // 정보: 변경된 업데이터 자동화 여부
              info(sender, "업데이터 자동화가 " + (next ? "" : "비") + "활성화되었습니다.");
            } else {
              // 정보: 현재 업데이터 자동화 여부
              info(sender, "현재 업데이트 자동화가 " + (next ? "" : "비") + "활성화되어 있습니다.");
            }
            return true;
          } else if (args[1].equalsIgnoreCase("channel")) {
            String next;
            if (args.length >= 3) {
              if (args[2].equalsIgnoreCase("release")) {
                next = "release";
              } else if (args[2].equalsIgnoreCase("dev")) {
                next = "dev";
              } else {
                // 오류: 알 수 없는 args[2]
                error(sender, Message.ERROR.UNKNOWN_ARG);
                info(sender, "사용법: /" + label + " " + args[0] + " " + args[1] + " [release|dev]");
                return true;
              }
              Updater.CHANNEL = next;
              AmethyTerminal.CONFIG.set("updater.channel", Updater.CHANNEL);
              // 정보: 변경된 업데이터 채널
              info(sender, "업데이터 채널이 " + next + " 채널로 변경되었습니다.");
            } else {
              // 정보: 현재 업데이터 채널
              info(sender, "현재 업데이터 채널은 " + Updater.CHANNEL + " 채널입니다.");
            }
            return true;
          } else {
            // 오류: 알 수 없는 args[1]
            error(sender, Message.ERROR.UNKNOWN_ARG);
            info(sender, "사용법: /" + label + " " + args[0] + " (channel|automation)");
            return true;
          }
        } else {
          // 오류: args[1] 필요
          error(sender, Message.ERROR.INSUFFICIENT_ARGS);
          info(sender, "사용법: /" + label + " " + args[0] + " (channel|automation)");
          return true;
        }
      }

      // 터미널 권한 부여
      case "grant": {
        if (!(sender instanceof Player)) {
          // 오류: 콘솔 명령어로만 사용 가능
          error(sender, Message.ERROR.ONLY_CONSOLE);
          return true;
        }

        if (!sender.hasPermission("amethy.terminal.grant")) {
          // 오류: 권한 없음
          error(sender, Message.ERROR.NO_PERM);
          return true;
        }

        if (args.length >= 2) {
          String aid = args[1];
          if (TerminalNodeAPI.grant(aid)) {
            // 정보: 권한 부여 성공
            info(sender, "터미널 권한이 성공적으로 부여되었습니다.");
          } else {
            // 오류: 권한 부여 실패
            error(sender, "터미널 권한 부여에 실패하였습니다.");
          }
        } else {
          // 오류: args[1] 필요
          error(sender, Message.ERROR.INSUFFICIENT_ARGS);
          info(sender, "사용법: /" + label + " " + args[0] + " (channel|automation)");
        }
        return true;
      }

      default: {
        // 오류 알 수 없는 args[0]
        error(sender, Message.ERROR.UNKNOWN_ARG);
        info(sender, "사용법: /" + label + " (version|reload|debug|update|updater)");
        return true;
      }

    }

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
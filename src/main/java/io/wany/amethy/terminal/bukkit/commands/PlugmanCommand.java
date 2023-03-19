package io.wany.amethy.terminal.bukkit.commands;

import io.wany.amethy.terminal.bukkit.AmethyTerminal;
import io.wany.amethy.terminal.bukkit.Message;
import io.wany.amethy.terminal.bukkit.Updater;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlugmanCommand implements CommandExecutor {

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {


    if (args.length == 0) {
      // 오류: args[0] 필요
      error(sender, Message.ERROR.INSUFFICIENT_ARGS);
      info(sender, "사용법: /" + label + " (version|reload|debug|update|updater)");
      return true;
    }

    switch (args[0].toLowerCase()) {

      // 플러그인 로드
      case "load": {
        if (!sender.hasPermission("plugman.load")) {
          // 오류: 권한 없음
          error(sender, Message.ERROR.NO_PERM);
          return true;
        }
        if (args.length >= 2) {
          File[] files = AmethyTerminal.PLUGINS_DIR.listFiles();
          HashMap<String, File> filesMap = new HashMap<>();
          for (int i = 0; i < files.length; i++) {

          }
          if (args[1].equalsIgnoreCase("automation")) {
            ExecutorService executor = Executors.newFixedThreadPool(1);
            executor.submit(() -> {

            });

            executor.shutdown();
          }
          else {
            // 오류: 알 수 없는 args[1]
            error(sender, Message.ERROR.UNKNOWN_ARG);
            info(sender, "사용법: /" + label + " " + args[0] + " (channel|automation)");
            return true;
          }
        }
        else {
          // 오류: args[1] 필요
          error(sender, Message.ERROR.INSUFFICIENT_ARGS);
          info(sender, "사용법: /" + label + " " + args[0] + " <plugin>");
          return true;
        }
      }

      // 플러그인 언로드
      case "version": {

      }

      // 플러그인 리로드
      case "reload": {

      }

      // 플러그인 디버그 메시지 설정
      case "debug": {

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
          }
          catch (Exception e) {
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
          }
          catch (Exception e) {
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
          }
          catch (Exception e) {
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

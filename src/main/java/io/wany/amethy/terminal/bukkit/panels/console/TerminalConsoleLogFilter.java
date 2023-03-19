package io.wany.amethy.terminal.bukkit.panels.console;

import io.wany.amethy.terminal.bukkit.TerminalNode;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

import java.util.concurrent.TimeUnit;

public class TerminalConsoleLogFilter implements Filter {

  public boolean disabled = false;

  public void disable() {
    disabled = true;
  }

  @Override
  public Result filter(LogEvent event) {
    // 필터 비활성화 시
    if (disabled) {
      return null;
    }
    try {
      // 로그 메시지 가져오기
      String message = event.getMessage().getFormattedMessage();

      // 로그에 스택이 있을 경우
      StringBuilder stack = new StringBuilder();
      Throwable thrown = event.getThrown();
      if (thrown != null) {
        stack.append("\r\n");
        stack.append(thrown.getClass().getName()).append(" => ");
        stack.append(thrown.getMessage());
        StackTraceElement[] stea = thrown.getStackTrace();
        for (StackTraceElement ste : stea) {
          stack.append("\r\n\tat ").append(ste.getFileName()).append(":").append(ste.getLineNumber()).append(" (").append(ste.getClassName()).append(".").append(ste.getMethodName()).append(")");
        }
        Throwable thrownCause = thrown.getCause();
        while (thrownCause != null) {
          stack.append("\r\nCaused by: ");
          stack.append(thrownCause.getClass().getName());
          StackTraceElement[] cstea = thrownCause.getStackTrace();
          for (StackTraceElement ste : cstea) {
            stack.append("\r\n\tat ").append(ste.getFileName()).append(":").append(ste.getLineNumber()).append(" (").append(ste.getClassName()).append(".").append(ste.getMethodName()).append(")");
          }
          thrownCause = thrownCause.getCause();
        }
      }
      message += stack.toString();

      // 로깅 시각
      long time = event.getTimeMillis();

      // 로그 스레드 이름
      String thread = event.getThreadName();

      // 로그 레벨
      String level = event.getLevel().name();

      // 로그 로거 이름
      String logger = event.getLoggerName();
      String loggerFqcn = event.getLoggerFqcn();

      // 로그 마커
      String marker = event.getMarker() != null ? event.getMarker().getName() : "";

      // 로그 호출 소스
      String source = "";
      if (event.getSource() != null) {
        source += event.getSource().getFileName() + ":";
        source += event.getSource().getLineNumber() + "@";
        source += event.getSource().getClassName() + ".";
        source += event.getSource().getMethodName();
      }

      try {
        TimeUnit.MICROSECONDS.sleep(1);
      }
      catch (InterruptedException ignored) {
      }

      // 로그 오브젝트 만들기
      TerminalConsole.Log log = new TerminalConsole.Log(message, time, level, thread, logger, loggerFqcn, marker, source);

      // 터미널이 열려 있는 경우
      if (TerminalNode.isOpened()) {
        // 오프라인 로그 스택이 비어 있는 경우 -> 로그 전송
        if (TerminalConsole.offlineLogs.size() <= 0) {
          TerminalConsole.log(log);
        }
        // 오프라인 로그 스택이 차 있는 경우 -> 오프라인 로그부터 전송
        else {
          TerminalConsole.offlineLogs.add(log);
          TerminalConsole.logOffline();
        }
      }
      // 터미널이 닫혀 있는 경우 -> 오프라인 로그 스택에 넣음
      else {
        TerminalConsole.offlineLogs.add(log);
      }

    }
    catch (Exception ignored) {
      // 로그 필터에서 예외가 발생하면 -> 서버가 폭발함
    }
    return null;
  }

  @Override
  public State getState() {
    return null;
  }

  @Override
  public void initialize() {

  }

  @Override
  public boolean isStarted() {
    return false;
  }

  @Override
  public boolean isStopped() {
    return false;
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {

  }

  @Override
  public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
    return null;
  }

  @Override
  public Result filter(Logger logger, Level level, Marker marker, String message, Object p0) {
    return null;
  }

  @Override
  public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
    return null;
  }

  @Override
  public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
    return null;
  }

  @Override
  public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1) {
    return null;
  }

  @Override
  public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
    return null;
  }

  @Override
  public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
    return null;
  }

  @Override
  public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
    return null;
  }

  @Override
  public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
    return null;
  }

  @Override
  public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
    return null;
  }

  @Override
  public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
    return null;
  }

  @Override
  public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
    return null;
  }

  @Override
  public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
    return null;
  }

  @Override
  public Result getOnMatch() {
    return null;
  }

  @Override
  public Result getOnMismatch() {
    return null;
  }

}

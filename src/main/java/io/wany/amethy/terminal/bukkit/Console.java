package io.wany.amethy.terminal.bukkit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Console {

  private static void log(Level level, String message) {
    Logger logger = (Logger) LogManager.getRootLogger();
    logger.log(level, message, message, message);
  }

  public static void info(String message) {
    log(Level.INFO, AmethyTerminal.PREFIX_CONSOLE + message + "\u001b[0m");
  }

  public static void warn(String message) {
    log(Level.WARN, AmethyTerminal.PREFIX_CONSOLE + "\u001b[93m" + message + "\u001b[0m");
  }

  public static void error(String message) {
    log(Level.ERROR, AmethyTerminal.PREFIX_CONSOLE + "\u001b[91m" + message + "\u001b[0m");
  }

  public static void fatal(String message) {
    log(Level.FATAL, AmethyTerminal.PREFIX_CONSOLE + "\u001b[97;41m" + message + "\u001b[0m");
  }

  public static void debug(String message) {
    if (AmethyTerminal.DEBUG) {
      log(Level.INFO, AmethyTerminal.PREFIX_CONSOLE + "[DEBUG] " + message + "\u001b[0m");
    }
  }

  public static void log(String message) {
    info(message);
  }

}

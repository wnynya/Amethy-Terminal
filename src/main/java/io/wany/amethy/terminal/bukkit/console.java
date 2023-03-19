package io.wany.amethy.terminal.bukkit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class console {

  private static void log(Level level, String message) {
    Logger logger = LogManager.getRootLogger();
    logger.log(level, message, message, message);
  }

  private static String process(String message) {
    if (message.startsWith(AmethyTerminal.PREFIX)) {
      message = message.substring(AmethyTerminal.PREFIX.length());
    };
    message = message.replaceAll("\\[([^]]+)]:", "[$1]");

    // Minecraft formatting codes
    Pattern mfchex = Pattern.compile("§x((§[0-9a-fA-F]){2}){3}");
    Matcher mfchexMacher = mfchex.matcher(message);
    while (mfchexMacher.find()) {
      String find = mfchexMacher.group(0);
      String rx = find.charAt(3) + "" + find.charAt(5);
      String gx = find.charAt(7) + "" + find.charAt(9);
      String bx = find.charAt(11) + "" + find.charAt(13);
      int r = Integer.parseInt(rx, 16);
      int g = Integer.parseInt(gx, 16);
      int b = Integer.parseInt(bx, 16);
      message = message.replaceAll(find, "\u001b[38;2;" + r + ";" + g + ";" + b + "m");
    }

    message = message.replaceAll("§r", "\u001b[0m"); /*  Reset */
    message = message.replaceAll("§l", "\u001b[1m"); /*  Bold */
    message = message.replaceAll("§m", "\u001b[9m"); /*  Strike */
    message = message.replaceAll("§n", "\u001b[4m"); /*  Underline */
    message = message.replaceAll("§o", "\u001b[3m"); /*  Italic */
    message = message.replaceAll("§k", "\u001b[7m"); /*  Random */

    message = message.replaceAll("§0", "\u001b[0m\u001b[30m");
    message = message.replaceAll("§4", "\u001b[0m\u001b[31m");
    message = message.replaceAll("§2", "\u001b[0m\u001b[32m");
    message = message.replaceAll("§6", "\u001b[0m\u001b[33m");
    message = message.replaceAll("§1", "\u001b[0m\u001b[34m");
    message = message.replaceAll("§5", "\u001b[0m\u001b[35m");
    message = message.replaceAll("§3", "\u001b[0m\u001b[36m");
    message = message.replaceAll("§7", "\u001b[0m\u001b[37m");
    message = message.replaceAll("§8", "\u001b[0m\u001b[90m");
    message = message.replaceAll("§c", "\u001b[0m\u001b[91m");
    message = message.replaceAll("§a", "\u001b[0m\u001b[92m");
    message = message.replaceAll("§e", "\u001b[0m\u001b[93m");
    message = message.replaceAll("§9", "\u001b[0m\u001b[94m");
    message = message.replaceAll("§d", "\u001b[0m\u001b[95m");
    message = message.replaceAll("§b", "\u001b[0m\u001b[96m");
    message = message.replaceAll("§f", "\u001b[0m\u001b[97m");
    
    return message;
  }

  public static void info(String message) {
    log(Level.INFO, AmethyTerminal.PREFIX_CONSOLE + process(message) + "\u001b[0m");
  }

  public static void warn(String message) {
    log(Level.WARN, AmethyTerminal.PREFIX_CONSOLE + "\u001b[93m" + process(message) + "\u001b[0m");
  }

  public static void error(String message) {
    log(Level.ERROR, AmethyTerminal.PREFIX_CONSOLE + "\u001b[91m" + process(message) + "\u001b[0m");
  }

  public static void fatal(String message) {
    log(Level.FATAL, AmethyTerminal.PREFIX_CONSOLE + "\u001b[97;41m" + process(message) + "\u001b[0m");
  }

  public static void debug(String message) {
    if (AmethyTerminal.DEBUG) {
      log(Level.INFO, AmethyTerminal.PREFIX_CONSOLE + "[DEBUG] " + process(message) + "\u001b[0m");
    }
  }

  public static void log(String message) {
    info(message);
  }

}

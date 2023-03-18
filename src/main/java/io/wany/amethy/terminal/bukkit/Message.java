package io.wany.amethy.terminal.bukkit;

import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class Message {

  public static BaseComponent[] of(Object... objects) {
    ComponentBuilder component = new ComponentBuilder("");
    for (Object object : objects) {
      if (object instanceof String) {
        component.append(new TranslatableComponent((String) object));
      } else {
        component.append(object.toString());
      }
    }
    return component.create();
  }

  public static void send(Object audience, String prefix, String level, Object... objects) {
    prefix = prefix == null ? "" : prefix;
    level = level == null ? "" : level;
    Object[] objs = new Object[objects.length + 2];
    objs[0] = prefix;
    objs[1] = level;
    System.arraycopy(objects, 0, objs, 2, objects.length);
    if (audience instanceof CommandSender) {
      ((CommandSender) audience).spigot().sendMessage(Message.of(objs));
    }
  }

  public static void send(Object audience, String prefix, Object... objects) {
    send(audience, prefix, null, objects);
  }

  public static void send(Object audience, Object... objects) {
    send(audience, null, null, objects);
  }

  public static void info(CommandSender sender, String prefix, Object... objects) {
    send(sender, prefix, objects);
  }

  public static void warn(CommandSender sender, String prefix, Object... objects) {
    send(sender, prefix, "§e§l[경고]: ", objects);
  }

  public static void error(CommandSender sender, String prefix, Object... objects) {
    send(sender, prefix, "§e§l[오류]: ", objects);
  }

  public static class ERROR {
    public static final String INSUFFICIENT_ARGS = "명령어 인자가 부족합니다.";
    public static final String NO_PERM = "명령어를 사용할 수 있는 권한이 없습니다.";
    public static final String UNKNOWN_ARG = "알 수 없는 명령어 인자입니다.";
    public static final String ONLY_CONSOLE = "서버 콘솔에서만 사용 가능한 명령어입니다.";
    public static final String ONLY_PLAYER = "플레이어만 사용 가능한 명령어입니다.";
  }

}

package io.wany.amethy.terminal.bukkit;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

public class Message {

  public static Component of(Object... objects) {
    return ComponentUtil.create(objects);
  }

  public static String stringify(Component component) {
    return GsonComponentSerializer.gson().serialize(component);
  }

  public static Component parse(String string) {
    return GsonComponentSerializer.gson().deserialize(string);
  }

  public static void send(Audience audience, String prefix, String level, Object... objects) {
    prefix = prefix == null ? "" : prefix;
    level = level == null ? "" : level;
    Object[] objs = new Object[objects.length + 2];
    objs[0] = prefix;
    objs[1] = level;
    System.arraycopy(objects, 0, objs, 2, objects.length);
    audience.sendMessage(Message.of(objs));
  }

  public static void send(Audience audience, String prefix, Object... objects) {
    send(audience, prefix, null, Message.of(objects));
  }

  public static void send(Audience audience, Object... objects) {
    send(audience, null, null, Message.of(objects));
  }

  public static void info(Audience audience, String prefix, Object... objects) {
    send(audience, prefix, objects);
  }

  public static void warn(Audience audience, String prefix, Object... objects) {
    send(audience, prefix, "§e§l[경고]: ", objects);
  }

  public static void error(Audience audience, String prefix, Object... objects) {
    send(audience, prefix, "§e§l[오류]: ", objects);
  }

}

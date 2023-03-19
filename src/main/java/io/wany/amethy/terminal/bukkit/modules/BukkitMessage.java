package io.wany.amethy.terminal.bukkit.modules;

import io.wany.amethy.terminal.bukkit.Message;
import io.wany.amethy.terminal.bukkit.console;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class BukkitMessage implements Message {

  @Override
  public Object of(Object... objects) {
    StringBuilder builder = new StringBuilder();
    for (Object obj : objects) {
      if (obj instanceof String) {
        builder.append((String) obj);
      }
      else {
        builder.append(obj != null ? obj.toString() : "null");
      }
    }
    return builder.toString();
  }

  @Override
  public void send(Object audience, String a1, String a2, Object... o) {
    a1 = a1 == null ? "" : a1;
    a2 = a2 == null ? "" : a2;
    Object[] objs = new Object[o.length + 2];
    objs[0] = a1;
    objs[1] = a2;
    System.arraycopy(o, 0, objs, 2, o.length);
    CommandSender sender = (CommandSender) audience;
    String string = (String) of(objs);
    if (sender instanceof ConsoleCommandSender) {
      console.log(string);
    }
    else {
      sender.sendMessage(string);
    }
  }

}

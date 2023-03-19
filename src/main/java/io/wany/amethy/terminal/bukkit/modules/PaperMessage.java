package io.wany.amethy.terminal.bukkit.modules;

import io.wany.amethy.terminal.bukkit.Message;
import io.wany.amethy.terminal.bukkit.console;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.ConsoleCommandSender;

public class PaperMessage implements Message {

  @Override
  public Object of(Object... objects) {
    Component component = Component.empty();
    for (Object obj : objects) {
      if (obj instanceof Component) {
        component = component.append((Component) obj);
      }
      else if (obj instanceof String) {
        String str = (String) obj;
        component = component.append(LegacyComponentSerializer.legacySection().deserialize(str));
      }
      else {
        component = component.append(Component.translatable(obj != null ? obj.toString() : "null"));
      }
    }
    return component;
  }

  @Override
  public void send(Object audience, String a1, String a2, Object... o) {
    a1 = a1 == null ? "" : a1;
    a2 = a2 == null ? "" : a2;
    Object[] objs = new Object[o.length + 2];
    objs[0] = a1;
    objs[1] = a2;
    System.arraycopy(o, 0, objs, 2, o.length);
    Audience aud = (Audience) audience;
    Component component = (Component) of(objs);
    if (aud instanceof ConsoleCommandSender) {
      String message = LegacyComponentSerializer.legacySection().serialize(component);
      console.log(message);
    }
    else {
      aud.sendMessage(component);
    }
  }

}

package io.wany.amethy.terminal.bukkit.modules;

public interface ServerMessage {

  Object of(Object... objects);

  String stringify(Object object);

  Object parse(String string);

  void send(Object audience, String a1, String a2, Object... o);

  default void send(Object audience, String a1, Object... o) {
    send(audience, a1, null, o);
  }

  default void send(Object audience, Object... o) {
    send(audience, null, null, o);
  }

  default void info(Object audience, String prefix, Object... objects) {
    send(audience, prefix, objects);
  }

  default void warn(Object audience, String prefix, Object... objects) {
    send(audience, prefix, "§e§l[경고]: ", objects);
  }

  default void error(Object audience, String prefix, Object... objects) {
    send(audience, prefix, "§e§l[오류]: ", objects);
  }

  @SuppressWarnings("unused")
  class ERROR {
    public static final String INSUFFICIENT_ARGS = "명령어 인자가 부족합니다.";
    public static final String NO_PERM = "명령어를 사용할 수 있는 권한이 없습니다.";
    public static final String UNKNOWN_ARG = "알 수 없는 명령어 인자입니다.";
    public static final String ONLY_CONSOLE = "서버 콘솔에서만 사용 가능한 명령어입니다.";
    public static final String ONLY_PLAYER = "플레이어만 사용 가능한 명령어입니다.";
  }

}

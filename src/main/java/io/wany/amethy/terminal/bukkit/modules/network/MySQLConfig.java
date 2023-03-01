package io.wany.amethy.terminal.bukkit.modules.network;

public class MySQLConfig {

  private final String host;
  private final int port;
  private final String username;
  private final String password;
  private final String database;

  public MySQLConfig(String host, int port, String username, String password, String database) {
    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
    this.database = database;
  }

  protected String url() {
    return "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database;
  }

  protected String user() {
    return this.username;
  }

  protected String password() {
    return this.password;
  }

}

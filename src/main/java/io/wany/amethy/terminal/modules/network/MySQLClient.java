package io.wany.amethy.terminal.modules.network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;

public class MySQLClient {

  private static HashMap<String, MySQLClient> databases = new HashMap<>();

  private final String id;
  private Connection conn;

  public MySQLClient(String id, MySQLConfig cfg) throws SQLException {
    this.id = id;
    this.conn = DriverManager.getConnection(cfg.url(), cfg.user(), cfg.password());
    databases.put(this.id, this);
  }

  private Statement getStatement() throws SQLException {
    return this.conn.createStatement();
  }

  private PreparedStatement getPreparedStatement(String q) throws SQLException {
    return this.conn.prepareStatement(q);
  }

  public MySQLResult query(String q) throws SQLException {
    Statement s = this.getStatement();
    MySQLResult r = null;
    if (q.toUpperCase().startsWith("SELECT")
        || q.toUpperCase().startsWith("SHOW")) {
      ResultSet rs = s.executeQuery(q);
      r = new MySQLResult();
      while (rs.next()) {
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
          r.set(meta.getColumnName(i), rs.getObject(i).toString());
        }
        r.nextIndex();
      }
      r.close();
      rs.close();
    } else {
      s.execute(q);
    }
    s.close();
    return r;
  }

  public MySQLResult query(String q, Object[] v) throws SQLException {
    PreparedStatement s = getPreparedStatement(q);
    for (int i = 0; i < v.length; i++) {
      Object o = v[i];
      if (o instanceof String) {
        s.setString(i + 1, (String) o);
      } else if (o instanceof String) {
        s.setString(i + 1, (String) o);
      } else if (o instanceof String) {
        s.setString(i + 1, (String) o);
      } else {
        s.setObject(i + 1, o);
      }
    }
    MySQLResult r = null;
    if (q.toUpperCase().startsWith("SELECT")
        || q.toUpperCase().startsWith("SHOW")) {
      ResultSet rs = s.executeQuery();
      r = new MySQLResult();
      while (rs.next()) {
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
          r.set(meta.getColumnName(i), rs.getObject(i).toString());
        }
        r.nextIndex();
      }
      r.close();
      rs.close();
    } else {
      s.execute();
    }
    s.close();
    return r;
  }

  public void close() throws SQLException {
    if (this.conn == null) {
      return;
    }
    this.conn.close();
  }

  public static Collection<MySQLClient> getDatabases() {
    return databases.values();
  }

}

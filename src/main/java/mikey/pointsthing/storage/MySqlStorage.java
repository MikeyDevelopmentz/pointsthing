package mikey.pointsthing.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

public class MySqlStorage implements Storage {
   private final String host;
   private final int port;
   private final String database;
   private final String username;
   private final String password;
   private final String table;
   private final boolean useSsl;

   private Connection connection;

   public MySqlStorage(String host, int port, String database, String username, String password,
                       String table, boolean useSsl) {
      this.host = host;
      this.port = port;
      this.database = database;
      this.username = username;
      this.password = password;
      this.table = sanitiseIdent(table);
      this.useSsl = useSsl;
   }

   private static String sanitiseIdent(String ident) {
      if (ident == null || !ident.matches("[A-Za-z0-9_]+")) {
         throw new IllegalArgumentException("Invalid table name: " + ident);
      }
      return ident;
   }

   private String jdbcUrl() {
      return "jdbc:mysql://" + host + ":" + port + "/" + database
            + "?useSSL=" + useSsl
            + "&autoReconnect=true"
            + "&useUnicode=true"
            + "&characterEncoding=UTF-8"
            + "&serverTimezone=UTC";
   }

   private Connection connection() throws SQLException {
      if (this.connection == null || this.connection.isClosed()) {
         this.connection = DriverManager.getConnection(this.jdbcUrl(), this.username, this.password);
      }
      return this.connection;
   }

   @Override
   public void init() throws SQLException {
      try {
         Class.forName("com.mysql.cj.jdbc.Driver");
      } catch (ClassNotFoundException e) {
         throw new SQLException("MySQL JDBC driver not found on classpath", e);
      }
      try (Connection c = this.connection();
           Statement st = c.createStatement()) {
         st.executeUpdate(
               "CREATE TABLE IF NOT EXISTS `" + this.table + "` ("
                     + "`uuid` VARCHAR(36) NOT NULL PRIMARY KEY, "
                     + "`points` INT NOT NULL DEFAULT 0"
                     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
      }
   }

   @Override
   public void loadAll(Map<UUID, Integer> out) throws SQLException {
      out.clear();
      try (Connection c = this.connection();
           Statement st = c.createStatement();
           ResultSet rs = st.executeQuery("SELECT `uuid`, `points` FROM `" + this.table + "`")) {
         while (rs.next()) {
            String uuidStr = rs.getString(1);
            int points = rs.getInt(2);
            try {
               out.put(UUID.fromString(uuidStr), points);
            } catch (IllegalArgumentException ignored) {
               // bad uuid in db, just skip it
            }
         }
      }
   }

   @Override
   public void saveAll(Map<UUID, Integer> data) throws SQLException {
      String sql = "INSERT INTO `" + this.table + "` (`uuid`, `points`) VALUES (?, ?) "
                 + "ON DUPLICATE KEY UPDATE `points` = VALUES(`points`)";
      Connection c = this.connection();
      boolean prevAuto = c.getAutoCommit();
      c.setAutoCommit(false);
      try (PreparedStatement ps = c.prepareStatement(sql)) {
         for (Map.Entry<UUID, Integer> e : data.entrySet()) {
            ps.setString(1, e.getKey().toString());
            ps.setInt(2, e.getValue());
            ps.addBatch();
         }
         ps.executeBatch();
         c.commit();
      } catch (SQLException ex) {
         c.rollback();
         throw ex;
      } finally {
         c.setAutoCommit(prevAuto);
      }
   }

   @Override
   public void clearAll() throws SQLException {
      try (Connection c = this.connection();
           Statement st = c.createStatement()) {
         st.executeUpdate("TRUNCATE TABLE `" + this.table + "`");
      }
   }

   @Override
   public void close() {
      if (this.connection != null) {
         try {
            this.connection.close();
         } catch (SQLException ignored) {
         }
      }
   }

   @Override
   public String name() {
      return "mysql (" + this.host + ":" + this.port + "/" + this.database + ")";
   }
}

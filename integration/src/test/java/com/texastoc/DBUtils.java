package com.texastoc;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@SuppressWarnings("ALL")
public class DBUtils {

  final static String DB_CONNECT = "jdbc:h2:file:~/testdb;AUTO_SERVER=TRUE";
  final static String DB_USER = "sa";
  final static String DB_PASSWORD = "";

  final static List<String> TABLES_TO_CLEAN_IN_ORDER = ImmutableList.of("season", "quarterlyseason", "seasonplayer", "quarterlyseasonplayer", "role", "player", "seats_per_table", "table_request", "seat", "game_table", "game_player", "game_payout", "game", "seating", "quarterlyseasonpayout", "seasonpayout", "seasonpayoutsettings", "toc_config", "settings", "version", "historicalseasonplayer", "historicalseason");

  static {
    try {
      Class.forName("org.h2.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  // Delete all the rows of all the tables
  public static void cleanDb() {
    try(Connection conn = DriverManager.getConnection(DB_CONNECT, DB_USER, DB_PASSWORD)) {
      Statement st = conn.createStatement();
      for (String tableName : TABLES_TO_CLEAN_IN_ORDER) {
        st.executeUpdate("delete from " + tableName);
      }
      st.close();

      seedTables(conn);

      conn.commit();
    } catch (SQLException | IOException throwables) {
      throwables.printStackTrace();
    }
  }

  // Reset the seed data
  static void seedTables(Connection conn) throws IOException, SQLException {
    InputStream resource = new ClassPathResource(
      "seed_toc.sql").getInputStream();
    try (BufferedReader reader = new BufferedReader(
      new InputStreamReader(resource))) {
      String line;
      StringBuilder sb = new StringBuilder();
      Statement st = conn.createStatement();
      while ((line = reader.readLine()) != null) {
        if (StringUtils.isBlank(line)) {
          continue;
        }
        if (line.startsWith("#")) {
          continue;
        }

        sb.append(" " + line);

        if (line.endsWith(";")) {
          st.executeUpdate(sb.toString());
          sb = new StringBuilder();
        }
      }
      st.close();
    }

  }
}
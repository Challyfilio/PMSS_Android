package com.example.challyfilio.pmss;

import java.sql.*;

public class DatabaseHelper {
    public static Connection openConnection() {
        Connection conn;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url1 = "jdbc:mysql://10.0.2.2:3307/pmss?useUnicode=true&characterEncoding=UTF8";//AVD
            String url2 = "jdbc:mysql://192.168.10.240:3307/pmss?useUnicode=true&characterEncoding=UTF8";//BoyNextDoor
            String url3 = "jdbc:mysql://192.168.199.120:3307/pmss?useUnicode=true&characterEncoding=UTF8";//Yoran_Personal
            String url4 = "jdbc:mysql://192.168.137.1:3307/pmss?useUnicode=true&characterEncoding=UTF8";//
            conn = DriverManager.getConnection(url4, "Challyfilio", "123456");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            conn = null;
        } catch (SQLException e) {
            e.printStackTrace();
            conn = null;
        }
        return conn;
    }

    public static ResultSet getResult(Connection conn, String sql) {
        Statement stat;
        ResultSet rs;
        try {
            stat = conn.createStatement();
            rs = stat.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            rs = null;
        }
        return rs;
    }

    public static Statement exeStat(Connection conn, String sql) {
        Statement stat;
        try {
            stat = conn.createStatement();
            stat.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            stat = null;
        }
        return stat;
    }
}
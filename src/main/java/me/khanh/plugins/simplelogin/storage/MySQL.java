package me.khanh.plugins.simplelogin.storage;

import me.khanh.plugins.simplelogin.SimpleLogin;
import me.khanh.plugins.simplelogin.utils.Utils;

import java.sql.*;

public class MySQL implements Utils {

    private final SimpleLogin plugin = SimpleLogin.getInstance();

    private final String host = getConfigString("mysql.host");
    private final String port = getConfigString("mysql.port");
    private final String database = getConfigString("mysql.database");
    private final String username = getConfigString("mysql.username");
    private final String password = getConfigString("mysql.password");

    private Connection connection;

    public boolean isConnected(){
        return connection != null;
    }

    public Connection getConnection(){
        return connection;
    }

    public void connect(){
        if (!isConnected()){
            try {
                connection = DriverManager.getConnection("jdbc:mysql://" +
                                host + ':' + port + '/' + database + "?useSSL=false",
                        username, password);
            } catch (SQLException e) {
                error("Cannot connecting to Database.");
                plugin.disable();
                e.printStackTrace();
            }
        }
    }

    public void disconnect(){
        if (isConnected()){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void createTables(){
        PreparedStatement ps;
        try {
            ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS SIMPLE_LOGIN_DATABASE "
            + "(PLAYER_NAME VARCHAR(100), PASSWORD VARCHAR(100), PRIMARY KEY (PLAYER_NAME))");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean exists(String name){
        PreparedStatement ps;
        try {
            ps = getConnection().prepareStatement("SELECT * FROM SIMPLE_LOGIN_DATABASE WHERE PLAYER_NAME=?");
            ps.setString(1, name);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setPassword(String name, String password){
        try {
            PreparedStatement ps = getConnection().prepareStatement("INSERT INTO SIMPLE_LOGIN_DATABASE" +
                    "(PLAYER_NAME,PASSWORD) VALUES(?,?)");
            ps.setString(1, name);
            ps.setString(2, password);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPassword(String name){
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM SIMPLE_LOGIN_DATABASE " +
                    "WHERE PLAYER_NAME=?");
            ps.setString(1, name);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()){
                return resultSet.getString("PASSWORD");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

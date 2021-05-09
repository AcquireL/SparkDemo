package com.startarget.sparkdemo.sparkCore.connector.mysql;

import java.sql.*;

/**
 *  测试使用5.1.44的jar包连接Mysql8.0的服务
 */


public class ConnectMysql {
    private Connection con;

    public ConnectMysql(){
        // 注册驱动
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.con= DriverManager.getConnection("jdbc:mysql://10.203.127.110:3307/vdf?useUnicode=yes&characterEncoding=UTF-8&useSSL=false&serverTimezone=UTC","owvdf","q3XB0T2p");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getData(){
        try {
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from hdfsToHiveTest");
            while (resultSet.next()){
                System.out.println(resultSet.getString(1));
                System.out.println(resultSet.getString(2));
                System.out.println(resultSet.getString(3));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ConnectMysql connectMysql = new ConnectMysql();
        connectMysql.getData();
    }


}

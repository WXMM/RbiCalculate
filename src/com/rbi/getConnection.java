package com.rbi;

import java.sql.*; 
//import java.sql.SQLException; //数据库链接实例
//import java.sql.DriverManager;       //数据库驱动管理类，调用其静态方法getConnection并传入数据库的URl获得数据库连接实例
//import java.sql.PreparedStatement;           //操作数据库要用的类，主要用于执行SQl语句
//import java.sql.ResultSet;           //数据库查询结果集  
//import java.sql.*;


//import com.mysql.jdbc.*;

public class getConnection {
    static DBHelper dBHelper = null;  
    static ResultSet ret     = null;  
	
	public static final String url = "jdbc:mysql://localhost:3306/rbi_bs";  
    public static final String name = "com.mysql.jdbc.Driver";  
    public static final String user = "root";  
    public static final String password = "";      
    static DBHelper   db1 = null;  
    public   static PreparedStatement  pst  = null;
    public   static Connection         conn = null; 
    public   static ResultSet           ret = null; 
  
    public  static void main(String[] args) {  
    	 String sql = "select *from dic_area";//SQL语句  
    	 getConnection GetConnection = new getConnection();//实例化对象
    	 String name = GetConnection.getData("你好"); 
         System.out.println(GetConnection.visitDB(sql));
         try {  
        	 Test.startConn(sql);//执行语句，得到结果集  
             ret = pst.executeQuery();
             while (ret.next()) {  
                 String uid = ret.getString(1);  
                 String ufname = ret.getString(2);  
                 String ulname = ret.getString(3);  
                 String udate = ret.getString(4);  
                 System.out.println(uid + "\t" + ufname + "\t" + ulname + "\t" + udate );  
             }//显示数据    
             Test.closeConn();//关闭连接  
         } catch (SQLException e) {  
             e.printStackTrace();  
         }  
    }
    
    public String visitDB(String sql){
    	
        Connection conn = null;
        // MySQL的JDBC URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称?参数=值
        // 避免中文乱码要指定useUnicode和characterEncoding
        // 执行数据库操作之前要在数据库管理系统上创建一个数据库，名字自己定，
        // 下面语句之前就要先创建javademo数据库
//        String sql="";
        String url = "jdbc:mysql://localhost:3306/rbi_bs?"
                + "user=root&password=&useUnicode=true&characterEncoding=UTF8";
        String re = null;
        try {
            // 之所以要使用下面这条语句，是因为要使用MySQL的驱动，所以我们要把它驱动起来，
            // 可以通过Class.forName把它加载进去，也可以通过初始化来驱动起来，下面三种形式都可以
            Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
            // or:
            // com.mysql.jdbc.Driver driver = new com.mysql.jdbc.Driver();
            // or：
            // new com.mysql.jdbc.Driver();
 
//            System.out.println("成功加载MySQL驱动程序");
            // 一个Connection代表一个数据库连接
            conn = DriverManager.getConnection(url);
            // Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);// executeQuery会返回结果的集合，否则返回空值
//            System.out.println("学号\t姓名");
            while (rs.next()) {
//                System.out
//                        .println(rs.getString(1) + "\t" + rs.getString(2));// 入如果返回的是int类型可以用getInt()
                re =  rs.getString(3);
            }
            
            conn.close();
////            return re;
//            System.out.println("这是name"+name);
        } catch (SQLException e){
        	re = "SQL报错";
        } catch (ClassNotFoundException e) {
        	re = "ClassNotFound报错";
        } catch (Exception e){
        	re = "其他报错";
        }
        return re;
    }
    
    
    public String getData(String d){
    	String udate = null;
    	String s = null;
        try { 
            String sql = "select * from dic_area";//SQL语句  
       	    getConnection Test = new getConnection();//实例化对象
       	    Test.startConn(sql);//执行语句，得到结果集  
            ret = pst.executeQuery();
            while (ret.next()) {   
                 udate  = ret.getString(4);  
//                System.out.println(uid + "\t" + ufname + "\t" + ulname + "\t" + udate );  
            }//显示数据 
            Test.closeConn();//关闭连接  
            
            Calculate calculate = new Calculate();
            float fail = calculate.getFailurePro(1,1,1);
             s   = udate + fail + d;
//            return sql;
        } catch (SQLException e) {  
            e.printStackTrace(); 
            s = "wrong" + d;
//            return sql;
        } 
        return s;	
    }
//    
    public void startConn(String sql) {
        try
        {
            Class.forName(name);     //加载JDBC驱动
            System.out.println("Driver Load Success.");

            conn = DriverManager.getConnection(url,user,password);    //创建数据库连接对象//            sta = conn.createStatement();       //创建Statement对象
            pst  = conn.prepareStatement(sql);//准备执行语句 
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void closeConn() {  
        try {  
        	ret.close(); 
            conn.close();  
            pst.close();  
        } catch (SQLException e) {  
            e.printStackTrace();  
        }  
    }  
//	
}
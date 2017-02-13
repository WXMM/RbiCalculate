package com.rbi;

import java.sql.Connection;  
import java.sql.DriverManager;  
import java.sql.PreparedStatement;  
import java.sql.SQLException;  
import java.sql.Statement;  
  
  
/* 
 * �ô���HandleMySQL�����뱾�����ݿ����ӣ����ֻ��Ҫ�޸����ݿ������û��������뼴�� 
 * */  
public class MySQLUtils {  
      
    private String driver = "com.mysql.jdbc.Driver";  
    private String dbName = "test";//���ݿ�����  
    private String ip = "127.0.0.1";  
    private String url = "jdbc:mysql://127.0.0.1:3306/";//���ݿ��ַ�Լ�����  
    private String user = "root";//���ݿ��û���  
    private String password = "";//���ݿ��û�����  
    private Connection conn;  
      
      
    public Statement linkMySQL(){  
          
        try {  
            Class.forName(driver);//������������  
            conn = DriverManager.getConnection(url+dbName, user, password);//�������ݿ�  
            if(!conn.isClosed())  
                System.out.println("Succeeded connecting to the Database!");  
            Statement statement = conn.createStatement();// statement����ִ��SQL���  
            return statement;  
        } catch (ClassNotFoundException e) {  
            // TODO Auto-generated catch block  
            System.err.println("HandleMySQL Class.forName����");  
            System.err.println(e.getMessage());  
            e.printStackTrace();  
            return null;  
        }catch (SQLException e) {  
            // TODO Auto-generated catch block  
            System.err.println("HandleMySQL Connection����");  
            System.err.println(e.getMessage());  
            //e.printStackTrace();  
            return null;  
        }  
          
    }  
      
    public PreparedStatement linkMySQL(String sql){  
          
        try {  
            Class.forName(driver);//������������  
            Connection conn = DriverManager.getConnection(url+dbName, user, password);//�������ݿ�  
            if(!conn.isClosed())  
                System.out.println("Succeeded connecting to the Database!");  
            PreparedStatement preStatement = conn.prepareStatement(sql);// PreparedStatement����ִ��SQL���  
            return preStatement;  
        } catch (ClassNotFoundException e) {  
            // TODO Auto-generated catch block  
            System.err.println("HandleMySQL Pre Class.forName����");  
            System.err.println(e.getMessage());  
            //e.printStackTrace();  
            return null;  
        }catch (SQLException e) {  
            // TODO Auto-generated catch block  
            System.err.println("HandleMySQL Pre Connection����");  
            System.err.println(e.getMessage());  
            //e.printStackTrace();  
            return null;  
        }  
          
    }  
      
    public void closeMySQL(Statement statement){  
        try {  
            if(statement == null)  
                return;  
            statement.close();  
            System.out.println("close the link to datebase");  
            //conn.close();  
        } catch (SQLException e) {  
            // TODO Auto-generated catch block  
            //e.printStackTrace();  
            System.err.println("HandleMySQL Pre closeMySQL����");  
            System.err.println(e.getMessage());  
        }  
    }  
      
    public void closeMySQL(PreparedStatement preStatement){  
        try {  
            if(preStatement == null)  
                return;  
            preStatement.close();  
            System.out.println("close the link to datebase");  
            //conn.close();  
        } catch (SQLException e) {  
            // TODO Auto-generated catch block  
            //e.printStackTrace();  
            System.err.println("HandleMySQL Pre closeMySQL����");  
            System.err.println(e.getMessage());  
        }  
    }  
      
      
}  

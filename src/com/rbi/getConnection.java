package com.rbi;

import java.sql.*; 
//import java.sql.SQLException; //���ݿ�����ʵ��
//import java.sql.DriverManager;       //���ݿ����������࣬�����侲̬����getConnection���������ݿ��URl������ݿ�����ʵ��
//import java.sql.PreparedStatement;           //�������ݿ�Ҫ�õ��࣬��Ҫ����ִ��SQl���
//import java.sql.ResultSet;           //���ݿ��ѯ�����  
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
    	 String sql = "select *from dic_area";//SQL���  
    	 getConnection GetConnection = new getConnection();//ʵ��������
    	 String name = GetConnection.getData("���"); 
         System.out.println(GetConnection.visitDB(sql));
         try {  
        	 Test.startConn(sql);//ִ����䣬�õ������  
             ret = pst.executeQuery();
             while (ret.next()) {  
                 String uid = ret.getString(1);  
                 String ufname = ret.getString(2);  
                 String ulname = ret.getString(3);  
                 String udate = ret.getString(4);  
                 System.out.println(uid + "\t" + ufname + "\t" + ulname + "\t" + udate );  
             }//��ʾ����    
             Test.closeConn();//�ر�����  
         } catch (SQLException e) {  
             e.printStackTrace();  
         }  
    }
    
    public String visitDB(String sql){
    	
        Connection conn = null;
        // MySQL��JDBC URL��д��ʽ��jdbc:mysql://�������ƣ����Ӷ˿�/���ݿ������?����=ֵ
        // ������������Ҫָ��useUnicode��characterEncoding
        // ִ�����ݿ����֮ǰҪ�����ݿ����ϵͳ�ϴ���һ�����ݿ⣬�����Լ�����
        // �������֮ǰ��Ҫ�ȴ���javademo���ݿ�
//        String sql="";
        String url = "jdbc:mysql://localhost:3306/rbi_bs?"
                + "user=root&password=&useUnicode=true&characterEncoding=UTF8";
        String re = null;
        try {
            // ֮����Ҫʹ������������䣬����ΪҪʹ��MySQL����������������Ҫ��������������
            // ����ͨ��Class.forName�������ؽ�ȥ��Ҳ����ͨ����ʼ������������������������ʽ������
            Class.forName("com.mysql.jdbc.Driver");// ��̬����mysql����
            // or:
            // com.mysql.jdbc.Driver driver = new com.mysql.jdbc.Driver();
            // or��
            // new com.mysql.jdbc.Driver();
 
//            System.out.println("�ɹ�����MySQL��������");
            // һ��Connection����һ�����ݿ�����
            conn = DriverManager.getConnection(url);
            // Statement������кܶ෽��������executeUpdate����ʵ�ֲ��룬���º�ɾ����
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);// executeQuery�᷵�ؽ���ļ��ϣ����򷵻ؿ�ֵ
//            System.out.println("ѧ��\t����");
            while (rs.next()) {
//                System.out
//                        .println(rs.getString(1) + "\t" + rs.getString(2));// ��������ص���int���Ϳ�����getInt()
                re =  rs.getString(3);
            }
            
            conn.close();
////            return re;
//            System.out.println("����name"+name);
        } catch (SQLException e){
        	re = "SQL����";
        } catch (ClassNotFoundException e) {
        	re = "ClassNotFound����";
        } catch (Exception e){
        	re = "��������";
        }
        return re;
    }
    
    
    public String getData(String d){
    	String udate = null;
    	String s = null;
        try { 
            String sql = "select * from dic_area";//SQL���  
       	    getConnection Test = new getConnection();//ʵ��������
       	    Test.startConn(sql);//ִ����䣬�õ������  
            ret = pst.executeQuery();
            while (ret.next()) {   
                 udate  = ret.getString(4);  
//                System.out.println(uid + "\t" + ufname + "\t" + ulname + "\t" + udate );  
            }//��ʾ���� 
            Test.closeConn();//�ر�����  
            
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
            Class.forName(name);     //����JDBC����
            System.out.println("Driver Load Success.");

            conn = DriverManager.getConnection(url,user,password);    //�������ݿ����Ӷ���//            sta = conn.createStatement();       //����Statement����
            pst  = conn.prepareStatement(sql);//׼��ִ����� 
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
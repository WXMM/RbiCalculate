package com.rbi;
import java.io.Serializable;  
import java.sql.Connection;  
import java.sql.DriverManager;  
import java.sql.PreparedStatement;  
import java.sql.ResultSet;  
import java.sql.SQLException;  
import java.sql.Statement;  
  
public class TableInfoService implements Serializable{  
  
	
	 public static void main(String[] args) {
		 TableInfoService test = new TableInfoService();
		 
	         System.out.println(test.getSingleColoumName());
	        // System.out.println(update());
	        // System.out.println(delete());
	    }
	 
    private static final long serialVersionUID = 677484458789332877L;  
    private RequestInfoFromMySQL requestInfo = new RequestInfoFromMySQL();  
    private String[] nameList;   
      
    public String getSingleColoumName(){  
          
        return requestInfo.getSingleColoumName();  
    }  
      
    public String getName(String name){  
        return requestInfo.getName(name);  
    }  
      
}  

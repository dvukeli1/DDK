/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package davateljikrvi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Mitja
 */
public class DBController {
    public DBController(){
        
    }
     // JDBC driver name and database URL
   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
   static final String DB_URL = "jdbc:mysql://localhost/ddk";

   //  Database credentials
   static final String USER = "elmas";
   static final String PASS = "9elmas4";
   
   public void DBInsert (int counter) {
   Connection conn = null;
   Statement stmt = null;
   try{
      //STEP 2: Register JDBC driver
      Class.forName("com.mysql.jdbc.Driver");

      //STEP 3: Open a connection
      System.out.println("Connecting to a selected database...");
      conn = DriverManager.getConnection(DB_URL, USER, PASS);
      System.out.println("Connected database successfully...");
      
      //STEP 4: Execute a query
      System.out.println("Creating statement...");
      stmt = conn.createStatement();
      String sql = "INSERT INTO cards(br_karte)VALUES("+counter+")";
      stmt.executeUpdate(sql);

      
   }catch(SQLException | ClassNotFoundException se){
       se.printStackTrace();
   }finally{
    
      try{
         if(stmt!=null)
            conn.close();
      }catch(SQLException se){
          se.printStackTrace();
      }
      try{
         if(conn!=null)
            conn.close();
      }catch(SQLException se){
          se.printStackTrace();
      }
   }
   System.out.println("Goodbye!");
}
     
}

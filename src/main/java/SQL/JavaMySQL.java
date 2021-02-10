package SQL;

import java.sql.*;
import java.util.ArrayList;
import Resources.Persona;
import Resources.Persone;

public class JavaMySQL {  
    Persone persone = new Persone();
    
    public ArrayList<Persona> getResult() {
        try {
            //create my mysql database connection
            String myDriver = "com.mysql.cj.jdbc.Driver";
            Class.forName(myDriver);      
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/HTTPServer?user=root&password=12345&serverTimezone=Europe/Rome"); //connection to server 
            Statement statement = conn.createStatement();
      
            String query = "SELECT Persone.* FROM Persone";
      
            ResultSet rs = statement.executeQuery(query); 
      
            while(rs.next()) {
                String nome = rs.getString("nome");
                String cognome = rs.getString("cognome");
                Persona p = new Persona(nome, cognome);
                persone.addPersona(p);
            }
        }catch(ClassNotFoundException | SQLException ex) {
            ex.toString();
        } 
        return persone.getArrayPersone();
    }
}
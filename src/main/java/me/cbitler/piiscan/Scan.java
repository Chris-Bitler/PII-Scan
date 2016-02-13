package me.cbitler.piiscan;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Scan {
    static Connection conn;
    static HashMap<String,Pattern> ppi = new HashMap<String, Pattern>();
    public static void main(String[] args) throws SQLException, IOException {
        if(args.length == 4) {
            //Grab the mysql info from the command line
            String u = args[0];
            String p = args[1];
            String h = args[2];
            String d = args[3];

            System.out.println("Compiling expressions");
            BufferedReader bf = new BufferedReader(new FileReader(new File("data_regex")));
            String line;

            //This reads the name of the expression and each expression from the data_regex file in the same directory as the program.
            while((line = bf.readLine()) != null) {
                String[] split = line.split(" ",2);
                Pattern pat = Pattern.compile(split[1],Pattern.CASE_INSENSITIVE);
                ppi.put(split[0],pat);
            }

            ///Connect to mysql
            conn = DriverManager.getConnection("jdbc:mysql://"+h+"/"+d+"?user="+u+"&password="+p);


            //Get the list of tables
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            System.out.println("Scanning for tables..");
            while(rs.next()) {
                System.out.println("Scanning " + rs.getString(1) + "...");
                Statement stmt2 = conn.createStatement();
                ResultSet vals = stmt2.executeQuery("SELECT * FROM `" + rs.getString(1) + "`");
                //For each table get all of the rows

                while(vals.next()) {
                    int colCount = vals.getMetaData().getColumnCount();
                    //For each cell in each row, compare it to the regexes provided.
                    for(int i = 1; i <= colCount; i++) {
                        String value = vals.getString(i);
                        for(Map.Entry<String,Pattern> pattern : ppi.entrySet()) {
                            if(pattern.getValue().matcher(value).matches()) {
                                //If a match is found, spit it out to commandline.
                                System.out.println("Potential match found: " + pattern.getKey() + ": " + value);
                            }
                        }
                    }
                }
                vals.close();
                stmt2.close();
            }
            rs.close();
            stmt.close();
        }else{
            //Tell them to provide the relevant information.
            System.err.println("You need to provide a username, password, hostname, and db name");
        }
    }
}

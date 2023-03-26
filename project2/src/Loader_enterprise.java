import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.sql.*;
import java.net.URL;
public class Loader_enterprise {
    private static final int  BATCH_SIZE = 500;
    private static final URL        propertyURL = Loader_enterprise.class
            .getResource("/loader.cnf");

    private static Connection         con = null;
    private static PreparedStatement[] stmt = new PreparedStatement[4];
    private static boolean            verbose = false;

    private static void closeDB() {
        if (con != null) {
            try {
                for(int i = 1; i <= 3; i++){
                    if (stmt[i] != null) {
                        stmt[i].close();
                    }
                }
                con.close();
                con = null;
            } catch (Exception e) {
                // Forget about it
            }
        }
    }

    private static void openDB(String host, String dbname,
                               String user, String pwd) {
        try {
            //
            Class.forName("org.postgresql.Driver");
        } catch(Exception e) {
            System.err.println("Cannot find the Postgres driver. Check CLASSPATH.");
            System.exit(1);
        }
        String url = "jdbc:postgresql://" + host + "/" + dbname;
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pwd);
        try {
            con = DriverManager.getConnection(url, props);
            if (verbose) {
                System.out.println("Successfully connected to the database "
                        + dbname + " as " + user);
            }
            con.setAutoCommit(false);
        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
        try {
            stmt[1] = con.prepareStatement("insert into industry(name)"
                    + " values(?)" + " on conflict (name) do nothing");
            stmt[2] = con.prepareStatement("insert into district(city_name,country_name,main_area)"
                    + " values(?,?,?)" + " on conflict (city_name,country_name) do nothing");
            stmt[3] = con.prepareStatement("insert into client_enterprise(id,client_enterprise_name,city,country,industry)"
                    + " values(?,?,?,?,?)" + " on conflict (client_enterprise_name) do nothing");
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }
    }

    private static void loadData(
            int id,
            String name,
            String country,
            String city,
            String supply_center,
            String industry)
            throws SQLException {
        if (con != null) {
            stmt[1].setString(1,industry);
            stmt[2].setString(1,city);
            stmt[2].setString(2,country);
            stmt[2].setString(3,supply_center);
            stmt[3].setInt(1,id);
            stmt[3].setString(2,name);
            stmt[3].setString(3,city);
            stmt[3].setString(4,country);
            stmt[3].setString(5,industry);
            for(int i = 1; i <= 3; i++)stmt[i].addBatch();
        }
    }

    public static void loader_enterprise() {
        for(int i = 1; i <= 3; i++)stmt[i] = null;
        String  fileName = "enterprise.csv";
        boolean verbose = false;
        Properties defprop = new Properties();
        defprop.put("host", "localhost");
        defprop.put("user", "checker");
        defprop.put("password", "123456");
        defprop.put("database", "cs3073");
        Properties prop = new Properties(defprop);
        /*try (BufferedReader conf
                     = new BufferedReader(new FileReader(propertyURL.getPath()))) {
            prop.load(conf);
        } catch (IOException e) {
            // Ignore
            System.err.println("No configuration file (loader.cnf) found");
        }*/
        try (BufferedReader infile
                     = new BufferedReader(new FileReader(fileName))) {
            long     start;
            long     end;
            String   line;
            String[] parts;
            int id;
            String name;
            String country;
            String city;
            String supply_center;
            String industry;
            SimpleDateFormat str_to_date = new SimpleDateFormat("yyyy-MM-dd");
            int      cnt = 0;
            // Empty target table
            openDB(prop.getProperty("host"), prop.getProperty("database"),
                    prop.getProperty("user"), prop.getProperty("password"));
            Statement stmt0;
            if (con != null) {
                stmt0 = con.createStatement();
                stmt0.execute("truncate table staff cascade");
                stmt0.close();
            }
            closeDB();
            //
            start = System.currentTimeMillis();
            openDB(prop.getProperty("host"), prop.getProperty("database"),
                    prop.getProperty("user"), prop.getProperty("password"));
            java.sql.Date sqlDate0 = null;
            java.sql.Date sqlDate1 = null;
            java.sql.Date sqlDate2 = null;
            boolean key;
            line = infile.readLine();
            while ((line = infile.readLine()) != null)
            {
                //line = infile.readLine();
                parts = line.split(",");
                if (parts.length > 1) {
                    id = Integer.parseInt(parts[0]);
                    name= parts[1];
                    country = parts[2];
                    city=parts[3];
                    supply_center=parts[4];
                    if(parts.length==6)
                    {
                        industry=parts[5];
                    }
                    else
                    {
                        supply_center+=parts[5];
                        industry=parts[6];
                    }
                    loadData(id, name,country,city,supply_center,industry);
                    cnt++;
                    if (cnt % BATCH_SIZE == 0) {
                        for(int i = 1; i <= 3; i++){
                            stmt[i].executeBatch();
                            stmt[i].clearBatch();
                        }
                    }
                }
            }
            if (cnt % BATCH_SIZE != 0) {
                for(int i = 1; i <= 3; i++){
                    stmt[i].executeBatch();
                }
            }
            con.commit();
            for(int i = 1; i <= 3; i++){
                stmt[i].close();
            }
            closeDB();
            end = System.currentTimeMillis();
            System.out.println(cnt + " records successfully loaded");
            System.out.println("Loading speed : "
                    + (cnt * 1000)/(end - start)
                    + " records/s");
        } catch (SQLException se) {
            System.err.println("SQL error: " + se.getMessage());
            try {
                con.rollback();
                for(int i = 1; i <= 3; i++){
                    stmt[i].close();
                }
            } catch (Exception e2) {
            }
            closeDB();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Fatal error: " + e.getMessage());
            try {
                con.rollback();
                for(int i = 1; i <= 3; i++){
                    stmt[i].close();
                }
            } catch (Exception e2) {
            }
            closeDB();
            System.exit(1);
        }
        closeDB();
    }
}

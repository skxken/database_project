import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.sql.*;
import java.net.URL;

public class Loader_model {
    private static final int  BATCH_SIZE = 500;
    private static final URL        propertyURL = Loader_model.class
            .getResource("/loader.cnf");

    private static Connection         con = null;
    private static PreparedStatement[] stmt = new PreparedStatement[3];
    private static boolean            verbose = false;

    private static void closeDB() {
        if (con != null) {
            try {
                for(int i = 1; i <= 2; i++){
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
            stmt[1] = con.prepareStatement("insert into product(number,name)"
                    + " values(?,?)" + " on conflict (number) do nothing");
            stmt[2] = con.prepareStatement("insert into model_price(id,model,unit_price,number)"
                    + " values(?,?,?,?)" + " on conflict (model) do nothing");
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }
    }

    private static void loadData(
            int id,
            String number,
            String model,
            String name,
            int price)
            throws SQLException {
        if (con != null) {
            stmt[1].setString(1,number);
            stmt[1].setString(2,name);
            stmt[2].setInt(1,id);
            stmt[2].setString(2,model);
            stmt[2].setInt(3,price);
            stmt[2].setString(4,number);
            for(int i = 1; i <= 2; i++)stmt[i].addBatch();
        }
    }

    public static void loader_model() {
        for(int i = 1; i <= 2; i++)stmt[i] = null;
        String  fileName = "model.csv";
        boolean verbose = false;

        Properties defprop = new Properties();
        defprop.put("host", "localhost");
        defprop.put("user", "checker");
        defprop.put("password", "123456");
        defprop.put("database", "cs3073");
        Properties prop = new Properties(defprop);
        try (BufferedReader infile
                     = new BufferedReader(new FileReader(fileName))) {
            long     start;
            long     end;
            String   line;
            String[] parts;
            int num=0;
            String number;
            String model;
            String name;
            int price=0;
            SimpleDateFormat str_to_date = new SimpleDateFormat("yyyy-MM-dd");
            int      cnt = 0;
            // Empty target table
            openDB(prop.getProperty("host"), prop.getProperty("database"),
                    prop.getProperty("user"), prop.getProperty("password"));
            Statement stmt0;
            if (con != null) {
                stmt0 = con.createStatement();
                stmt0.execute("truncate table model_price cascade");
                stmt0.execute("truncate table product cascade");
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
                    num = Integer.parseInt(parts[0]);
                    number = parts[1];
                    model = parts[2];
                    name = parts[3];
                    price = Integer.parseInt(parts[4]);
                    loadData(num,number,model,name,price);
                    cnt++;
                    if (cnt % BATCH_SIZE == 0) {
                        for(int i = 1; i <= 2; i++){
                            stmt[i].executeBatch();
                            stmt[i].clearBatch();
                        }
                    }
                }
            }
            if (cnt % BATCH_SIZE != 0) {
                for(int i = 1; i <= 2; i++){
                    stmt[i].executeBatch();
                }
            }
            con.commit();
            for(int i = 1; i <= 2; i++){
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
                for(int i = 1; i <= 2; i++){
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
                for(int i = 1; i <= 2; i++){
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


import java.text.SimpleDateFormat;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.sql.*;
import java.net.URL;

public class DatabaseManipulation implements DataManipulation {
    private Connection con = null;
    private ResultSet resultSet;

    private String host = "localhost";
    private String dbname = "cs3073";
    private String user = "checker";
    private String pwd = "123456";
    private String port = "5432";


    @Override
    public void openDatasource() {
        try {
            Class.forName("org.postgresql.Driver");

        } catch (Exception e) {
            System.err.println("Cannot find the PostgreSQL driver. Check CLASSPATH.");
            System.exit(1);
        }

        try {
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
            con = DriverManager.getConnection(url, user, pwd);

        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void closeDatasource() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public int stockIn(String supply_center,String product_model,String supply_staff,String date,int purchase_price,int quantity)
    {
        SimpleDateFormat str_to_date = new SimpleDateFormat("yyyy-MM-dd");
        int result = 0;
        String sql = "insert into supply_center_stock (supply_center, product_model, supply_staff, date, purchase_price, quantity, type) \n" +
                "select ?,?,?,?,?,?,? \n"+
                "where not exists (select * from staff \n" +
                "where (number=? and supply_center!=?) or(number=? and type!='Supply Staff')) \n"+
                " and exists(select * from staff \n" +
                "        where number=?) \n" +
                "and exists(select * from supply_center \n" +
                "        where name=?) \n" +
                "and exists(select * from model_price \n" +
                "        where model=?);";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            //System.out.println(preparedStatement.toString());
            preparedStatement.setString(1, supply_center);
            //System.out.println(preparedStatement.toString());
            preparedStatement.setString(2, product_model);
            preparedStatement.setString(3, supply_staff);
            preparedStatement.setDate(4, new java.sql.Date(str_to_date.parse(date).getTime()));
            preparedStatement.setInt(5,purchase_price);
            preparedStatement.setInt(6,quantity);
            preparedStatement.setString(7,"buy");
            preparedStatement.setString(8, supply_staff);
            preparedStatement.setString(9, supply_center);
            preparedStatement.setString(10, supply_staff);
            preparedStatement.setString(11, supply_staff);
            preparedStatement.setString(12, supply_center);
            preparedStatement.setString(13, product_model);
            //System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public int placeOrder(String contract_num,String enterprise,String product_model,int quantity,String contract_manager,String contract_date,String estimated_delivery_date,String lodgement_date,String salesman_num,String contract_type)
    {
        SimpleDateFormat str_to_date = new SimpleDateFormat("yyyy-MM-dd");
        int result = 0;
        String sql = "insert into supply_center_stock (supply_center, product_model, supply_staff, date, purchase_price, quantity, type)\n" +
                " select (select sc.name \n"+
                "    from client_enterprise \n" +
                "    join district d on client_enterprise.city = d.city_name and client_enterprise.country = d.country_name \n" +
                "    join supply_center sc on d.main_area = sc.name \n" +
                "    where client_enterprise_name= ? ),?,?,?,?,?,? \n" +
                "where (select sum(s.quantity) from supply_center_stock s \n" +
                "    where product_model=? and supply_center=(select sc.name\n" +
                "    from client_enterprise\n" +
                "    join district d on client_enterprise.city = d.city_name and client_enterprise.country = d.country_name\n" +
                "    join supply_center sc on d.main_area = sc.name\n" +
                "    where client_enterprise_name= ?))> ? \n" +
                "    and not exists(select * from staff \n" +
                "        where(number=? and type!='Salesman'));" ;


        //System.out.println(sql);
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, enterprise);
            //System.out.println(preparedStatement.toString());
            preparedStatement.setString(2, product_model);
            preparedStatement.setString(3, salesman_num);
            preparedStatement.setDate(4, new java.sql.Date(str_to_date.parse(contract_date).getTime()));
            preparedStatement.setInt(5,0);
            preparedStatement.setInt(6,-quantity);
            preparedStatement.setString(7,"sell");

            preparedStatement.setString(8, product_model);
            preparedStatement.setString(9, enterprise);
            preparedStatement.setInt(10, quantity);
            preparedStatement.setString(11, salesman_num);
            //System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }catch (ParseException e) {
            e.printStackTrace();
        }
        sql = "insert into contract (contract_number, client_enterprise_name, contract_date, contract_type,contract_manager) \n" +
                "select ?,?,?,?,? \n" +
                "where (select sum(s.quantity) from supply_center_stock s \n" +
                "    where product_model=? and supply_center=(select sc.name\n" +
                "    from client_enterprise\n" +
                "    join district d on client_enterprise.city = d.city_name and client_enterprise.country = d.country_name\n" +
                "    join supply_center sc on d.main_area = sc.name\n" +
                "    where client_enterprise_name= ?))>? \n" +
                "    and not exists(select * from staff \n" +
                "        where(number=? and type!='Salesman')) \n" +
                "   and not exists(select * from contract where contract_number=?)\n";

        //System.out.println(sql);
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);

            preparedStatement.setString(1, contract_num);
            preparedStatement.setString(2, enterprise);
            preparedStatement.setDate(3, new java.sql.Date(str_to_date.parse(contract_date).getTime()));
            //System.out.println(preparedStatement.toString());
            preparedStatement.setString(4, contract_type);
            preparedStatement.setString(5, contract_manager);

            preparedStatement.setString(6, product_model);
            preparedStatement.setString(7, enterprise);
            preparedStatement.setInt(8, quantity);
            preparedStatement.setString(9, salesman_num);
            preparedStatement.setString(10, contract_num);
            //System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }catch (ParseException e) {
            e.printStackTrace();
        }
        sql = "insert into detail (product_model, quantity, salesman_number, contract_number, estimated_delivery_date, lodgement_date)\n" +
                "select ?,?,?,?,?,? \n" +
                "where (select sum(s.quantity) from supply_center_stock s \n" +
                "    where product_model=? and supply_center=(select sc.name\n" +
                "    from client_enterprise\n" +
                "    join district d on client_enterprise.city = d.city_name and client_enterprise.country = d.country_name\n" +
                "    join supply_center sc on d.main_area = sc.name\n" +
                "    where client_enterprise_name= ?))>? \n" +
                "    and not exists(select * from staff \n" +
                "        where(number=? and type!='Salesman'))\n";
        //System.out.println(sql);
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);

            preparedStatement.setString(1, product_model);
            preparedStatement.setInt(2, quantity);
            preparedStatement.setString(3, salesman_num);
            preparedStatement.setString(4, contract_num);
            preparedStatement.setDate(5, new java.sql.Date(str_to_date.parse(estimated_delivery_date).getTime()));
            preparedStatement.setDate(6, new java.sql.Date(str_to_date.parse(lodgement_date).getTime()));

            preparedStatement.setString(7, product_model);
            preparedStatement.setString(8, enterprise);
            preparedStatement.setInt(9, quantity);
            preparedStatement.setString(10, salesman_num);
            //System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public int updateOrder(String contract,String product_model,String salesman,int quantity,String estimate_delivery_date,String lodgement_date)
    {
        SimpleDateFormat str_to_date = new SimpleDateFormat("yyyy-MM-dd");
        int result = 0;
        String sql = "with det as\n" +
                "(\n" +
                "    select max(sc.name) as name,max(de.quantity) as quantity\n" +
                "    from detail de join contract c on c.contract_number = de.contract_number\n" +
                "        join client_enterprise ce on ce.client_enterprise_name = c.client_enterprise_name\n" +
                "        join district d on ce.city = d.city_name and ce.country = d.country_name\n" +
                "        join supply_center sc on d.main_area = sc.name\n" +
                "    where de.salesman_number=? and de.contract_number=? and de.product_model=?\n" +
                ")\n" +
                "insert into supply_center_stock(supply_center, product_model, supply_staff, date, purchase_price, quantity, type)\n" +
                "select (select name from det),?,?,?,0,(select quantity from det)-?,'update'\n" +
                "where exists(select * from detail\n" +
                "    where salesman_number=? and contract_number=? and product_model=?);";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            //System.out.println(preparedStatement.toString());
            preparedStatement.setString(1, salesman);
            preparedStatement.setString(2, contract);
            preparedStatement.setString(3, product_model);
            preparedStatement.setString(4, product_model);
            preparedStatement.setString(5,salesman);
            preparedStatement.setDate(6,new java.sql.Date(str_to_date.parse(lodgement_date).getTime()));
            preparedStatement.setInt(7,quantity);
            preparedStatement.setString(8, salesman);
            preparedStatement.setString(9, contract);
            preparedStatement.setString(10, product_model);
            //System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }catch (ParseException e) {
            e.printStackTrace();
        }
        if(quantity!=0)
        {
            sql="update detail\n" +
                    "set quantity=?,estimated_delivery_date=?,lodgement_date=?\n" +
                    "where salesman_number=? and contract_number=? and product_model=?;";
            //System.out.println(sql);
            try {
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                //System.out.println(preparedStatement.toString());
                preparedStatement.setInt(1, quantity);
                preparedStatement.setDate(2, new java.sql.Date(str_to_date.parse(estimate_delivery_date).getTime()));
                preparedStatement.setDate(3, new java.sql.Date(str_to_date.parse(lodgement_date).getTime()));
                preparedStatement.setString(4, salesman);
                preparedStatement.setString(5, contract);
                preparedStatement.setString(6,product_model);
                //System.out.println(preparedStatement.toString());

                result = preparedStatement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else
        {
            sql="delete from detail\n" +
                    "where salesman_number=? and contract_number=? and product_model=?;";
            //System.out.println(sql);
            try {
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1, salesman);
                preparedStatement.setString(2, contract);
                preparedStatement.setString(3,product_model);
                //System.out.println(preparedStatement.toString());

                result = preparedStatement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    public int deleteOrder(String contract,String salesman,int seq)
    {
        SimpleDateFormat str_to_date = new SimpleDateFormat("yyyy-MM-dd");
        int result = 0;
        String sql = "with det as\n" +
                "    (\n" +
                "        select sc.name as supply_center,estimated_delivery_date as date, product_model as model,quantity,dense_rank() over(order by (estimated_delivery_date, product_model)desc) as rank\n" +
                "        from detail de join contract c on c.contract_number = de.contract_number\n" +
                "        join client_enterprise ce on ce.client_enterprise_name = c.client_enterprise_name\n" +
                "        join district d on ce.city = d.city_name and ce.country = d.country_name\n" +
                "        join supply_center sc on d.main_area = sc.name\n" +
                "        where salesman_number=? and de.contract_number=? \n" +
                "    )\n" +
                "insert into supply_center_stock(supply_center, product_model, supply_staff, date, purchase_price, quantity, type)\n" +
                "select (select max(supply_center) from det where rank=?),(select max(model) from det where rank=?),?,(select max(date) from det where rank=?),0,(select max(quantity) from det where rank=?),'update'\n" +
                "where exists(select * from detail\n" +
                "    where salesman_number=? and contract_number=?) \n" +
                "and exists(select * from det where rank>=?);";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            //System.out.println(preparedStatement.toString());
            preparedStatement.setString(1, salesman);
            //System.out.println(preparedStatement.toString());
            preparedStatement.setString(2, contract);
            preparedStatement.setInt(3,seq);
            preparedStatement.setInt(4,seq);
            preparedStatement.setString(5, salesman);
            preparedStatement.setInt(6,seq);
            preparedStatement.setInt(7,seq);
            preparedStatement.setString(8, salesman);
            preparedStatement.setString(9, contract);
            preparedStatement.setInt(10,seq);
            //System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        sql = "with det1 as\n" +
                "    (\n" +
                "        select estimated_delivery_date as date, product_model as model,quantity,dense_rank() over(order by (estimated_delivery_date, product_model)desc) as rank\n" +
                "        from detail\n" +
                "        where salesman_number=? and contract_number=?\n" +
                "    )\n" +
                "delete from detail\n" +
                "where salesman_number=? and contract_number=? and product_model=(select max(model) from det1 where rank=?) and estimated_delivery_date=(select max(date) from det1 where rank=?);";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, salesman);
            preparedStatement.setString(2, contract);
            preparedStatement.setString(3, salesman);
            preparedStatement.setString(4, contract);
            preparedStatement.setInt(5,seq);
            preparedStatement.setInt(6,seq);
            //System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    public String getAllStaffCount()
    {
        StringBuilder sb = new StringBuilder();
        String sql = "select count(*),type\n" +
                "from staff\n" +
                "group by type";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                sb.append(resultSet.getString("count")).append("\t");
                sb.append(resultSet.getString("type")).append("\t");
                sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    public String getContractCount()
    {
        StringBuilder sb = new StringBuilder();
        String sql = "select count(*)\n" +
                "from contract";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                sb.append(resultSet.getString("count")).append("\t");
                sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    public String getOrderCount()
    {
        StringBuilder sb = new StringBuilder();
        String sql = "select count(*)\n" +
                "from detail";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                sb.append(resultSet.getString("count")).append("\t");
                sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    public String getNeverSoldProductCount()
    {
        StringBuilder sb = new StringBuilder();
        String sql = "with det as\n" +
                "    (\n" +
                "        select sum(quantity) as sum,product_model\n" +
                "        from supply_center_stock\n" +
                "        group by product_model\n" +
                "    ),\n" +
                "det1 as\n" +
                "    (\n" +
                "        select distinct product_model\n" +
                "        from detail\n" +
                "    )\n" +
                "select count(*)\n" +
                "from det\n" +
                "where not exists(select * from det1 where det1.product_model=det.product_model);";
        //System.out.println(sql);
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                sb.append(resultSet.getString("count")).append("\t");
                sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    public String getFavoriteProductModel()
    {
        StringBuilder sb = new StringBuilder();
        String sql = "with det as\n" +
                "    (\n" +
                "        select sum(quantity) as quantity,product_model\n" +
                "        from supply_center_stock\n" +
                "        where type='sell'\n" +
                "        group by product_model\n" +
                "    ),\n" +
                "det1 as\n" +
                "    (\n" +
                "        select min(quantity) as quantity\n" +
                "        from det\n" +
                "    )\n" +
                "select det.product_model,-det.quantity as quantity\n" +
                "from det\n" +
                "where det.quantity=(select quantity from det1);";
        //System.out.println(sql);
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                sb.append(resultSet.getString("product_model")).append("\t");
                sb.append(resultSet.getString("quantity")).append("\t");
                sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    public String getAvgStockByCenter()
    {
        StringBuilder sb = new StringBuilder();
        String sql = "with det as\n" +
                "    (\n" +
                "        select round(sum(quantity),1) as quantity,product_model,supply_center\n" +
                "        from supply_center_stock\n" +
                "        group by product_model,supply_center_stock.supply_center\n" +
                "        order by supply_center\n" +
                "    ),\n" +
                "det1 as\n" +
                "    (\n" +
                "        select count(*) as cnt,supply_center\n" +
                "        from det\n" +
                "        group by det.supply_center\n" +
                "    )\n" +
                "select supply_center,round(1.0* sum(quantity)/(1.0*(select cnt from det1 where det1.supply_center=det.supply_center)),1) as num\n" +
                "from det\n" +
                "group by supply_center\n" +
                "order by supply_center";
        //System.out.println(sql);
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                sb.append(resultSet.getString("supply_center")).append("\t");
                sb.append(resultSet.getString("num")).append("\t");
                sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    public String getProductByNumber(String product_number)
    {
        StringBuilder sb = new StringBuilder();
        String sql = "select sum(quantity),supply_center,product_model\n" +
                "from supply_center_stock s\n" +
                "    join model_price mp on mp.model = s.product_model\n" +
                "    join product p on p.number = mp.number\n" +
                "where p.number= ?\n" +
                "group by supply_center,product_model";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1,product_number);
            //System.out.println(preparedStatement.toString());
            resultSet = preparedStatement.executeQuery();
            sb.append("supply_center\tmodel\tquantity\n");
            while (resultSet.next()) {
                sb.append(resultSet.getString("supply_center")).append("\t");
                sb.append(resultSet.getString("product_model")).append("\t");
                sb.append(resultSet.getString("sum")).append("\t");
                sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    public String getContractInfo(String contract_number)
    {
        StringBuilder sb = new StringBuilder();
        String sql1="select contract_number,client_enterprise_name,s1.name,s1.supply_center\n" +
                "from contract c join staff s1 on c.contract_manager=s1.number\n" +
                "where contract_number= ?;";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql1);
            preparedStatement.setString(1,contract_number);
            //System.out.println(preparedStatement.toString());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                sb.append("contract_number: ");
                sb.append(resultSet.getString("contract_number")).append("\n");
                sb.append("enterprise: ");
                sb.append(resultSet.getString("client_enterprise_name")).append("\n");
                sb.append("manager: ");
                sb.append(resultSet.getString("name")).append("\n");
                sb.append("supply_center: ");
                sb.append(resultSet.getString("supply_center")).append("\n");
                //sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String sql = "select c.contract_number,s1.name as name1,client_enterprise_name,s1.supply_center,product_model,s2.name as name2,quantity,estimated_delivery_date,lodgement_date\n" +
                "from contract c\n" +
                "    join detail d on c.contract_number = d.contract_number\n" +
                "    join staff s1 on c.contract_manager=s1.number\n" +
                "    join staff s2 on d.salesman_number=s2.number\n" +
                "where c.contract_number=?;";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1,contract_number);
            //System.out.println(preparedStatement.toString());
            resultSet = preparedStatement.executeQuery();
            sb.append("product_model\tsalesman\tquantity\t estimate_delivery_date\tlodgement_date    \n");
            while (resultSet.next()) {
                sb.append(resultSet.getString("product_model")).append("\t");
                sb.append(resultSet.getString("name2")).append("\t");
                sb.append(resultSet.getString("quantity")).append("\t");
                sb.append(resultSet.getString("estimated_delivery_date")).append("\t");
                sb.append(resultSet.getString("lodgement_date")).append("\t");
                sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    public int delete(int number)
    {
        SimpleDateFormat str_to_date = new SimpleDateFormat("yyyy-MM-dd");
        int result = 0;
        String sql = "delete from staff\n" +
                "where number=?;";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            //System.out.println(preparedStatement.toString());
            preparedStatement.setString(1, String.valueOf(number));


            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    public int insert(int num, String name,int age,String gender,int number,String supply_center,String mobile_number,String type)
    {
        SimpleDateFormat str_to_date = new SimpleDateFormat("yyyy-MM-dd");
        int result = 0;
        String sql = "insert into staff(name,age,gender,number,supply_center,mobile_phone,type,id)\n" +
                "                    values(?,?,?,?,?,?,?,?) on conflict (number) do nothing";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            //System.out.println(preparedStatement.toString());
            preparedStatement.setString(1,name);
            preparedStatement.setInt(2,age);
            preparedStatement.setString(3,gender);
            preparedStatement.setInt(4,number);
            preparedStatement.setString(5,supply_center);
            preparedStatement.setString(6,mobile_number);
            preparedStatement.setString(7,type);
            preparedStatement.setInt(8,num);
            //System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    public int update(int ori,int tar)
    {
        SimpleDateFormat str_to_date = new SimpleDateFormat("yyyy-MM-dd");
        int result = 0;
        String sql = "update staff\n" +
                "set number= ?\n" +
                "where number= ?";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            //System.out.println(preparedStatement.toString());
            preparedStatement.setString(1,String.valueOf(tar));
            preparedStatement.setString(2,String.valueOf(ori));
            //System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    public String select(String type)
    {
        StringBuilder sb = new StringBuilder();
        String sql = "select number\n" +
                "from staff\n" +
                "where type=?;";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1,type);
            //System.out.println(preparedStatement.toString());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                sb.append(resultSet.getString("number")).append("\t");
                sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}

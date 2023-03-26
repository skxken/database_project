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

public class Client {

    public static void main(String[] args) {
        SimpleDateFormat str_to_date = new SimpleDateFormat("yyyy-MM-dd");
        try {
            DataManipulation dm = new DataFactory().createDataManipulation();
            dm.openDatasource();
            //dm.insert(1024, "Sun Kaixuan",20,"Male",12012008,"Asia","13070853107","Salesman");//插入一行员工
            //dm.update(12012008,12012000);//把前一个编号的员工编号改成后一个
            //dm.delete(12012000);//API1，删除指定编号的员工
            //System.out.println(dm.select("Director"));//找出指定类型员工的编号
            //dm.stockIn("Southwestern China","LaptopC6","11110111","2008-01-01",100,100);//API2
            //dm.placeOrder("CSE0000101","ENI","LaptopC6",125,"12112115","2022-01-01","2022-01-01","2022-01-01","11110209","Finished");
            //dm.updateOrder("CSE0000101","LaptopC6","11110209",35,"2022-01-01","2022-02-01");
            //dm.deleteOrder("CSE0000101","11110209",1);
            //System.out.println(dm.getAllStaffCount());//6
            //System.out.println(dm.getContractCount());//7
            //System.out.println(dm.getOrderCount());//8
            //System.out.println(dm.getNeverSoldProductCount());//9
            //System.out.println(dm.getFavoriteProductModel());//10
            //System.out.println(dm.getAvgStockByCenter());//11
            //System.out.println(dm.getProductByNumber("A50L172"));//12
            //System.out.println(dm.getContractInfo("CSE0000106"));//13
            dm.closeDatasource();
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}


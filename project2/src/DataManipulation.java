public interface DataManipulation {

    public void openDatasource();
    public void closeDatasource();
    public int stockIn(String supply_center,String product_model,String supply_staff,String date,int purchase_price,int quantity);
    public int placeOrder(String contract_num,String enterprise,String product_model,int quantity,String contract_manager,String contract_date,String estimated_delivery_date,String lodgement_date,String salesman_num,String contract_type);
    public int updateOrder(String contract,String product_model,String salesman,int quantity,String estimate_delivery_date,String lodgement_date);
    public int deleteOrder(String contract,String salesman,int seq);
    public String getAllStaffCount();
    public String getContractCount();
    public String getOrderCount();
    public String getNeverSoldProductCount();
    public String getFavoriteProductModel();
    public String getAvgStockByCenter();
    public String getProductByNumber(String product_number);
    public String getContractInfo(String contract_number);
    public int delete(int number);
    public int insert(int num, String name,int age,String gender,int number,String supply_center,String mobile_number,String type);
    public int update(int ori,int tar);
    public String select(String type);
}

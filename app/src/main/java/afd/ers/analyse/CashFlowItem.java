package afd.ers.analyse;

public class CashFlowItem {
    private String name;
    private String revenue;
    private String amount;

    public CashFlowItem(String name,String amount, String revenue) {
        this.name = name;
        this.amount = amount;
        this.revenue = revenue;
    }

    public String getName() {
        return name;
    }
    public String getRevenue(){
        return revenue;
    }
    public String getAmount(){return amount;}
}

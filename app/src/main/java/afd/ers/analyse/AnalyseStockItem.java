package afd.ers.analyse;

public class AnalyseStockItem {
    private int ID;
    private String category;
    private String name;
    private String picture;
    private String proposedAmountADay;
    private String stock;

    public AnalyseStockItem(int ID, String category, String name, String picture, String stock, String proposedAmountADay) {
        this.ID = ID;
        this.category = category;
        this.name = name;
        this.picture = picture;
        this.proposedAmountADay = proposedAmountADay;
        this.stock = stock;
    }

    public int getID() {
        return ID;
    }
    public String getCategory() {
        return category;
    }
    public String getName() {
        return name;
    }
    public String getPicture(){
        return picture;
    }
    public String getStock(){return stock;}
    public String getProposedAmountADay(){return proposedAmountADay;}
    public String getAmountBuy(){return String.valueOf(Math.max(0,Float.valueOf(proposedAmountADay)-Float.valueOf(stock)));}
}

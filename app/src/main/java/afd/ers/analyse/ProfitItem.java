package afd.ers.analyse;


public class ProfitItem {

    private String name;
    private float profit;
    private String picture;

    public String getName() {
        return name;
    }

    public float getProfit() {
        return profit;
    }

    public String getPicture() {
        return picture;
    }

    public ProfitItem(String name, String picture, float profit) {
        this.name = name;
        this.profit = profit;
        this.picture = picture;
    }
}

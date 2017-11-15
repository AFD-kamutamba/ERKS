package afd.ers;


public class StockItem {
    private int ID;
    private String category;
    private String name;
    private float price;
    private int amount;
    private String picture;
    private int itemID;
    private int stock;
    public StockItem(int ID, String category, String name, float price, int amount) {
        this.ID = ID;
        this.category = category;
        this.name = name;
        this.price = (float)Math.floor(price * 10.0) / (float) 10.0;
        this.amount = amount;
    }
    public StockItem(int ID, String category, String name, float price, int amount, String picture) {
        this.ID = ID;
        this.category = category;
        this.name = name;
        this.price = (float)Math.floor(price * 10.0) / (float) 10.0;
        this.amount = amount;
        this.picture = picture;
    }
    public StockItem(int ID,int itemID, String category, String name, float price, int amount, String picture,int stock) {
        this.ID = ID;
        this.category = category;
        this.name = name;
        this.price = (float)Math.floor(price * 10.0) / (float) 10.0;
        this.amount = amount;
        this.picture = picture;
        this.itemID = itemID;
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

    public float getPrice() {
        return price;
    }

    public float getNegativePrice() {
        return -1 * price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getAmount() {
        return amount;
    }

    public void increaseAmount(int extra) {this.amount = this.amount + extra; }

    public String getPicture(){
        return picture;
    }

    public int getItemID(){return  itemID;}

    public int getStock(){return  stock;}
}

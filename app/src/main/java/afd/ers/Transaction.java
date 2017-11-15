package afd.ers;


public class Transaction {
    private int record_ID;
    private int item_ID;
    private float price;
    private int amount;
    private String date;

    public Transaction(int record_ID, int item_ID, float price, int amount, String date) {
        this.record_ID = record_ID;
        this.item_ID = item_ID;
        this.price = (float)Math.floor(price*10.0)/(float)10.0;;
        this.amount = amount;
        this.date = date;
    }

    public int getRecordID() {
        return record_ID;
    }

    public int getItemID() {
        return item_ID;
    }

    public float getPrice() {
        return price;
    }

    public float getNegativePrice() {
        return -1*price;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }
}

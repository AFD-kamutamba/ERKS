package afd.ers;


public class ClockIn {
    private String name;
    private String date;
    private String start_time;
    private String end_time;
    private float start_money;
    private float end_money;
    private float difference;
    private boolean discrepancy;

    public ClockIn(String name, String date, String start_time, String end_time, float start_money, float end_money, float difference) {
        this.name = name;
        this.date = date;
        this.start_time = start_time;
        this.end_time = end_time;
        this.start_money = start_money;
        this.end_money = end_money;
        this.difference = difference;
    }
    public ClockIn(String name, String date, String start_time, String end_time, float start_money, float end_money,float difference,boolean discrepancy) {
        this.name = name;
        this.date = date;
        this.start_time = start_time;
        this.end_time = end_time;
        this.start_money = start_money;
        this.end_money = end_money;
        this.difference = difference;
        this.discrepancy = discrepancy;
    }



    public String getClockInName() {
        return name;
    }

    public String getClockInDate() {
        return date;
    }

    public String getClockInStartTime() {
        return start_time;
    }

    public String getClockInEndTime() {
        return end_time;
    }

    public float getClockInStartMoney() {
        return start_money;
    }

    public float getClockInEndMoney() {
        return end_money;
    }

    public boolean getClockInDiscrepancy(){
        return discrepancy;
    }

    public float getDifference() {
        return difference;
    }
}

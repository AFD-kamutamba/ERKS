package afd.ers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import afd.ers.analyse.AnalyseActivity;
import afd.ers.analyse.CashFlowActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public Dialog adminDialog;
    public TextView adminPassword;
    public java.lang.Class targetClass = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Kiosk");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.header_pattern));
    }

    // Override the back button to do nothing
    @Override
    public void onBackPressed() {
    }

    public void newSell(View view) {
        Intent intent = new Intent(this, SellingActivity.class);
        startActivity(intent);
    }

    public void newBuy(View view) {
        Intent intent = new Intent(this, StockActivity.class);
        startActivity(intent);
        /*
        LayoutInflater factory = LayoutInflater.from(this);
        targetClass = StockActivity.class;
        final View dialogView = factory.inflate(
                R.layout.admin_popup, null);
        adminDialog = new Dialog(this, R.style.ourTheme);
        adminDialog.setCancelable(false);
        adminDialog.setContentView(dialogView);
        adminDialog.show();
        adminPassword= (TextView)dialogView.findViewById(R.id.admin_password);
        */
    }

    public void newProducts(View view) {
        Intent intent = new Intent(this, ProductActivity.class);
        startActivity(intent);
        /*
        LayoutInflater factory = LayoutInflater.from(this);
        targetClass = ProductActivity.class;
        final View dialogView = factory.inflate(
                R.layout.admin_popup, null);
        adminDialog = new Dialog(this, R.style.ourTheme);
        adminDialog.setCancelable(false);
        adminDialog.setContentView(dialogView);
        adminDialog.show();
        adminPassword= (TextView)dialogView.findViewById(R.id.admin_password);
        */
    }

    public void newAnalysis(View view) {
        Intent intent = new Intent(this, AnalyseActivity.class);
        startActivity(intent);
    }

    public void newCashFlow(View view) {
        Intent intent = new Intent(this, CashFlowActivity.class);
        startActivity(intent);
    }

    public void newExtra(View view) {
        Intent intent = new Intent(this, ExtraActivity.class);
        startActivity(intent);
        /*
        LayoutInflater factory = LayoutInflater.from(this);
        targetClass = ExtraActivity.class;
        final View dialogView = factory.inflate(
                        R.layout.admin_popup, null);
        adminDialog = new Dialog(this, R.style.ourTheme);
        adminDialog.setCancelable(false);
        adminDialog.setContentView(dialogView);
        adminDialog.show();
        adminPassword= (TextView)dialogView.findViewById(R.id.admin_password);
        */
    }

    public void newEmployees(View view) {
        Intent intent = new Intent(this, EmployeeActivity.class);
        startActivity(intent);
        /*
        LayoutInflater factory = LayoutInflater.from(this);
        targetClass = ExtraActivity.class;
        final View dialogView = factory.inflate(
                        R.layout.admin_popup, null);
        adminDialog = new Dialog(this, R.style.ourTheme);
        adminDialog.setCancelable(false);
        adminDialog.setContentView(dialogView);
        adminDialog.show();
        adminPassword= (TextView)dialogView.findViewById(R.id.admin_password);
        */
    }

    public void cancelAdmin(View view){
        adminDialog.cancel();
    }
    public void confirmAdmin(View view){
        if(!checkValidPassword(targetClass)) {
            adminPassword.setText("");
            Toast.makeText(getApplicationContext(),"Wrong Password!",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkValidPassword(java.lang.Class activity) {

        if (adminPassword.getText().toString().equalsIgnoreCase("QWERTY")) {
            Intent intent = new Intent(this, activity);
            startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    public void addLetterToPassword(String letter){
        adminPassword.setText(adminPassword.getText()+letter);
    }
    public void pressedBackspace(View view){
        adminPassword.setText(
                adminPassword.getText().subSequence(0,Math.max(0,adminPassword.getText().length() -1)));
    }

    public void pressedQ(View view){
        addLetterToPassword("Q");
    }
    public void pressedW(View view){
        addLetterToPassword("W");
    }
    public void pressedE(View view){
        addLetterToPassword("E");
    }
    public void pressedR(View view){
        addLetterToPassword("R");
    }
    public void pressedT(View view){
        addLetterToPassword("T");
    }
    public void pressedY(View view){
        addLetterToPassword("Y");
    }
    public void pressedU(View view){
        addLetterToPassword("U");
    }
    public void pressedI(View view){
        addLetterToPassword("I");
    }
    public void pressedO(View view){
        addLetterToPassword("O");
    }
    public void pressedP(View view){
        addLetterToPassword("P");
    }
    public void pressedA(View view){
        addLetterToPassword("A");
    }
    public void pressedS(View view){
        addLetterToPassword("S");
    }
    public void pressedD(View view){
        addLetterToPassword("D");
    }
    public void pressedF(View view){
        addLetterToPassword("F");
    }
    public void pressedG(View view){
        addLetterToPassword("G");
    }
    public void pressedH(View view){
        addLetterToPassword("H");
    }
    public void pressedJ(View view){
        addLetterToPassword("J");
    }
    public void pressedK(View view){
        addLetterToPassword("K");
    }
    public void pressedL(View view){
        addLetterToPassword("L");
    }
    public void pressedZ(View view){
        addLetterToPassword("Z");
    }
    public void pressedX(View view){
        addLetterToPassword("X");
    }
    public void pressedC(View view){
        addLetterToPassword("C");
    }
    public void pressedV(View view){
        addLetterToPassword("V");
    }
    public void pressedB(View view){
        addLetterToPassword("B");
    }
    public void pressedN(View view){
        addLetterToPassword("N");
    }
    public void pressedM(View view){
        addLetterToPassword("M");
    }
}

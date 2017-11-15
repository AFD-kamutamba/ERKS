package afd.ers.analyse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import afd.ers.MainActivity;
import afd.ers.R;

public class AnalyseActivity extends AppCompatActivity {
    private static final String TAG = "AnalyseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analyse_main);

        setTitle("Analysis");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.header_pattern));
    }

    public void newGraph(View view) {
        Intent intent = new Intent(this, GraphActivity.class);
        startActivity(intent);
    }

    public void newTopList(View view) {
        Intent intent = new Intent(this, TopSellActivity.class);
        startActivity(intent);
    }
    public void newStockAnalyse(View view){
        Intent intent = new Intent(this, StockAnalyseActivity.class);
        startActivity(intent);
    }
    public void newCashFlow(View view){
        Intent intent = new Intent(this, CashFlowActivity.class);
        startActivity(intent);
    }

    // Override the back button to do nothing
    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_extra, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*
            case R.id.record_list_go_back:
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
                return true;
               */
            case R.id.action_admin:
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivityForResult(mainIntent,1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

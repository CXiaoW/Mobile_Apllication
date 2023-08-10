package my.edu.utar.individualassignment;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class History extends AppCompatActivity {

    private SQLiteAdapter sqLiteAdapter;
    private ListView listView;
    private TextView tvNoData;
    private Button btnRefresh;
    private Button btnClear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        sqLiteAdapter = new SQLiteAdapter(this);
        tvNoData = findViewById(R.id.tv_na);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnClear = findViewById(R.id.btn_clear);
        listView = findViewById(R.id.list_view);

        setupUIListeners();
        // Inflate the custom layout for the ActionBar
        LayoutInflater inflater = LayoutInflater.from(this);
        View customActionBarView = inflater.inflate(R.layout.layout_action_bar, null);

        // Set the custom view for the ActionBar
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(customActionBarView);

        // Enable the back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set the title
        TextView titleTextView = customActionBarView.findViewById(R.id.title);
        titleTextView.setText("History");

        loadData();

    }

    // Override the onSupportNavigateUp() method to handle back arrow clicks
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupUIListeners() {
        btnRefresh.setOnClickListener(v -> loadData());

        btnClear.setOnClickListener(v -> clearDatabase());

    }

    private void loadData() {

        sqLiteAdapter.openToRead();
        Cursor cursor = sqLiteAdapter.queueAll();

        if (cursor.getCount() > 0) {
            showData(cursor);
        } else {
            showNoData();
        }

        sqLiteAdapter.close();
    }

    private void showData(Cursor cursor) {

        listView.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);

        List<Map<String, String>> data = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                // Check if the amount is greater than 0, meaning a valid calculation was stored
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(SQLiteAdapter.VALUE));
                if (amount > 0) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteAdapter.KEY_CONTENT));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteAdapter.KEY_CONTENT_2));
                    String formattedAmount = "RM " + new DecimalFormat("0.00").format(amount);
                    String details = "Date: " + date + "\nAmount: " + formattedAmount;

                    Map<String, String> item = new HashMap<>();
                    item.put("name", name);
                    item.put("details", details);
                    data.add(item);
                }
            } while (cursor.moveToNext());
        }

        String[] fromColumns = {"name", "details"};
        int[] toViews = {R.id.tv_name, R.id.tv_amount};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, data, R.layout.list_item, fromColumns, toViews);
        listView.setAdapter(simpleAdapter);
    }

    private void showNoData() {

        listView.setVisibility(View.GONE);
        tvNoData.setVisibility(View.VISIBLE);
    }

    private void clearDatabase() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Database")
                .setMessage("Are you sure you want to clear the database?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    sqLiteAdapter.openToWrite();
                    sqLiteAdapter.clearAll();
                    sqLiteAdapter.close();
                    loadData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

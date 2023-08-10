package my.edu.utar.individualassignment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    private static final String[] CALCULATION_METHODS = {"By Percentage", "By Ratio", "By Amount"};
    private SQLiteAdapter sqLiteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sqLiteAdapter = new SQLiteAdapter(this);

        Button btnEqualBreakdown = findViewById(R.id.equal_break);
        btnEqualBreakdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to EqualBreakdown activity
                Intent intent = new Intent(MainActivity.this, EqualBreakdown.class);
                startActivity(intent);
            }
        });

        Button btnCustomBreakdown = findViewById(R.id.custom_break);
        btnCustomBreakdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a dialog to choose the calculation method
                showCustomBreakdownDialog();
            }
        });

        Button btnHistory = findViewById(R.id.history);
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, History.class);
                startActivity(intent);
            }
        });

    }

    private void showCustomBreakdownDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Breakdown Type")
                .setItems(CALCULATION_METHODS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedMethod = CALCULATION_METHODS[which];
                        navigateToCustomBreakdown(selectedMethod);
                    }
                })
                .show();
    }

    private void navigateToCustomBreakdown(String calculationMethod) {
        Intent intent = new Intent(MainActivity.this, CustomBreakdown.class);
        intent.putExtra("breakdown_method", calculationMethod);
        startActivity(intent);
    }
}
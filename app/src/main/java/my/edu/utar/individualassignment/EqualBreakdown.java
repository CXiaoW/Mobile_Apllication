package my.edu.utar.individualassignment;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;


public class EqualBreakdown extends AppCompatActivity {

    private SQLiteAdapter sqLiteAdapter;

    private LinearLayout container;
    private EditText etTotalAmount;
    private Button btnCalculate;
    private List<String> individualNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equal);

        sqLiteAdapter = new SQLiteAdapter(this);

        container = findViewById(R.id.container);
        etTotalAmount = findViewById(R.id.etTotalAmount);
        individualNames = new ArrayList<>();

        showNumPeopleDialog();

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
        titleTextView.setText("Equal");
    }

    // Override the onSupportNavigateUp() method to handle back arrow clicks
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showNumPeopleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Number of People");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String numPeopleStr = input.getText().toString().trim();
                if (!TextUtils.isEmpty(numPeopleStr)) {
                    int numPeople = Integer.parseInt(numPeopleStr);
                    createInputFields(numPeople);
                } else {
                    showToast("Please enter the number of people.");
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });

        builder.show();
    }

    private void createInputFields(int numPeople) {
        container.removeAllViews(); // Clear existing views
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < numPeople; i++) {
            View personView = inflater.inflate(R.layout.layout_equal_input, container, false);

            EditText etName = personView.findViewById(R.id.etPersonsName);
            etName.setHint("Person " + (i + 1) + " Name");

            container.addView(personView);
        }

        // Add the calculate button outside the container
        if (btnCalculate == null) {
            btnCalculate = new Button(this);
            btnCalculate.setText("Calculate");
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            btnCalculate.setLayoutParams(layoutParams);

            // Set the onClick listener for the Calculate button
            btnCalculate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calculateEqualBreakdown(); // Calculate and display the equal breakdown
                }
            });

            // Add the button to the container's parent (which is a LinearLayout)
            ((LinearLayout) container.getParent()).addView(btnCalculate);
        }
    }


    private void calculateEqualBreakdown() {
        String totalAmountString = etTotalAmount.getText().toString().trim();
        double totalAmount = Double.parseDouble(totalAmountString);

        individualNames.clear(); // Clear the list of names

        // Iterate through the child views of the container to get individual names
        for (int i = 0; i < container.getChildCount(); i++) {
            View personView = container.getChildAt(i);
            EditText etName = personView.findViewById(R.id.etPersonsName);
            String name = etName.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("Please enter a name");
                return; // Exit the method if there's an empty name
            } else {
                etName.setError(null); // Clear error if the name is not empty
            }

            individualNames.add(name);
        }

        int numPeople = individualNames.size(); // Get the number of people from the list
        List<Double> individualAmounts = calculateEqualBreakdown(totalAmount, numPeople);
        displayEqualBreakdown(individualNames, individualAmounts);
    }

    private List<Double> calculateEqualBreakdown(double totalAmount, int numPeople) {
        List<Double> individualAmounts = new ArrayList<>();
        double equalAmount = totalAmount / numPeople;

        for (int i = 0; i < numPeople; i++) {
            individualAmounts.add(equalAmount);
        }

        return individualAmounts;
    }

    private void displayEqualBreakdown(List<String> individualNames,List<Double> individualAmounts) {
        container.removeAllViews();

        int numPeople = individualAmounts.size();

        // Increase the text size for the output text
        int textSize = getResources().getDimensionPixelSize(R.dimen.text_size_large);

        sqLiteAdapter.openToWrite();

        for (int i = 0; i < numPeople; i++) {
            String name = individualNames.get(i);
            double individualAmount = individualAmounts.get(i);

            // Format individualAmount to two significant figures
            String formattedAmount = String.format("%.2f", individualAmount);

            sqLiteAdapter.Result(name, individualAmount);

            TextView textView = new TextView(this);
            textView.setText(name + ": RM " + formattedAmount);

            // Set the text size for the TextView
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

            container.addView(textView);
        }

        sqLiteAdapter.close();

    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}

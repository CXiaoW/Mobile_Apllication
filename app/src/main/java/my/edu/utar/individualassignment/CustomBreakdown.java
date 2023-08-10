package my.edu.utar.individualassignment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.List;

public class CustomBreakdown extends AppCompatActivity {

    private SQLiteAdapter sqLiteAdapter;

    private LinearLayout container;
    private EditText etTotalAmount;
    private Button btnCalculate;

    private List<String> individualNames; // List to store individual names
    private List<Double> individualPercentages; // List to store individual percentages

    private String calculationMethod; // To store the selected calculation method

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);

        sqLiteAdapter = new SQLiteAdapter(this);

        container = findViewById(R.id.container);
        etTotalAmount = findViewById(R.id.etTotalAmount);

        individualNames = new ArrayList<>(); // Initialize the list of names
        individualPercentages = new ArrayList<>(); // Initialize the list of percentages

        // Retrieve the calculation method selected from MainActivity
        calculationMethod = getIntent().getStringExtra("breakdown_method");

        // Show the dialog to ask the user for the number of people
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
        titleTextView.setText("Custom");
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

    private boolean areInputFieldsFilled() {
        for (int i = 0; i < container.getChildCount(); i++) {
            View personView = container.getChildAt(i);
            EditText etName = personView.findViewById(R.id.etPersonName);
            EditText etPercentage = personView.findViewById(R.id.etPercentage);

            String name = etName.getText().toString().trim();
            String percentageString = etPercentage.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(percentageString)) {
                return false;
            }
        }
        return true;
    }

    private void createInputFields(int numPeople) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < numPeople; i++) {
            View personView = inflater.inflate(R.layout.layout_individual_input, container, false);

            EditText etName = personView.findViewById(R.id.etPersonName);
            EditText etPercentage = personView.findViewById(R.id.etPercentage);

            etName.setHint("Person " + (i + 1) + " Name");
            etPercentage.setHint("%/Ratio/Amount");

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
                    // Call the appropriate calculation method based on the selected breakdown method
                    switch (calculationMethod) {
                        case "By Percentage":
                            calculateByPercentage();
                            break;
                        case "By Ratio":
                            calculateByRatio();
                            break;
                        case "By Amount":
                            calculateByAmount();
                            break;
                    }
                }
            });

            // Add the button to the container's parent (which is a LinearLayout)
            ((LinearLayout) container.getParent()).addView(btnCalculate);
        }
    }

    private void calculateByPercentage() {
        // Get the total amount
        String totalAmountString = etTotalAmount.getText().toString().trim();

        if (TextUtils.isEmpty(totalAmountString)) {
            showToast("Please enter the total amount.");
            return;
        }

        double totalAmount = Double.parseDouble(totalAmountString);

        // Clear the lists before adding new values
        individualNames.clear();
        individualPercentages.clear();

        // Retrieve individual names and percentages from input fields
        for (int i = 0; i < container.getChildCount(); i++) {
            View personView = container.getChildAt(i);
            EditText etName = personView.findViewById(R.id.etPersonName);
            EditText etPercentage = personView.findViewById(R.id.etPercentage);

            String name = etName.getText().toString().trim();
            String percentageString = etPercentage.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("Please enter a name");
                return;
            } else {
                etName.setError(null); // Clear error if the name is not empty
            }

            if (TextUtils.isEmpty(percentageString)) {
                etPercentage.setError("Please enter the percentage");
                return;
            } else {
                double percentage = Double.parseDouble(percentageString);
                individualNames.add(name);
                individualPercentages.add(percentage);
            }
        }

        // Calculate individual amounts based on percentages
        List<Double> individualAmounts = calculateByPercentage(totalAmount, individualPercentages);
        displayCustomBreakdown(individualNames, individualAmounts);


    }

    private void calculateByRatio() {
        // Get the total amount
        String totalAmountString = etTotalAmount.getText().toString().trim();

        if (TextUtils.isEmpty(totalAmountString)) {
            showToast("Please enter the total amount.");
            return;
        }

        double totalAmount = Double.parseDouble(totalAmountString);

        // Save individual names and ratios in separate lists
        List<String> namesList = new ArrayList<>();
        List<Integer> ratiosList = new ArrayList<>();

        int totalRatio = 0;

        for (int i = 0; i < container.getChildCount(); i++) {
            View personView = container.getChildAt(i);
            EditText etName = personView.findViewById(R.id.etPersonName);
            String name = etName.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("Please enter a name");
            } else {
                etName.setError(null); // Clear error if the name is not empty
            }

            namesList.add(name);

            EditText etRatio = personView.findViewById(R.id.etPercentage); // Reusing the same input field for ratio
            String ratioString = etRatio.getText().toString().trim();

            if (TextUtils.isEmpty(ratioString)) {
                etRatio.setError("Please enter the ratio");
            } else {
                try {
                    int ratio = Integer.parseInt(ratioString);
                    totalRatio += ratio;
                    ratiosList.add(ratio);
                } catch (NumberFormatException e) {
                    etRatio.setError("Please enter a valid whole number for the ratio");
                }
            }
        }

        if (totalRatio == 0) {
            showToast("Total ratio cannot be zero.");
            return;
        }

        // Calculate individual amounts based on ratios
        List<Double> individualAmounts = calculateByRatio(totalAmount, totalRatio, ratiosList);
        displayCustomBreakdown(namesList, individualAmounts);

    }

    private void calculateByAmount() {
        // Get the total amount
        String totalAmountString = etTotalAmount.getText().toString().trim();

        if (TextUtils.isEmpty(totalAmountString)) {
            showToast("Please enter the total amount.");
            return;
        }

        double totalAmount = Double.parseDouble(totalAmountString);

        // Save individual names and amounts in separate lists
        List<String> namesList = new ArrayList<>();
        List<Double> amountsList = new ArrayList<>();
        double sumAmounts = 0; // Variable to calculate the sum of individual amounts

        for (int i = 0; i < container.getChildCount(); i++) {
            View personView = container.getChildAt(i);
            EditText etName = personView.findViewById(R.id.etPersonName);
            String name = etName.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("Please enter a name");
                return; // Exit the method if there's an empty name
            } else {
                etName.setError(null); // Clear error if the name is not empty
            }

            namesList.add(name);

            EditText etAmount = personView.findViewById(R.id.etPercentage); // Reusing the same input field for amount
            String amountString = etAmount.getText().toString().trim();

            if (TextUtils.isEmpty(amountString)) {
                etAmount.setError("Please enter the amount");
                return; // Exit the method if there's an empty amount
            } else {
                try {
                    double amount = Double.parseDouble(amountString);
                    sumAmounts += amount;
                    amountsList.add(amount);
                } catch (NumberFormatException e) {
                    etAmount.setError("Please enter a valid number for the amount");
                    return; // Exit the method if there's an invalid amount
                }
            }
        }

        if (Math.abs(sumAmounts - totalAmount) > 1e-6) {
            showToast("Sum of individual amounts does not match the total amount. Please reenter.");
            return; // Exit the method if the sums don't match
        }


        // Calculate individual percentages based on amounts
        List<Double> individualPercentages = calculateByAmount(sumAmounts, amountsList);
        displayCustomBreakdown(namesList, individualPercentages);
    }

    private List<Double> calculateByPercentage(double totalAmount, List<Double> individualPercentages) {
        List<Double> individualAmounts = new ArrayList<>();

        for (Double percentage : individualPercentages) {
            double individualAmount = (percentage / 100) * totalAmount;
            individualAmounts.add(individualAmount);
        }

        return individualAmounts;
    }

    private List<Double> calculateByRatio(double totalAmount, int totalRatio, List<Integer> individualRatios) {
        List<Double> individualAmounts = new ArrayList<>();

        for (Integer ratio : individualRatios) {
            double individualAmount = (ratio * totalAmount) / totalRatio;
            individualAmounts.add(individualAmount);
        }

        return individualAmounts;
    }

    private List<Double> calculateByAmount(double sumAmounts, List<Double> individualAmounts) {
        List<Double> individualPercentages = new ArrayList<>();
        double calculatedSum = 0.0;

        for (Double amount : individualAmounts) {
            calculatedSum += amount;
        }

        if (Math.abs(calculatedSum - sumAmounts) > 1e-6) {
            showToast("Sum of individual amounts does not match the total amount. Please reenter.");
            return null; // Indicate an error condition
        }

        // Calculate individual percentages based on amounts
        else {
            for (Double amount : individualAmounts) {
                individualPercentages.add(amount);
            }
        }

        return individualPercentages;
    }

    private void displayCustomBreakdown(List<String> individualNames, List<Double> individualAmounts) {
        container.removeAllViews();

        int numPeople = individualNames.size();

         switch (calculationMethod) {
             case "By Percentage":
                 if (individualAmounts.size() != numPeople) {
                     showToast("Please enter valid percentages for all individuals.");
                     return;
                 }
             case "By Ratio":
                 if (individualAmounts.size() != numPeople) {
                     showToast("Please enter valid ratios for all individuals.");
                     return;
                 }

         }

        // Increase the text size for the output text
        int textSize = getResources().getDimensionPixelSize(R.dimen.text_size_large);

        // Open the database for writing
        sqLiteAdapter.openToWrite();

        for (int i = 0; i < numPeople; i++) {
            String name = individualNames.get(i);
            double individualAmount = individualAmounts.get(i);

            // Format individualAmount to two significant figures
            String formattedAmount = String.format("%.2f", individualAmount);

            // Add the data to the database
            sqLiteAdapter.Result(name, individualAmount);

            TextView textView = new TextView(this);
            textView.setText(name + ": RM " + formattedAmount);

            // Set the text size for the TextView
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

            container.addView(textView);
        }

        // Close the database after adding data
        sqLiteAdapter.close();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}

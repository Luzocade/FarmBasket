package com.smartagriculture;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class EditCustomerProfile extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "AppTest>>>>";
    UiEnhancement uiEnhancement;
    TextInputEditText txtName, txtPhno, txtArea, txtDob, txtEmail;
    ImageButton btnPickDate;
    Button btnSave;
    SharedPreferences userDetails;
    FirebaseDatabase database;
    String lastPhno;


    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar cal = new GregorianCalendar(year, month, day);
        String formattedDate = DateFormat.format("dd/MM/yyyy", cal.getTime()).toString();
        txtDob.setText(formattedDate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_customer_profile);

        txtName = findViewById(R.id.txtName);
        txtPhno = findViewById(R.id.txtPhno);
        txtArea = findViewById(R.id.txtArea);
        txtDob = findViewById(R.id.txtDob);
        txtEmail = findViewById(R.id.txtEmail);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnSave = findViewById(R.id.btnSave);
        uiEnhancement = new UiEnhancement();
        userDetails = getSharedPreferences("userDetails", MODE_PRIVATE);
        database = FirebaseDatabase.getInstance();

        Log.i(TAG, "onCreate: userDetails: "+userDetails.getAll().toString());

        uiEnhancement.blur(this, R.drawable.crops, 10, true, findViewById(R.id.bg));
        txtName.setText(userDetails.getString("name", ""));
        txtEmail.setText(userDetails.getString("email", ""));
        txtDob.setText(userDetails.getString("dob", ""));
        txtPhno.setText(userDetails.getString("phno", ""));
        txtArea.setText(userDetails.getString("area", ""));
        lastPhno = userDetails.getString("phno", "");

        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment fragment = new DatePickerFragment();
                fragment.show(getSupportFragmentManager(), "date");
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtName.getText().toString().isEmpty()) {
                    txtName.setError("Name cannot be empty");
                    txtName.requestFocus();
                    return;
                }

                if (txtPhno.getText().toString().isEmpty()) {
                    txtPhno.setError("Phone number cannot be empty");
                    txtPhno.requestFocus();
                    return;
                } else if (txtPhno.getText().toString().length() != 10) {
                    txtPhno.setError("Invalid Phone number");
                    txtPhno.requestFocus();
                    return;
                }

                if (txtArea.getText().toString().isEmpty()) {
                    txtArea.setError("Area cannot be empty");
                    txtArea.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(txtEmail.getText().toString().trim()).matches()) {
                    txtEmail.setError("Invalid E-mail");
                    txtEmail.requestFocus();
                    return;
                }

                Customer c = new Customer(txtName.getText().toString().trim(),
                        txtPhno.getText().toString().trim(),
                        txtArea.getText().toString().trim());
                c.setEmail(txtEmail.getText().toString().trim());
                c.setDob(txtDob.getText().toString().trim());

                updateUser(c);
            }
        });
    }

    private void updateUser(Customer c) {
        if (!lastPhno.equalsIgnoreCase(txtPhno.getText().toString().trim())) {
            DatabaseReference cGroup = database.getReference("customers");
            DatabaseReference cMember = cGroup.child(lastPhno);
            cMember.getRef().removeValue();
            cMember = cGroup.child(txtPhno.getText().toString().trim());
            cMember.setValue(c);
        } else {
            DatabaseReference cGroup = database.getReference("customers");
            DatabaseReference cMember = cGroup.child(lastPhno);
            cMember.setValue(c);
        }
        SharedPreferences.Editor userEditor = userDetails.edit();
        userEditor.putString("email", c.getEmail());
        userEditor.putString("name", c.getName());
        userEditor.putString("dob", c.getDob());
        userEditor.putString("area", c.getArea());
        userEditor.putString("phno", c.getPhno());
        userEditor.apply();
        Log.i(TAG, "updateUser: userDetails: "+userDetails.getAll().toString());
        Toast.makeText(this, "Details succesfully updated", Toast.LENGTH_SHORT).show();
        finish();
    }
}

package com.smartagriculture;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class CustomerProfile extends Fragment {

    private static final String TAG = "AppTest>>>>";

    private Button btnSignOut,btnMyOrders,btnCall,btnEmail,btnAbout;
    private ViewGroup viewContainer;
    private SharedPreferences userDetails,userOrders;
    private TextInputEditText txtName, txtArea, txtPhno;
    private ImageButton btnEditUpdate,btnUpdateAdd;
    private TextView txtAddress;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_customer_profile, container, false);
        viewContainer = container;
        userDetails = getActivity().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        userOrders = getActivity().getSharedPreferences("userOrders", Context.MODE_PRIVATE);
        btnSignOut = v.findViewById(R.id.btnSignOut);
        txtName = v.findViewById(R.id.txtName);
        txtArea = v.findViewById(R.id.txtArea);
        txtPhno = v.findViewById(R.id.txtPhno);
        txtAddress = v.findViewById(R.id.txtAddress);
        btnEditUpdate = v.findViewById(R.id.btnEditUpdate);
        btnUpdateAdd = v.findViewById(R.id.btnUpdateAdd);
        btnMyOrders = v.findViewById(R.id.btnMyOrders);
        btnCall = v.findViewById(R.id.btnCall);
        btnEmail = v.findViewById(R.id.btnEmail);
        btnAbout = v.findViewById(R.id.btnAbout);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtName.setText(userDetails.getString("name",""));
        txtArea.setText(userDetails.getString("area",""));
        txtPhno.setText(userDetails.getString("phno",""));
        txtAddress.setText(userDetails.getString("addr",""));

        txtName.setKeyListener(null);
        txtArea.setKeyListener(null);
        txtPhno.setKeyListener(null);


        btnUpdateAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Objects.requireNonNull(getActivity()).startActivity(new Intent(getActivity(),EditCustomerAddress.class));
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor userEditor = userDetails.edit();
                userEditor.clear().apply();
                SharedPreferences.Editor userOrdersEditor = userOrders.edit();
                userOrdersEditor.clear().apply();
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getActivity(), "Successfully Signed out", Toast.LENGTH_SHORT).show();
                Objects.requireNonNull(getActivity()).startActivity(new Intent(getActivity(), Login.class));
                Objects.requireNonNull(getActivity()).finish();
            }
        });

        btnEditUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Objects.requireNonNull(getActivity()).startActivity(new Intent(getActivity(),EditCustomerProfile.class));
            }
        });

        btnMyOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Objects.requireNonNull(getActivity()).startActivity(new Intent(getActivity(),CustomerPendingOrders.class));
            }
        });

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //final String[] TO = {"kaliakr@gmail.com"};

                Intent emailIntent = new Intent(Intent.ACTION_SEND);

                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getActivity().getSharedPreferences("cc", 0).getString("ccemail", "kaliakr@gmail.com")});
                /*emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your subject");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");*/

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                    Log.i(TAG, "onClick: Email Sent");
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String PHNO = getActivity().getSharedPreferences("cc",0).getString("ccphno","8437860605");

                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_CALL);

                    intent.setData(Uri.parse("tel:" + PHNO));
                    getActivity().startActivity(intent);
                }

            }

        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 getActivity().startActivity(new Intent(getActivity(),AboutUs.class));
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        txtName.setText(userDetails.getString("name",""));
        txtArea.setText(userDetails.getString("area",""));
        txtPhno.setText(userDetails.getString("phno",""));
        txtAddress.setText(userDetails.getString("addr",""));
    }
}

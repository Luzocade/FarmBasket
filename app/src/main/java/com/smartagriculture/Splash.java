package com.smartagriculture;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Splash extends AppCompatActivity {

    TextView txtAppName;
    ProgressBar prgLoading;
    UiEnhancement uiEnhancement;
    FirebaseUser user;
    SharedPreferences userDetails;
    private SharedPreferences userOrders;
    private FirebaseDatabase database;
    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        uiEnhancement = new UiEnhancement();

        //view initialisation
        txtAppName = findViewById(R.id.txtAppName);
        prgLoading = findViewById(R.id.prgLoading);
        //end view initialisation

        //anims and other ui initialisations
        uiEnhancement.blur(this, R.drawable.crops, 5, true, findViewById(R.id.bg));
        uiEnhancement.hide(false, 0, 0, txtAppName, prgLoading);
        uiEnhancement.show(true, 500, 1000, txtAppName, prgLoading);
        //end anims and other ui initialisations

        //var init
        user = FirebaseAuth.getInstance().getCurrentUser();
        //Log.i("AppTest>>>>", "onCreate: "+user.getPhoneNumber());
        userDetails = getSharedPreferences("userDetails", MODE_PRIVATE);
        userOrders = getSharedPreferences("userOrders", MODE_PRIVATE);
        database = FirebaseDatabase.getInstance();
        //end var init

        getcc();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!connected) {
                    Toast.makeText(Splash.this, "There is a internet connectivity problem, please try again later", Toast.LENGTH_SHORT).show();
                    Splash.this.finish();
                }

            }
        }, 15000);
    }

    private void getcc(){
        DatabaseReference ccemail = database.getReference("cc/email");
        final DatabaseReference ccphno = database.getReference("cc/phno");

        ccemail.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getSharedPreferences("cc",MODE_PRIVATE).edit().putString("ccemail",dataSnapshot.getValue(String.class)).apply();
                ccphno.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        connected = true;
                        getSharedPreferences("cc",MODE_PRIVATE).edit().putString("ccphno",dataSnapshot.getValue(String.class)).apply();
                        openIntent();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("AppTest>>>>", "onCancelled: ", databaseError.toException() );
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if(databaseError.getMessage().contains("Permission denied"))
                    openIntent();
            }
        });
    }

    private void openIntent(){
        if (user == null||userDetails.getString("type","").isEmpty()) {
            Intent i = new Intent(Splash.this, Login.class);
            startActivity(i);
            //Log.i("AppTest>>>>","logged in user: "+ userDetails.getAll());
            finish();
        }else{
            //Log.i("AppTest>>>>","logged in user: "+user.getPhoneNumber()+" "+ userDetails.getAll());
            if(userDetails.getString("type","").equalsIgnoreCase("farmer")){
                startActivity(new Intent(Splash.this,FarmerMain.class));
                finish();
            }else if(userDetails.getString("type","").equalsIgnoreCase("customer")){
                startActivity(new Intent(Splash.this,CustomerMain.class));
                finish();
            }

        }
    }


}

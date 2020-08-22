package com.smartagriculture;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class AboutUs extends AppCompatActivity {

    private UiEnhancement uiEnhancement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        uiEnhancement = new UiEnhancement();
        uiEnhancement.blur(this,R.drawable.crops,20,true,findViewById(R.id.bg));
    }
}

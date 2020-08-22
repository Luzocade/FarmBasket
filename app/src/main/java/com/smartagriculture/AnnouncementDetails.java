package com.smartagriculture;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class AnnouncementDetails extends AppCompatActivity {

    TextView txtPostDate,txtMsg;
    UiEnhancement uiEnhancement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_details);

        uiEnhancement = new UiEnhancement();
        uiEnhancement.blur(this,R.drawable.crops,20,true,findViewById(R.id.bg));

        txtPostDate = findViewById(R.id.txtPostDate);
        txtMsg = findViewById(R.id.txtMsg);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            txtPostDate.setText(String.format("Posted on: %s", android.text.format.DateFormat.format("dd/MM/yyyy", b.getLong("postDate")).toString()));
            txtMsg.setText(b.getString("msg"));
        }
    }
}

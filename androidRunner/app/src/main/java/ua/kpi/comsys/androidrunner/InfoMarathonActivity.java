package ua.kpi.comsys.androidrunner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class InfoMarathonActivity extends AppCompatActivity {

    private TextView marathonName;
    private TextView marathonInfo;
    private Button runMarathon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_marathon);

        marathonName = findViewById(R.id.marathon_name);
        marathonInfo = findViewById(R.id.information_about_smth);
        runMarathon = findViewById(R.id.start_marathon);

        runMarathon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeActivity.redirectActivity(InfoMarathonActivity.this, SingleMarathonActivity.class);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void ClickBack(View view)
    {
        super.onBackPressed();
    }
}
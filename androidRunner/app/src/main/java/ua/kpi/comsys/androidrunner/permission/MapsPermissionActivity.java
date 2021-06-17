package ua.kpi.comsys.androidrunner.permission;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import ua.kpi.comsys.androidrunner.HomeActivity;
import ua.kpi.comsys.androidrunner.R;
import ua.kpi.comsys.androidrunner.RunActivity;

import static android.content.ContentValues.TAG;

public class MapsPermissionActivity extends AppCompatActivity {

    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_permission);

        ImageView permissionCheck = (ImageView) findViewById(R.id.permission_button);
        //View view = findViewById(R.id.view);

        Animation animationScaleBack = AnimationUtils.loadAnimation(this, R.anim.run_scale_back);
        animationScaleBack.setDuration(1000);
        animationScaleBack.setRepeatCount(1000);

        Animation animationScale = AnimationUtils.loadAnimation(this, R.anim.run_scale);
        animationScale.setDuration(1000);
        animationScale.setRepeatCount(1000);

        //view.startAnimation(animationScale);
        permissionCheck.startAnimation(animationScaleBack);

        permissionCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeActivity.redirectActivity(MapsPermissionActivity.this, RunActivity.class);
            }
        });
    }
}
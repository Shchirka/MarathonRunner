package ua.kpi.comsys.androidrunner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.View;

import ua.kpi.comsys.androidrunner.permission.MapsPermissionActivity;

public class SettingsActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        drawerLayout = findViewById(R.id.android_runner_navigation);
    }

    public void ClickMenu(View view){
        HomeActivity.openDrawer(drawerLayout);
    }

    public void ClickLogo(View view){
        HomeActivity.closeDrawer(drawerLayout);
    }

    public void ClickHome(View view){
        HomeActivity.redirectActivity(this, HomeActivity.class);
    }

    public void ClickRun(View view){
        HomeActivity.redirectActivity(this, MapsPermissionActivity.class);
    }

    public void ClickFriends(View view){
        HomeActivity.redirectActivity(this, FriendsActivity.class);
    }

    public void ClickSettings(View view){
        recreate();
    }

    public void ClickAboutUs(View view){
        HomeActivity.redirectActivity(this, AboutUsActivity.class);
    }

    public void ClickLogout(View view){
        HomeActivity.logout(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        HomeActivity.closeDrawer(drawerLayout);
    }
}
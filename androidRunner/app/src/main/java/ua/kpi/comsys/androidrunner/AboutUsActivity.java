package ua.kpi.comsys.androidrunner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.View;

import ua.kpi.comsys.androidrunner.permission.MapsPermissionActivity;

public class AboutUsActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

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
        HomeActivity.redirectActivity(this, SettingsActivity.class);
    }

    public void ClickAboutUs(View view){
        recreate();
    }

    public void ClickLogout(View view){
        HomeActivity.logout(this);
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
        HomeActivity.closeDrawer(drawerLayout);
    }
}
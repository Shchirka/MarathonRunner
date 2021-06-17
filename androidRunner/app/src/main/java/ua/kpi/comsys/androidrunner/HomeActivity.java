package ua.kpi.comsys.androidrunner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import ua.kpi.comsys.androidrunner.account.LoginActivity;
import ua.kpi.comsys.androidrunner.permission.MapsPermissionActivity;

public class HomeActivity extends AppCompatActivity {

    private StorageReference storageReference;
    private FirebaseUser user;
    private String userID;

    DrawerLayout drawerLayout;
    public static ImageView accountPhoto;
    ImageView refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();
        storageReference = FirebaseStorage.getInstance().getReference().child("Users").child(userID)
                .child("accountPhotos").child("accountPhoto.jpg");

        drawerLayout = findViewById(R.id.android_runner_navigation);
        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        accountPhoto = findViewById(R.id.account_image);
        accountPhoto.setDrawingCacheEnabled(true);
        final File localFile;
        try {
            localFile = File.createTempFile("accountPhoto", "jpg");
            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    accountPhoto.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    accountPhoto.setImageResource(R.drawable.ic_empty_photo);
                    Toast.makeText(HomeActivity.this, "Downloading photo failed",
                            Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ClickMenu(View view){
        openDrawer(drawerLayout);
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void ClickLogo(View view){
        closeDrawer(drawerLayout);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void ClickHome(View view){
        recreate();
    }

    public void ClickRun(View view){
        redirectActivity(this, MapsPermissionActivity.class);
    }

    public void ClickAboutUs(View view){
        redirectActivity(this, AboutUsActivity.class);
    }

    public void ClickFriends(View view){
        redirectActivity(this, FriendsActivity.class);
    }

    public void ClickSettings(View view){
        redirectActivity(this, SettingsActivity.class);
    }

    public void ClickLogout(View view){
        logout(this);
    }

    /*public void ClickViewPhoto(View view){
        Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
        intent.putExtra("image_id", R.drawable.ic_empty_photo);
        startActivity(intent);
    }*/

    public void ClickStatus(View view){
        redirectActivity(this, StatusActivity.class);
    }

    public void ClickMarathons(View view){
        redirectActivity(this, MarathonsActivity.class);
    }

    public void ClickStore(View view){
        redirectActivity(this, StoreActivity.class);
    }

    public void ClickNewsFeed(View view){
        redirectActivity(this, UsersPostsActivity.class);
    }

    public void ClickHistory(View view){
        redirectActivity(this, HistoryActivity.class);
    }

    public void ClickAccountSettings(View view){
        redirectActivity(this, AccountSettingsActivity.class);
    }

    public void ClickSignOut(View view){
        signout(this);
    }

    public void signout(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Signing out");
        builder.setMessage("Are you sure?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            }
        });
        builder.setNegativeButton("NO!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static void logout(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Closing app");
        builder.setMessage("Are you sure?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finishAffinity();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static void redirectActivity(Activity activity, Class aClass) {
        Intent intent = new Intent(activity, aClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public void ClickBack(View view)
    {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
        closeDrawer(drawerLayout);
    }

    public void refresh() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }
}
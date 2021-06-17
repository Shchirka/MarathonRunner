package ua.kpi.comsys.androidrunner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ua.kpi.comsys.androidrunner.adapter.FriendsAdapter;
import ua.kpi.comsys.androidrunner.adapter.MarathonsAvailableAdapter;
import ua.kpi.comsys.androidrunner.adapter.StoreAdapter;
import ua.kpi.comsys.androidrunner.list.Friend;
import ua.kpi.comsys.androidrunner.list.Marathon;
import ua.kpi.comsys.androidrunner.models.User;
import ua.kpi.comsys.androidrunner.permission.MapsPermissionActivity;

import static ua.kpi.comsys.androidrunner.HomeActivity.logout;
import static ua.kpi.comsys.androidrunner.HomeActivity.redirectActivity;

public class StoreActivity extends AppCompatActivity {

    ArrayList<Marathon> marathonsInStore = new ArrayList<Marathon>();
    Map<String, Long> collectMarathons = new HashMap<>();
    List<String> collectAvailMarathons = new ArrayList<>();

    private DatabaseReference referenceUsers;
    private DatabaseReference referenceStore;
    private DatabaseReference referenceMarathons;

    ProgressDialog progressDialog;
    RecyclerView recyclerViewAvail;
    StoreAdapter storeAdapter;
    TextView pointsView;
    DrawerLayout drawerLayout;
    private ImageView refresh;

    private int points;
    private long sum = 0;
    private long fullSum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        recyclerViewAvail = (RecyclerView) findViewById(R.id.marathon_instore_list);
        drawerLayout = findViewById(R.id.android_runner_navigation);
        pointsView = findViewById(R.id.store_points);
        progressDialog = new ProgressDialog(StoreActivity.this);
        progressDialog.setTitle("Downloading marathons list...");
        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        referenceUsers = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        referenceStore = FirebaseDatabase.getInstance().getReference("Store");
        referenceMarathons = FirebaseDatabase.getInstance().getReference("UsersMarathons")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("available");

        progressDialog.show();
        referenceStore.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() == 0){
                    Log.d("TAG", "onCreate: store doesn't have any marathons yet");
                    try {
                        setInitialData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        collectMarathons.put(dataSnapshot.getKey(), (long)dataSnapshot.getValue());
                        fullSum += (long) dataSnapshot.getValue();
                    }
                    try {
                        setInitialData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setInitialData() throws IOException{
        if(collectMarathons.size() == 0){
            progressDialog.dismiss();
            Toast.makeText(this, "Store doesn't have any marathons yet", Toast.LENGTH_SHORT).show();
        }
        else{
            for (String tempMarathon : collectMarathons.keySet()) {
                if(tempMarathon.equals("id")){
                    continue;
                }
                setData(tempMarathon);
            }
        }
    }

    private void setData(String marathon){
        Log.d("TAG", "setInitialData: " + marathon);
        referenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);
                points = userProfile.points;
                pointsView.setText(Integer.toString(points));
                referenceMarathons.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String title = "";
                        if(snapshot.getChildrenCount() != 0){
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                if(dataSnapshot.getKey().equals(marathon)){
                                    title = marathon;
                                    break;
                                }
                            }
                            if(userProfile.points >= collectMarathons.get(marathon) - 10 && !marathon.equals(title)){
                                marathonsInStore.add(new Marathon(marathon, collectMarathons.get(marathon)));
                            }
                        }
                        else{
                            if(userProfile.points >= collectMarathons.get(marathon) - 10){
                                marathonsInStore.add(new Marathon(marathon, collectMarathons.get(marathon)));
                            }
                        }
                        sum += collectMarathons.get(marathon);
                        if(sum == fullSum){
                            progressDialog.dismiss();
                            storeAdapter = new StoreAdapter(StoreActivity.this, marathonsInStore);
                            recyclerViewAvail.setAdapter(storeAdapter);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
        redirectActivity(this, HomeActivity.class);
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

    public void ClickBack(View view)
    {
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
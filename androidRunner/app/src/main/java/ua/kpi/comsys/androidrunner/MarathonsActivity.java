package ua.kpi.comsys.androidrunner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ua.kpi.comsys.androidrunner.adapter.FriendsAdapter;
import ua.kpi.comsys.androidrunner.adapter.MarathonsAvailableAdapter;
import ua.kpi.comsys.androidrunner.adapter.MarathonsCompletedAdapter;
import ua.kpi.comsys.androidrunner.list.Friend;
import ua.kpi.comsys.androidrunner.list.Marathon;

public class MarathonsActivity extends AppCompatActivity {

    ArrayList<Marathon> marathonsAvail = new ArrayList<Marathon>();
    ArrayList<Marathon> marathonsCompl = new ArrayList<Marathon>();

    List<String> collectAvailMarathons = new ArrayList<>();
    List<String> collectComplMarathons = new ArrayList<>();

    private DatabaseReference referenceMarathons;
    private TabHost tabHost;
    private TabWidget tabs;

    ProgressDialog progressDialog;
    RecyclerView recyclerViewAvail;
    RecyclerView recyclerViewCompl;
    MarathonsAvailableAdapter marathonsAvailableAdapter;
    MarathonsCompletedAdapter marathonsCompletedAdapter;

    private ImageView refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marathons);

        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
        tabs = findViewById(android.R.id.tabs);

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("Available");
        tabSpec.setContent(R.id.available_page);
        tabSpec.setIndicator("Available");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("Completed");
        tabSpec.setContent(R.id.completed_page);
        tabSpec.setIndicator("Completed");
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(0);

        recyclerViewAvail = (RecyclerView) findViewById(R.id.marathon_avail_list);
        recyclerViewCompl = (RecyclerView) findViewById(R.id.marathon_compl_list);
        progressDialog = new ProgressDialog(MarathonsActivity.this);
        progressDialog.setTitle("Downloading marathons list...");
        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        referenceMarathons = FirebaseDatabase.getInstance().getReference("UsersMarathons")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        referenceMarathons.child("available").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.show();
                if(snapshot.getChildrenCount() == 0){
                    try {
                        setInitialDataAvail();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        collectAvailMarathons.add(dataSnapshot.getKey());
                    }
                    try {
                        setInitialDataAvail();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        referenceMarathons.child("completed").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.show();
                if(snapshot.getChildrenCount() == 0){
                    try {
                        setInitialDataCompl();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        collectComplMarathons.add(dataSnapshot.getKey());
                    }
                    try {
                        setInitialDataCompl();
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

    private void setInitialDataAvail() throws IOException{
        if(collectAvailMarathons.size() == 0){
            tabs.setBackground(getResources().getDrawable(R.drawable.ic_available_focused));
            progressDialog.dismiss();
            Toast.makeText(this, "You don't have any marathons yet", Toast.LENGTH_SHORT).show();
        }
        else{
            for (String tempID : collectAvailMarathons) {
                if(tempID.equals("id")){
                    continue;
                }
                tabs.setBackground(getResources().getDrawable(R.drawable.ic_available_focused));
                setAvailData(tempID);
            }
        }
    }

    private void setInitialDataCompl() throws IOException{
        if(collectComplMarathons.size() == 0){
            tabs.setBackground(getResources().getDrawable(R.drawable.ic_completed_focused));
            progressDialog.dismiss();
            Toast.makeText(this, "You didn't complete any marathons yet", Toast.LENGTH_SHORT).show();
        }
        else{
            for (String tempID : collectComplMarathons) {
                if(tempID.equals("id")){
                    continue;
                }
                tabs.setBackground(getResources().getDrawable(R.drawable.ic_completed_focused));
                setComplData(tempID);
            }
        }
    }

    private void setAvailData(String title){
        marathonsAvail.add(new Marathon(title));
        if(marathonsAvail.size() == collectAvailMarathons.size()){
            progressDialog.dismiss();
            marathonsAvailableAdapter = new MarathonsAvailableAdapter(this, marathonsAvail);
            recyclerViewAvail.setAdapter(marathonsAvailableAdapter);
        }
    }

    private void setComplData(String title){
        marathonsCompl.add(new Marathon(title));
        if(marathonsCompl.size() == collectComplMarathons.size()){
            progressDialog.dismiss();
            marathonsCompletedAdapter = new MarathonsCompletedAdapter(this, marathonsCompl);
            recyclerViewCompl.setAdapter(marathonsCompletedAdapter);
        }
    }

    public void ClickBack(View view)
    {
        super.onBackPressed();
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
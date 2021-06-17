package ua.kpi.comsys.androidrunner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ua.kpi.comsys.androidrunner.adapter.FriendsAdapter;
import ua.kpi.comsys.androidrunner.adapter.UsersAdapter;
import ua.kpi.comsys.androidrunner.list.Friend;
import ua.kpi.comsys.androidrunner.models.User;
import ua.kpi.comsys.androidrunner.permission.MapsPermissionActivity;

import static ua.kpi.comsys.androidrunner.UsersPostsActivity.getBitmapFromURL;

public class FriendsActivity extends AppCompatActivity {

    List<String> collectFriends = new ArrayList<>();
    List<String> allUsersWithFriends = new ArrayList<>();

    private DatabaseReference referenceUsers;
    private DatabaseReference referenceFriends;
    private StorageReference storageReference;
    private StorageReference storageSingleReference;

    ProgressDialog progressDialog;
    RecyclerView recyclerView;
    DrawerLayout drawerLayout;
    ArrayList<Friend> friends = new ArrayList<Friend>();
    FriendsAdapter friendsAdapter;
    SearchView searchView;
    ImageView addFriends;

    private ImageView refresh;
    private Bitmap userBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        recyclerView = (RecyclerView) findViewById(R.id.friends_list);
        drawerLayout = findViewById(R.id.android_runner_navigation);
        addFriends = findViewById(R.id.add_friends);
        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        progressDialog = new ProgressDialog(FriendsActivity.this);
        progressDialog.setTitle("Downloading users list...");
        storageReference = FirebaseStorage.getInstance().getReference().child("Users");
        referenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        referenceFriends = FirebaseDatabase.getInstance().getReference().child("Friends");

        addFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeActivity.redirectActivity(FriendsActivity.this, UsersActivity.class);
            }
        });

        progressDialog.show();
        referenceFriends.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        collectFriends = new ArrayList<>();
                        if(snapshot.getChildrenCount() == 0){
                            Log.d("TAG", "onCreate: user don't have any friends");
                            try {
                                setInitialData();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                collectFriends.add(String.valueOf(dataSnapshot.getKey()));
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
        searchView = findViewById(R.id.search_friends);
    }

    private void setInitialData() throws IOException{
        if(collectFriends.size() == 0){
            progressDialog.dismiss();
            Toast.makeText(this, "You don't have any followings", Toast.LENGTH_SHORT).show();
        }
        else{
            for (String tempID : collectFriends) {
                if(tempID.equals("id")){
                    continue;
                }
                setData(tempID);
            }
        }
    }

    private void setData(String id){
        Log.d("TAG", "setInitialData: " + id);
        referenceUsers.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User singleUser = snapshot.getValue(User.class);
                storageSingleReference = storageReference.child(id).child("accountPhotos").child("accountPhoto.jpg");
                storageSingleReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("TAG", "onSuccess: " + uri.toString());
                        try {
                            userBitmap = new AsyncRequest().execute(uri.toString()).get();
                            friends.add(new Friend(singleUser.username,
                                    singleUser.nickname, userBitmap));
                            if(friends.size() == collectFriends.size()) {
                                progressDialog.dismiss();
                                friendsAdapter = new FriendsAdapter(FriendsActivity.this, friends);
                                recyclerView.setAdapter(friendsAdapter);
                                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                    @Override
                                    public boolean onQueryTextSubmit(String query) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onQueryTextChange(String newText) {
                                        friendsAdapter.getFilter().filter(newText);
                                        return false;
                                    }
                                });
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    class AsyncRequest extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap temp = getBitmapFromURL(strings[0]);
            /*mapBitmap = getBitmapFromURL(userPost.imageURL);
            return mapBitmap;*/
            Log.d("TAG", "doInBackground: downloading image");
            return temp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null){
                userBitmap = bitmap;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
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
        recreate();
    }

    public void ClickSettings(View view){
        HomeActivity.redirectActivity(this, SettingsActivity.class);
    }

    public void ClickAboutUs(View view){
        HomeActivity.redirectActivity(this, AboutUsActivity.class);
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

    public void refresh() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }
}
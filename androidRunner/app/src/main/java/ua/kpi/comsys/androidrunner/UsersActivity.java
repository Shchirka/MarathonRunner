package ua.kpi.comsys.androidrunner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import static ua.kpi.comsys.androidrunner.UsersPostsActivity.getBitmapFromURL;

public class UsersActivity extends AppCompatActivity {

    ArrayList<Friend> possibleFriends = new ArrayList<Friend>();
    List<String> collectUsers = new ArrayList<>();

    private DatabaseReference referenceUsers;
    private StorageReference storageReference;
    private StorageReference storageSingleReference;

    ProgressDialog progressDialog;
    RecyclerView recyclerView;
    UsersAdapter usersAdapter;
    SearchView searchView;

    private ImageView refresh;
    private Bitmap userBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        recyclerView = (RecyclerView) findViewById(R.id.possible_friends_list);
        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        progressDialog = new ProgressDialog(UsersActivity.this);
        progressDialog.setTitle("Downloading users list...");

        storageReference = FirebaseStorage.getInstance().getReference().child("Users");
        referenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        progressDialog.show();
        referenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                collectUsers = new ArrayList<>();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    collectUsers.add(String.valueOf(dataSnapshot.getKey()));
                }
                try {
                    setInitialData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        searchView = findViewById(R.id.search_possible_friends);
    }

    private void setInitialData() throws IOException{
        for (String tempID : collectUsers) {
            if(tempID.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) || tempID.equals("id")){
                continue;
            }
            setData(tempID);
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
                            possibleFriends.add(new Friend(singleUser.username,
                                    singleUser.nickname, userBitmap));
                            if(possibleFriends.size() == collectUsers.size() - 2) {
                                progressDialog.dismiss();
                                usersAdapter = new UsersAdapter(UsersActivity.this, possibleFriends);
                                recyclerView.setAdapter(usersAdapter);
                                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                    @Override
                                    public boolean onQueryTextSubmit(String query) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onQueryTextChange(String newText) {
                                        usersAdapter.getFilter().filter(newText);
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

    public void refresh() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
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
}
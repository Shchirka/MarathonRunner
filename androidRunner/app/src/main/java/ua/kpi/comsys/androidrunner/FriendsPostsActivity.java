package ua.kpi.comsys.androidrunner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import ua.kpi.comsys.androidrunner.adapter.PostAdapter;
import ua.kpi.comsys.androidrunner.list.Post;
import ua.kpi.comsys.androidrunner.models.UploadPost;
import ua.kpi.comsys.androidrunner.models.User;

public class FriendsPostsActivity extends AppCompatActivity {

    private ArrayList<Post> posts = new ArrayList<Post>();

    List<String> collectPosts = new ArrayList<>();

    private DatabaseReference referencePost;
    private DatabaseReference referenceSinglePost;
    private DatabaseReference referenceUser;
    private StorageReference storageReference;
    private PostAdapter postAdapter;
    private RecyclerView recyclerView;

    private String usersNickname;

    private User userProfile;
    private Bitmap userBitmap;
    private Bitmap mapBitmap;
    private Bitmap tempBitmap;
    ProgressDialog progressDialog;
    private String friendsID;
    private String friendsPhotoLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_posts);

        friendsID = getIntent().getStringExtra("FRIEND_FEED_ID");
        friendsPhotoLink = getIntent().getStringExtra("FRIEND_PHOTO");

        progressDialog = new ProgressDialog(FriendsPostsActivity.this);
        progressDialog.setTitle("Downloading your friend's posts...");

        referencePost = FirebaseDatabase.getInstance().getReference("Posts").child(friendsID);
        referenceUser = FirebaseDatabase.getInstance().getReference("Users").child(friendsID);
        storageReference = FirebaseStorage.getInstance().getReference().child("Users").child(friendsID)
                .child("accountPhotos");
        progressDialog.show();
        referenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userProfile = snapshot.getValue(User.class);
                usersNickname = userProfile.nickname;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        referencePost.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                collectPosts = new ArrayList<>();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(snapshot.getChildrenCount() == 0){
                        break;
                    }
                    collectPosts.add(String.valueOf(dataSnapshot.getKey()));
                    Log.d("TAG", "onDataChange: " + String.valueOf(dataSnapshot.getKey()));
                }
                try {
                    setInitialData();
                } catch (IOException e) {
                    Log.d("TAG", "onCreate: post hasn't been created");
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.friends_posts_list);
    }

    private void setInitialData() throws IOException{
        if(collectPosts.size() == 0){
            progressDialog.dismiss();
            Toast.makeText(this, "Your friend don't have any posts yet", Toast.LENGTH_SHORT).show();
        }
        else{
            for(int i = collectPosts.size() - 1; i >= 0; i--){
                if(collectPosts.get(i).equals("id")){
                    continue;
                }
                setData(i);
            }
        }
    }

    private void setData(int index){
        referenceSinglePost = referencePost.child(collectPosts.get(index));
        referenceSinglePost.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UploadPost userPost = snapshot.getValue(UploadPost.class);
                try {
                    userBitmap = new AsyncRequest().execute(friendsPhotoLink).get();
                    mapBitmap = new AsyncRequest().execute(userPost.imageURL).get();
                    int minutes = (int)userPost.mTime/60;
                    int seconds = (int)userPost.mTime - minutes*60;
                    int milliseconds = (int)((userPost.mTime - (int)userPost.mTime)*1000);
                    String finalTime = "";
                    if(seconds < 10){
                        finalTime += minutes + " : 0" + seconds;
                    }
                    else{
                        finalTime += minutes + " : " + seconds;
                    }
                    if(milliseconds < 100){
                        if(milliseconds < 10){
                            finalTime += " : 00" + milliseconds;
                        }
                        else{
                            finalTime += " : 0" + milliseconds;
                        }
                    }
                    else{
                        finalTime += " : " + milliseconds;
                    }

                    posts.add(new Post(usersNickname, userBitmap, mapBitmap, userPost.imageName.substring(0, 10),
                            0, finalTime, userPost.mDistance));
                    if(posts.size() == collectPosts.size()){
                        progressDialog.dismiss();
                        postAdapter = new PostAdapter(FriendsPostsActivity.this, posts);
                        recyclerView.setAdapter(postAdapter);
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
                tempBitmap = bitmap;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoInput(true);
            //connection.setDoOutput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
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
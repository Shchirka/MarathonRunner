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

import ua.kpi.comsys.androidrunner.adapter.HistoryAdapter;
import ua.kpi.comsys.androidrunner.list.History;
import ua.kpi.comsys.androidrunner.models.UploadPost;
import ua.kpi.comsys.androidrunner.models.User;

public class HistoryActivity extends AppCompatActivity {

    private ArrayList<History> posts = new ArrayList<History>();
    List<String> collectHistory = new ArrayList<>();

    private FirebaseUser user;
    private DatabaseReference referencePost;
    private DatabaseReference referenceSinglePost;
    private HistoryAdapter historyAdapter;
    private RecyclerView recyclerView;

    private String userID;

    private Bitmap mapBitmap;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        progressDialog = new ProgressDialog(HistoryActivity.this);
        progressDialog.setTitle("Downloading your history...");

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();

        referencePost = FirebaseDatabase.getInstance().getReference("History").child(userID);
        progressDialog.show();

        referencePost.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                collectHistory = new ArrayList<>();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(snapshot.getChildrenCount() == 0){
                        break;
                    }
                    collectHistory.add(String.valueOf(dataSnapshot.getKey()));
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

        recyclerView = (RecyclerView) findViewById(R.id.user_history_list);
    }

    class AsyncRequest extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap temp = getBitmapFromURL(strings[0]);
            Log.d("TAG", "doInBackground: downloading image");
            return temp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null){
                mapBitmap = bitmap;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private void setInitialData() throws IOException {
        if(collectHistory.size() == 0){
            progressDialog.dismiss();
            Toast.makeText(this, "You don't have any runs yet", Toast.LENGTH_SHORT).show();
        }
        else{
            for(int i = collectHistory.size() - 1; i >= 0; i--){
                if(collectHistory.get(i).equals("id")){
                    continue;
                }
                setData(i);
            }
        }
    }

    private void setData(int index){
        referenceSinglePost = referencePost.child(collectHistory.get(index));
        referenceSinglePost.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UploadPost userPost = snapshot.getValue(UploadPost.class);
                try {
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

                    posts.add(new History(userPost.imageName.substring(0, 10), mapBitmap, userPost.mDistance , finalTime));
                    if(posts.size() == collectHistory.size()){
                        progressDialog.dismiss();
                        historyAdapter = new HistoryAdapter(HistoryActivity.this, posts);
                        recyclerView.setAdapter(historyAdapter);
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
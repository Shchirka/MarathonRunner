package ua.kpi.comsys.androidrunner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ua.kpi.comsys.androidrunner.models.User;

public class StatusActivity extends AppCompatActivity {

    private FirebaseUser user;
    private DatabaseReference reference;

    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        TextView numOfRuns = findViewById(R.id.num_of_runs);
        TextView fullTime = findViewById(R.id.full_time);
        TextView fullDistance = findViewById(R.id.full_distance);
        TextView pointsView = findViewById(R.id.points);
        TextView marathonsView = findViewById(R.id.completed_marathons);
        TextView nicknameStatus = findViewById(R.id.nickname_status);

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                if(userProfile != null){
                    String nickname = userProfile.nickname;
                    int runs = userProfile.runs;
                    int points = userProfile.points;
                    int completedMarathons = userProfile.completedMarathons;
                    double spendTime = userProfile.spendTime;
                    double distance = userProfile.distance;

                    int minutes = (int)spendTime/60;
                    int hours = (int) minutes/60;
                    int seconds = (int)spendTime - minutes*60;
                    int milliseconds = (int) ((spendTime - (int)spendTime)*1000);
                    String finalTime = Integer.toString(hours);
                    if(minutes < 10){
                        finalTime += " : 0" + minutes;
                    }
                    else{
                        finalTime += " : " + minutes;
                    }
                    if(seconds < 10){
                        finalTime += " : 0" + seconds;
                    }
                    else{
                        finalTime += " : " + seconds;
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

                    nicknameStatus.setText(nickname);
                    numOfRuns.setText(Integer.toString(runs));
                    fullTime.setText(finalTime);
                    fullDistance.setText(distance/1000.0 + "km");
                    pointsView.setText(Integer.toString(points));
                    marathonsView.setText(Integer.toString(completedMarathons));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StatusActivity.this, "Something wrong happened!",
                        Toast.LENGTH_LONG).show();
            }
        });
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
package ua.kpi.comsys.androidrunner.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import ua.kpi.comsys.androidrunner.AccountSettingsActivity;
import ua.kpi.comsys.androidrunner.R;
import ua.kpi.comsys.androidrunner.fragment.ChangeAccInfoFragment;
import ua.kpi.comsys.androidrunner.models.User;
import ua.kpi.comsys.androidrunner.permission.MapsPermissionActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private AppCompatButton signInGoogle;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    private StorageReference storageReference;
    private DatabaseReference referenceUsers;
    List<String> users = new ArrayList<>();

    private GoogleSignInClient mGoogleSignInClient;

    private final static int RC_SIGN_IN = 123;

    Toast toast;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            Intent intent = new Intent(getApplicationContext(), MapsPermissionActivity.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInGoogle = findViewById(R.id.google_sign_in_button);
        signInGoogle.setOnClickListener(this);

        Animation animationScaleBack = AnimationUtils.loadAnimation(this, R.anim.run_scale_back);
        animationScaleBack.setDuration(1000);
        animationScaleBack.setRepeatCount(1000);

        signInGoogle.setAnimation(animationScaleBack);

        referenceUsers = FirebaseDatabase.getInstance().getReference("Users");
        referenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    users.add(String.valueOf(dataSnapshot.getKey()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        storageReference = FirebaseStorage.getInstance().getReference();

        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        toast = new Toast(getApplicationContext());

        progressBar.setVisibility(View.VISIBLE);
        createRequest();
    }

    @Override
    public void onClick(View v) {
        signIn();
    }

    private void createRequest(){
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        progressBar.setVisibility(View.GONE);
    }

    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }
            catch (ApiException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w("error: ", e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            User createdUser = new User(user.getEmail(), "nickname", user.getDisplayName());
                            for(String userID : users){
                                if(user.getUid().equals(userID)){
                                    Intent intent = new Intent(LoginActivity.this, MapsPermissionActivity.class);
                                    startActivity(intent);
                                    return;
                                }
                            }
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(createdUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    if(task.isSuccessful()){
                                        toast.makeText(LoginActivity.this, "User has been created successfully",
                                                Toast.LENGTH_LONG).show();
                                        storageReference.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("accountPhotos").child("accountPhoto.jpg")
                                                .putFile(Uri.parse("android.resource://"
                                                        + getPackageName() + "/" + R.drawable.empty_photo));
                                        FirebaseDatabase.getInstance().getReference("Friends")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue("friends");
                                        FirebaseDatabase.getInstance().getReference("Posts")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue("posts");
                                        FirebaseDatabase.getInstance().getReference("History")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue("history");
                                        FirebaseDatabase.getInstance().getReference("Nicknames").child("nickname")
                                                .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        progressBar.setVisibility(View.GONE);
                                        //redirect to runs instead of login!!!!
                                        startActivity(new Intent(LoginActivity.this, AccountSettingsActivity.class));
                                    }
                                    else{
                                        toast.makeText(LoginActivity.this, "Sorry auth failed. Try again",
                                                Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            toast.makeText(LoginActivity.this, "Sorry auth failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
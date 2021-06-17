package ua.kpi.comsys.androidrunner.account;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import ua.kpi.comsys.androidrunner.R;
import ua.kpi.comsys.androidrunner.models.User;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener
{
    private FirebaseAuth mAuth;

    private ProgressBar progressBar;
    private EditText editNickname, editEmail, editPassword, editConfirmPassword, editName;
    private AppCompatButton registerUser;
    private StorageReference storageReference;
    private DatabaseReference referenceNicknames;

    List<String> nicknames = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        registerUser = findViewById(R.id.btn_register);
        registerUser.setOnClickListener(this);
        storageReference = FirebaseStorage.getInstance().getReference();
        referenceNicknames = FirebaseDatabase.getInstance().getReference("Nicknames");
        referenceNicknames.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    nicknames.add(String.valueOf(dataSnapshot.getKey()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        editEmail = findViewById(R.id.input_email);
        editPassword = findViewById(R.id.input_password);
        editConfirmPassword = findViewById(R.id.input_confirm_password);
        editName = findViewById(R.id.input_name);
        editNickname = findViewById(R.id.input_nickname);

        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_register:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();
        String name = editName.getText().toString().trim();
        String nickname = editNickname.getText().toString().trim();

        if(email.isEmpty()){
            editEmail.setError("Email is required!");
            editEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editEmail.setError("Please provide valid email!");
            editEmail.requestFocus();
            return;
        }
        if(name.isEmpty()){
            editName.setError("Your name is required!");
            editName.requestFocus();
            return;
        }
        if(nickname.isEmpty()){
            editNickname.setError("Nickname is required!");
            editNickname.requestFocus();
            return;
        }
        for(String nick : nicknames){
            if(nickname.equals(nick)){
                editNickname.setError("Nickname is used by another user");
                editNickname.requestFocus();
                return;
            }
        }
        if(password.isEmpty()){
            editPassword.setError("Password is required!");
            editPassword.requestFocus();
            return;
        }
        if(password.length() < 6){
            editPassword.setError("Minimum password length should be 6 characters!");
            editPassword.requestFocus();
            return;
        }
        if(confirmPassword.isEmpty()){
            editConfirmPassword.setError("Confirm your password please!");
            editConfirmPassword.requestFocus();
            return;
        }
        if(!confirmPassword.equals(password)){
            editConfirmPassword.setError("Please write the correct password!");
            editConfirmPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            User user = new User(email, nickname, name);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this, "User has been registered successfully",
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
                                        FirebaseDatabase.getInstance().getReference("UsersMarathons")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("available").setValue("none");
                                        FirebaseDatabase.getInstance().getReference("UsersMarathons")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("completed").setValue("none");
                                        referenceNicknames.child(nickname)
                                                .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        progressBar.setVisibility(View.GONE);
                                        //redirect to runs instead of login!!!!
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    }
                                    else{
                                        Toast.makeText(RegisterActivity.this, "Failed to register! Try again",
                                                Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(RegisterActivity.this, "Failed to register",
                                    Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}
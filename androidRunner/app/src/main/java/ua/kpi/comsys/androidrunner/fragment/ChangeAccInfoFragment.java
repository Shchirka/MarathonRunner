package ua.kpi.comsys.androidrunner.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ua.kpi.comsys.androidrunner.AccountSettingsActivity;
import ua.kpi.comsys.androidrunner.HomeActivity;
import ua.kpi.comsys.androidrunner.R;
import ua.kpi.comsys.androidrunner.models.User;

import static android.app.Activity.RESULT_OK;

public class ChangeAccInfoFragment extends Fragment {

    public static final int GALLERY_REQUEST = 1;

    List<String> nicknames = new ArrayList<>();

    private ProgressDialog progressDialog;
    private EditText editNickname, editName;
    private AppCompatButton confirm;
    private ImageButton editPhoto;
    private ImageView image;

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private StorageReference storageReference;
    private DatabaseReference referenceNicknames;

    private String userID;
    private Uri selectedImage;
    private String changeNickname;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = firebaseUser.getUid();

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

        editNickname = getView().findViewById(R.id.edit_nickname);
        editName = getView().findViewById(R.id.edit_name);
        editPhoto = getView().findViewById(R.id.edit_photo);
        confirm = getView().findViewById(R.id.confirm_account_changes);

        progressDialog = new ProgressDialog((AccountSettingsActivity)getActivity());

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                if(userProfile != null){
                    String name = userProfile.username;
                    String nickname = userProfile.nickname;
                    changeNickname = nickname;

                    editName.setHint(name);
                    editNickname.setHint(nickname);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText((AccountSettingsActivity)getActivity(), "Something wrong happened!",
                        Toast.LENGTH_LONG).show();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = editName.getText().toString().trim();
                String newNickname = editNickname.getText().toString().trim();
                if(!newName.equals("")){
                    reference.child(userID).child("username").setValue(newName);
                }
                if(!newNickname.equals("")){
                    for(String nick : nicknames){
                        if(newNickname.equals(nick)){
                            editNickname.setError("Nickname is used by another user");
                            editNickname.requestFocus();
                            return;
                        }
                        else{
                            reference.child(userID).child("nickname").setValue(newNickname);
                            referenceNicknames.child(newNickname).setValue(userID);
                            referenceNicknames.child(changeNickname).setValue(null);
                        }
                    }
                }
                if(image != null){
                    if(image.getResources() != null){
                        storageReference.child("Users").child(userID).child("accountPhotos").child("accountPhoto.jpg")
                                .putFile(selectedImage);
                    }
                }
                Fragment fragment = new AccountSettingsFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.hide(getActivity().getSupportFragmentManager().findFragmentById(R.id.fr_account_settings))
                        .add(R.id.fr_account_settings, fragment);
                fragmentTransaction.commit();
            }
        });

        editPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checkFilePermission(); ???
                Intent photoPickerIntent;
                image = new ImageView(getView().getContext());
                photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_acc_info, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;
        switch(requestCode) {
            case GALLERY_REQUEST:
                if(resultCode == RESULT_OK){
                    selectedImage = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                        image.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
    }
}
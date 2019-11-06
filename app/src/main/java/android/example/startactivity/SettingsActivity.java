package android.example.startactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button update;
    private EditText Username,UserProfileStatus;
    private CircleImageView circleImageView;

    private FirebaseAuth mauth;
    private String currentUserId;
    private DatabaseReference Rootref;
    public static final int GalleryPick =1;
    private ProgressDialog loadingBar;
    private Toolbar SettingsToolbar;

    private StorageReference UserProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        InitializeFields();

        loadingBar = new ProgressDialog(this);

        mauth=FirebaseAuth.getInstance();
        currentUserId=mauth.getCurrentUser().getUid();
        Rootref= FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery = new Intent();
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery,GalleryPick);
            }
        });
    }

    private void RetrieveUserInfo() {
        Rootref.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))){
                             String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                             String retrieveUserStatus=dataSnapshot.child("status").getValue().toString();
                             String retrieveProfileImage=dataSnapshot.child("image").getValue().toString();

                             Username.setText(retrieveUserName);
                             UserProfileStatus.setText(retrieveUserStatus);

                            Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.profile_image).into(circleImageView);

                        }else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            String retrieveUserStatus=dataSnapshot.child("status").getValue().toString();

                            Username.setText(retrieveUserName);
                            UserProfileStatus.setText(retrieveUserStatus);
                        }
                        else{
                            Toast.makeText(SettingsActivity.this, "Please set & update your profile information...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void InitializeFields() {
        update=findViewById(R.id.update_settings);
        Username=findViewById(R.id.set_username);
        UserProfileStatus=findViewById(R.id.set_profile_status);
        circleImageView=findViewById(R.id.profile_image);

        SettingsToolbar =findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
      }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait,while we are updating your image...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri =result.getUri();

                StorageReference filepath = UserProfileImageRef.child(currentUserId + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                       if(task.isSuccessful()){
                           Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully...", Toast.LENGTH_SHORT).show();

                           final String downloadedUri=task.getResult().getUploadSessionUri().toString();

                           Rootref.child("Users").child(currentUserId).child("image").setValue(downloadedUri)
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                         if(task.isSuccessful()){
                                             Toast.makeText(SettingsActivity.this, "Image saved in database successfully...", Toast.LENGTH_SHORT).show();
                                              loadingBar.dismiss();
                                         }
                                         else{
                                             Toast.makeText(SettingsActivity.this, "Error: "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                                             loadingBar.dismiss();
                                         }
                                       }
                                   });
                       }
                       else{
                           Toast.makeText(SettingsActivity.this, "Error: "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                       }
                    }
                });
            }
        }

    }

    private void UpdateSettings() {
        String setUsername = Username.getText().toString();
        String setStatus=UserProfileStatus.getText().toString();

        if(TextUtils.isEmpty(setUsername)){
            Toast.makeText(this, "Please enter the username first... ", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus)){
            Toast.makeText(this, "Please enter the profile Status", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setUsername);
            profileMap.put("status",setStatus);

            Rootref.child("Users").child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                          if(task.isSuccessful()){
                              SendUsersToMainActivity();
                              Toast.makeText(SettingsActivity.this, "Profile updated Successfully...", Toast.LENGTH_SHORT).show();
                          }
                          else{
                              Toast.makeText(SettingsActivity.this, "Error: "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                          }
                        }
                    });
        }

    }
    private void SendUsersToMainActivity() {
        Intent settingsintent=new Intent(SettingsActivity.this,MainActivity.class);
        settingsintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsintent);
        finish();
    }


}

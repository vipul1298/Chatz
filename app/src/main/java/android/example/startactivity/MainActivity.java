 package android.example.startactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


 public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabsAdapter tabsAdapter;
    private FirebaseAuth mauth;
    private FirebaseUser currentUser;
    private DatabaseReference RootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar=findViewById(R.id.main_page_toolbar);

        viewPager=findViewById(R.id.main_tabs_pager);
        tabsAdapter=new TabsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAdapter);

        tabLayout=findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);

        mauth=FirebaseAuth.getInstance();
        currentUser=mauth.getCurrentUser();
        RootRef= FirebaseDatabase.getInstance().getReference();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chatz");
    }


     private void SendUserToLoginActivity() {
         Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
         loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
         startActivity(loginIntent);
         finish();
     }

     private void SendUserToSettingsActivity(){
         Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
         startActivity(settingsIntent);
     }

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.options_menu,menu);
         return true;
     }

     @Override
     public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId()==R.id.logout_menu){
             mauth.signOut();
             SendUserToLoginActivity();
         }
         if(item.getItemId()==R.id.settings_menu){
              SendUserToSettingsActivity();
         }
         if(item.getItemId()==R.id.find_friends_menu){
             SendUserToFindFriendsActivity();
         }
         if(item.getItemId()==R.id.create_group_menu){
             RequestNewGroup();
         }
         return true;
     }

     private void SendUserToFindFriendsActivity() {
        startActivity(new Intent(MainActivity.this,FindFriendsActivity.class));
     }

     private void RequestNewGroup() {
         AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
         builder.setTitle("Enter Group Name:");

         final EditText groupNameField = new EditText(MainActivity.this);
         groupNameField.setHint("e.g. Coders");
         builder.setView(groupNameField);

         builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                String groupName =groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please write Group Name", Toast.LENGTH_SHORT).show();
                }
                else{
                     CreateNewGroup(groupName);
                }

             }
         });
         builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                   dialogInterface.cancel();
             }
         });
         builder.show();

     }

     private void CreateNewGroup(final String groupName) {
        RootRef.child("Group").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, groupName+" group is created successfully...", Toast.LENGTH_SHORT).show();
                    }
                });
     }

     @Override
     protected void onStart() {
         super.onStart();
         if(currentUser==null){
             SendUserToLoginActivity();
         }
         else{
             VerifyUserExistence();
         }
     }

     private void VerifyUserExistence() {
          String currentUserId=mauth.getCurrentUser().getUid();

          RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   if((dataSnapshot.child("name").exists())){
                       Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                   }
                   else{
                       SendUserToSettingsActivity();
                   }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError) {

              }
          });
     }
 }

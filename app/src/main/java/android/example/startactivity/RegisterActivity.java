package android.example.startactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

     private FirebaseUser currentUser;
     private Button registerbtn;
     private EditText UserEmail,UserPassword;
     private TextView AlreadyHaveAcc;

     private FirebaseAuth mauth;
     private DatabaseReference RootRef;
     private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mauth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        AlreadyHaveAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToLoginActivity();
            }
        });

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter the email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter the password", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait,while we are making an account...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

             mauth.createUserWithEmailAndPassword(email,password)
                     .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                         @Override
                         public void onComplete(@NonNull Task<AuthResult> task) {
                           if(task.isSuccessful()){
                               String currentUserId=mauth.getCurrentUser().getUid();
                               RootRef.child("Users").child(currentUserId).setValue("");
                               SendUserToMainActivity();
                               Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                               loadingBar.dismiss();
                           }
                           else{
                               Toast.makeText(RegisterActivity.this, "Error: "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                             loadingBar.dismiss();
                           }
                         }
                     });
        }
    }
    private void SendUserToMainActivity() {
        Intent mainintent=new Intent(RegisterActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent loginintent=new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginintent);
    }

    private void InitializeFields() {
        loadingBar=new ProgressDialog(this);
        registerbtn=findViewById(R.id.reg_btn);
        UserEmail=findViewById(R.id.reg_email);
        UserPassword=findViewById(R.id.reg_password);
        AlreadyHaveAcc=findViewById(R.id.already_have_an_acc);
    }


}

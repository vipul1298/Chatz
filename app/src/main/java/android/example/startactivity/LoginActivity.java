package android.example.startactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class LoginActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private Button LoginBtn,Phonebtn;
    private EditText UserEmail,UserPassword;
    private TextView ForgetLink,NeedNewAcc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    InitializeFields();

    mAuth=FirebaseAuth.getInstance();
    currentUser=mAuth.getCurrentUser();

    NeedNewAcc.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SendUserToRegisterActivity();
        }
    });

    LoginBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AllowUserToLogin();
        }
    });

    Phonebtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(LoginActivity.this,PhoneLoginActivity.class));
        }
    });
    }

    private void AllowUserToLogin() {
        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter the email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter the password", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();


            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                           if(task.isSuccessful()){

//                               String currentUserId = mAuth.getCurrentUser().getUid();
//                               String deviceToken =FirebaseInstanceId.getInstance().getInstanceId();

                               SendUserToMainActivity();
                               Toast.makeText(LoginActivity.this, "Logged in Successfully..", Toast.LENGTH_SHORT).show();
                               loadingBar.dismiss();
                           }
                           else{
                               Toast.makeText(LoginActivity.this, "Error: "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                               loadingBar.dismiss();
                           }
                        }
                    });
        }
    }

    private void InitializeFields() {
        LoginBtn=findViewById(R.id.login_btn);
        Phonebtn=findViewById(R.id.login_phone);
        UserEmail=findViewById(R.id.login_email);
        UserPassword=findViewById(R.id.login_password);
        ForgetLink=findViewById(R.id.forget_password);
        NeedNewAcc=findViewById(R.id.new_register);
        loadingBar=new ProgressDialog(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser!=null){
            SendUserToMainActivity();
        }
    }


    private void SendUserToMainActivity() {
        Intent mainintent=new Intent(LoginActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }
    private void SendUserToRegisterActivity() {
        Intent regIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(regIntent);
    }

}

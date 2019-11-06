package android.example.startactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId,senderUserId,Current_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button SendMessageRequestBtn,DeclineMessageRequestBtn;

    private DatabaseReference UserRef,ChatRequestRef,ContactsRef,NotificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");
        mAuth=FirebaseAuth.getInstance();


        receiverUserId=getIntent().getStringExtra("visit_user_id");
        senderUserId=mAuth.getCurrentUser().getUid();

        Toast.makeText(this, "USer ID: "+receiverUserId, Toast.LENGTH_SHORT).show();
         userProfileImage=findViewById(R.id.visit_image);
         userProfileName=findViewById(R.id.visit_user_name);
         userProfileStatus=findViewById(R.id.visit_profile_status);
         SendMessageRequestBtn=findViewById(R.id.send_message_request);
         DeclineMessageRequestBtn=findViewById(R.id.decline_message_request);
         Current_State ="new";

         RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {

        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))){
                     String userImage = dataSnapshot.child("image").getValue().toString();
                     String userName = dataSnapshot.child("name").getValue().toString();
                     String userStatus = dataSnapshot.child("status").getValue().toString();

                     Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                     userProfileName.setText(userName);
                     userProfileStatus.setText(userStatus);

                     ManageChatRequest();
                 }
                 else{
                     String userName = dataSnapshot.child("name").getValue().toString();
                     String userStatus = dataSnapshot.child("status").getValue().toString();

                     userProfileName.setText(userName);
                     userProfileStatus.setText(userStatus);

                     ManageChatRequest();
                 }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void ManageChatRequest() {
         ChatRequestRef.child(senderUserId)
                 .addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiverUserId)){
                            String request_type=dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                            if(request_type.equals("sent")){
                                Current_State = "request_sent";
                                SendMessageRequestBtn.setText("Cancel Chat Request");
                            }
                            else if(request_type.equals("received")){
                                Current_State ="request_received";
                                SendMessageRequestBtn.setText("Accept Chat request");

                                DeclineMessageRequestBtn.setVisibility(View.VISIBLE);
                                DeclineMessageRequestBtn.setEnabled(true);

                                DeclineMessageRequestBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }
                        else{
                            ContactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(receiverUserId)){
                                                Current_State ="friends";
                                                SendMessageRequestBtn.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {

                     }
                 });

         if(!senderUserId.equals(receiverUserId)){
             SendMessageRequestBtn.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     SendMessageRequestBtn.setEnabled(false);

                     if(Current_State.equals("new")){
                          SendChatRequest();
                     }
                     if(Current_State.equals("request_sent")){
                         CancelChatRequest();
                     }
                     if(Current_State.equals("request_received")){
                         AcceptChatRequest();
                     }
                     if(Current_State.equals("friends")){
                         RemoveSpecificContact();
                     }
                 }
             });
         }
         else{
             SendMessageRequestBtn.setVisibility(View.INVISIBLE);
         }
    }

    private void RemoveSpecificContact() {
        ContactsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ContactsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendMessageRequestBtn.setEnabled(true);
                                                Current_State="new";
                                                SendMessageRequestBtn.setText("Send Message");

                                                DeclineMessageRequestBtn.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void AcceptChatRequest() {
        ContactsRef.child(senderUserId).child(receiverUserId)
                .child("contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ContactsRef.child(receiverUserId).child(senderUserId)
                                    .child("contacts").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                              ChatRequestRef.child(senderUserId).child(receiverUserId)
                                                      .removeValue()
                                                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                          @Override
                                                          public void onComplete(@NonNull Task<Void> task) {
                                                             if(task.isSuccessful()){
                                                                 ChatRequestRef.child(receiverUserId).child(senderUserId)
                                                                         .removeValue()
                                                                         .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                             @Override
                                                                             public void onComplete(@NonNull Task<Void> task) {
                                                                                SendMessageRequestBtn.setEnabled(true);
                                                                                Current_State="friends";
                                                                                SendMessageRequestBtn.setText("Remove this Contact");

                                                                                DeclineMessageRequestBtn.setVisibility(View.INVISIBLE);
                                                                                DeclineMessageRequestBtn.setEnabled(false);
                                                                             }
                                                                         });
                                                             }
                                                          }
                                                      });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void SendChatRequest() {
        ChatRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                      if(task.isSuccessful()){

                          ChatRequestRef.child(receiverUserId).child(senderUserId)
                                  .child("request_type").setValue("received")
                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(@NonNull Task<Void> task) {
                                          if(task.isSuccessful()){
//                                              HashMap<String,String> chatNotification = new HashMap<>();
//                                              chatNotification.put("from",senderUserId);
//                                              chatNotification.put("type","request");
//
//                                              NotificationRef.child(receiverUserId).push()
//                                                      .setValue(chatNotification)
//                                                      .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                          @Override
//                                                          public void onComplete(@NonNull Task<Void> task) {
//                                                            if(task.isSuccessful()){
//                                                          }
//                                                      });

                                              SendMessageRequestBtn.setEnabled(true);
                                              Current_State="request_sent";
                                              SendMessageRequestBtn.setText("Cancel Chat Request");

                                          }
                                      }
                                  });
                      }
                    }
                });
    }

    private void CancelChatRequest() {
       ChatRequestRef.child(senderUserId).child(receiverUserId)
               .removeValue()
               .addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                       if(task.isSuccessful()){
                           ChatRequestRef.child(receiverUserId).child(senderUserId)
                                   .removeValue()
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                          if(task.isSuccessful()){
                                              SendMessageRequestBtn.setEnabled(true);
                                              Current_State="new";
                                              SendMessageRequestBtn.setText("Send Message");

                                              DeclineMessageRequestBtn.setVisibility(View.INVISIBLE);
                                              DeclineMessageRequestBtn.setEnabled(false);
                                          }
                                       }
                                   });
                       }
                   }
               });
    }


}

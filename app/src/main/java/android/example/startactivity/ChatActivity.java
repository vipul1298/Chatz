package android.example.startactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId,messageReceiverName,messageReceiverImage,messageSenderId;
    private TextView userName,userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ImageButton SendMessageBtn;
    private EditText MessageInputText;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private RecyclerView userMessagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverId=getIntent().getStringExtra("visit_user_id");
        messageReceiverName=getIntent().getStringExtra("visit_user_name");
        messageReceiverImage=getIntent().getStringExtra("visit_image");

        InitializeControllers();
         mAuth =FirebaseAuth.getInstance();
         messageSenderId=mAuth.getCurrentUser().getUid();
         RootRef= FirebaseDatabase.getInstance().getReference();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

         SendMessageBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 SendMessage();
             }
         });
    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                       Messages messages = dataSnapshot.getValue(Messages.class);
                       messagesList.add(messages);
                       messagesAdapter.notifyDataSetChanged();

                       userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void InitializeControllers() {

        ChatToolbar =findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView =layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        userImage=findViewById(R.id.custom_profile_image);
        userName=findViewById(R.id.custom_profile_name);
        userLastSeen=findViewById(R.id.custom_user_last_seen);

        SendMessageBtn=findViewById(R.id.send_message_btn);
        MessageInputText=findViewById(R.id.input_message);

        messagesAdapter=new MessagesAdapter(messagesList);
        userMessagesList=findViewById(R.id.chat_recycler);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);
    }


    private void SendMessage() {
       final String message = MessageInputText.getText().toString();
       
       if (TextUtils.isEmpty(message)){
           Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
       }
       else{
           String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
           String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

           DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                   .push();

           String messagePushID = userMessageKeyRef.getKey();

           Map messageTextBody = new HashMap();
           messageTextBody.put("message",message);
           messageTextBody.put("type","text");
           messageTextBody.put("from",messageSenderId);

           Map messageBodyDetails = new HashMap();
           messageBodyDetails.put(messageSenderRef + "/" + messagePushID ,messageTextBody);
           messageBodyDetails.put(messageReceiverRef + "/" + messagePushID ,messageTextBody);

           RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
               @Override
               public void onComplete(@NonNull Task task) {
                  if(task.isSuccessful()){
                      Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                  }
                  else
                  {
                      Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                  }
                  MessageInputText.setText("");

               }
           });
       }
    }
}

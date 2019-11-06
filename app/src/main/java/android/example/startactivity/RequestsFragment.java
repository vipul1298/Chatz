package android.example.startactivity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private View RequestsFragmentView;
    private RecyclerView myRequestsList;
    private DatabaseReference ChatRequestRef,UsersRef,ContactsRef;
    private FirebaseAuth mAuth;
    private String CurrentUserId;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragmentView=inflater.inflate(R.layout.fragment_requests, container, false);

        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");

        mAuth=FirebaseAuth.getInstance();
        CurrentUserId =mAuth.getCurrentUser().getUid();

        myRequestsList=RequestsFragmentView.findViewById(R.id.request_recycler);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));



         return RequestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestRef.child(CurrentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter
                =new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder requestViewHolder, int i, @NonNull Contacts contacts) {
                requestViewHolder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                requestViewHolder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                final String list_user_id = getRef(i).getKey();

                DatabaseReference getTypeRef = getRef(i).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){
                            String type = dataSnapshot.getValue().toString();

                            if(type.equals("received")){
                                 UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                     @Override
                                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                         if(dataSnapshot.hasChild("image")){

                                             final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                             Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(requestViewHolder.profileImage);
                                         }

                                             final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                             final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                             requestViewHolder.userName.setText(requestUserName);
                                             requestViewHolder.userStatus.setText(requestUserStatus);

                                             requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View view) {
                                                     CharSequence options[] = new CharSequence[]
                                                             {
                                                                     "Accept",
                                                                     "Cancel"
                                                             };

                                                     AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                     builder.setTitle(requestUserName +"  Chat Request");

                                                     builder.setItems(options, new DialogInterface.OnClickListener() {
                                                         @Override
                                                         public void onClick(DialogInterface dialogInterface, int i) {
                                                             if(i==0){
                                                                 ContactsRef.child(CurrentUserId).child(list_user_id).child("contacts")
                                                                         .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                     @Override
                                                                     public void onComplete(@NonNull Task<Void> task) {
                                                                         if(task.isSuccessful()){
                                                                             ContactsRef.child(list_user_id).child(CurrentUserId).child("contacts")
                                                                                     .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                 @Override
                                                                                 public void onComplete(@NonNull Task<Void> task) {
                                                                                     if(task.isSuccessful()){

                                                                                         ChatRequestRef.child(CurrentUserId).child(list_user_id)
                                                                                                 .removeValue()
                                                                                                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                     @Override
                                                                                                     public void onComplete(@NonNull Task<Void> task) {
                                                                                                         if(task.isSuccessful()){
                                                                                                             ChatRequestRef.child(list_user_id).child(CurrentUserId)
                                                                                                                     .removeValue()
                                                                                                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                         @Override
                                                                                                                         public void onComplete(@NonNull Task<Void> task) {
                                                                                                                             if(task.isSuccessful()){
                                                                                                                                 Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show();
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
                                                                     }
                                                                 });
                                                             }
                                                             if(i==1){
                                                                 ChatRequestRef.child(CurrentUserId).child(list_user_id)
                                                                         .removeValue()
                                                                         .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                             @Override
                                                                             public void onComplete(@NonNull Task<Void> task) {
                                                                                 if(task.isSuccessful()){
                                                                                     ChatRequestRef.child(list_user_id).child(CurrentUserId)
                                                                                             .removeValue()
                                                                                             .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                 @Override
                                                                                                 public void onComplete(@NonNull Task<Void> task) {
                                                                                                     if(task.isSuccessful()){
                                                                                                         Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();
                                                                                                     }

                                                                                                 }
                                                                                             });
                                                                                 }

                                                                             }
                                                                         });

                                                             }
                                                         }
                                                     });
                                                  builder.show();
                                                 }

                                             });

                                     }

                                     @Override
                                     public void onCancelled(@NonNull DatabaseError databaseError) {

                                     }
                                 });
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                 View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display,parent,false);
                 RequestViewHolder requestViewHolder = new RequestViewHolder(view);
                 return requestViewHolder;
            }
        };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;
        Button accept_btn,cancel_btn;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            accept_btn=itemView.findViewById(R.id.request_accept_btn);
            cancel_btn=itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}

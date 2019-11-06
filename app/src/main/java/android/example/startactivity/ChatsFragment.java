package android.example.startactivity;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
public class ChatsFragment extends Fragment {

    private View PrivateChatView;
    private RecyclerView ChatsList;
    private DatabaseReference ChatsRef,UsersRef;
    private FirebaseAuth mAuth;
    private String CurrentUserID;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatView=inflater.inflate(R.layout.fragment_chats, container, false);

        ChatsList = PrivateChatView.findViewById(R.id.chat_fragment_recycler);
        ChatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth=FirebaseAuth.getInstance();
        CurrentUserID=mAuth.getCurrentUser().getUid();
        ChatsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(CurrentUserID);
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");


      return PrivateChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder chatsViewHolder, int i, @NonNull Contacts contacts) {
                        final String userId =getRef(i).getKey();
                        final String[] retImage = {"default_image"};

                        UsersRef.child(userId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                               if(dataSnapshot.exists()){
                                   if(dataSnapshot.hasChild("image")){
                                       retImage[0] =dataSnapshot.child("image").getValue().toString();
                                       Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(chatsViewHolder.profileImage);
                                   }

                                   final String retName =dataSnapshot.child("name").getValue().toString();
                                   final String retStatus =dataSnapshot.child("status").getValue().toString();

                                   chatsViewHolder.userName.setText(retName);
                                   chatsViewHolder.userStatus.setText("Last Seen: " + "\n" + "Date:  "+ "Time: ");


                                   chatsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View view) {
                                           Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                           chatIntent.putExtra("visit_user_id",userId);
                                           chatIntent.putExtra("visit_user_name",retName);
                                           chatIntent.putExtra("visit_image", retImage[0]);
                                           startActivity(chatIntent);
                                       }
                                   });
                               }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display,parent,false);
                        ChatsViewHolder chatsViewHolder =new ChatsViewHolder(view);
                        return chatsViewHolder;
                    }
                };

        ChatsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{
         CircleImageView profileImage;
         TextView userName,userStatus;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage=itemView.findViewById(R.id.users_profile_image);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
        }
    }
}

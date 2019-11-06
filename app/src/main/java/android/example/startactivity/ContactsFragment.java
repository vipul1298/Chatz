package android.example.startactivity;


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
public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView myContactsList;
    private DatabaseReference ContactRef,UsersRef;
    private FirebaseAuth mAuth;
    private String CurrentUserId;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView=inflater.inflate(R.layout.fragment_contacts, container, false);

        mAuth=FirebaseAuth.getInstance();
        CurrentUserId=mAuth.getCurrentUser().getUid();

        ContactRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(CurrentUserId);
        UsersRef =FirebaseDatabase.getInstance().getReference().child("Users");

         myContactsList=ContactsView.findViewById(R.id.contacts_list);
         myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));
         return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter =
              new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
                  @Override
                  protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, int i, @NonNull Contacts contacts) {
                     String userIds =getRef(i).getKey();

                     UsersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("image")){
                                String proImage =dataSnapshot.child("image").getValue().toString();
                                String profileName =dataSnapshot.child("name").getValue().toString();
                                String profileStatus =dataSnapshot.child("status").getValue().toString();

                                contactsViewHolder.userName.setText(profileName);
                                contactsViewHolder.userStatus.setText(profileStatus);
                                Picasso.get().load(proImage).placeholder(R.drawable.profile_image).into(contactsViewHolder.profileImage);

                            }
                            else{
                                String profileName =dataSnapshot.child("name").getValue().toString();
                                String profileStatus =dataSnapshot.child("status").getValue().toString();

                                contactsViewHolder.userName.setText(profileName);
                                contactsViewHolder.userStatus.setText(profileStatus);
                            }
                         }

                         @Override
                         public void onCancelled(@NonNull DatabaseError databaseError) {

                         }
                     });
                  }

                  @NonNull
                  @Override
                  public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                      View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display,parent,false);
                      ContactsViewHolder contactsViewHolder = new ContactsViewHolder(view);
                      return contactsViewHolder;
                  }
              };
        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{
         TextView userName,userStatus;
         CircleImageView profileImage;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
        }
    }
}

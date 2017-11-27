package fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.ServerValue;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.sql.Ref;

import de.hdodenhof.circleimageview.CircleImageView;
import model.Friends;
import musictag.hytham1.com.mychat.ChatActivity;
import musictag.hytham1.com.mychat.ProfileActivity;
import musictag.hytham1.com.mychat.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView myFriendsList;
    private DatabaseReference FriendsReference;
    private DatabaseReference usersReference;
    private FirebaseAuth mAuth;
    String onLine_user_id;
    private View myMainView;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myMainView = inflater.inflate(R.layout.fragment_friends , container , false);
        myFriendsList = myMainView.findViewById(R.id.friends_list);
        mAuth  = FirebaseAuth.getInstance();
        onLine_user_id = mAuth.getCurrentUser().getUid();

        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(onLine_user_id);
        FriendsReference.keepSynced(true);

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        usersReference.keepSynced(true);

        myFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return myMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friends , FriendsViewHolder> FBRA = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class, R.layout.all_users_display_out , FriendsViewHolder.class , FriendsReference
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                viewHolder.setDate(model.getDate());

                final String list_user_Id = getRef(position).getKey();
                usersReference.child(list_user_Id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        String image = dataSnapshot.child("user_thumb_image").getValue().toString();

                        viewHolder.setUserName(userName);
                        viewHolder.setThumbImage(getContext(),image);





                        if (dataSnapshot.hasChild("online"))
                        {
                            String online_status = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnLine(online_status);
                        }

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options []  = new CharSequence[]
                                        {
                                                userName + "'s Profile",
                                                "Send Message"

                                        };
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int position) {

                                        if (position == 0){
                                            Intent profileIntent = new Intent(getContext() , ProfileActivity.class);
                                            profileIntent.putExtra("visit_user_id" , list_user_Id);
                                            startActivity(profileIntent);
                                        }
                                        if (position == 1){
                                            if (dataSnapshot.child("online").exists())
                                            {
                                                Intent chatIntent = new Intent(getContext() , ChatActivity.class);
                                                chatIntent.putExtra("visit_user_id" , list_user_Id);
                                                chatIntent.putExtra("user_name" , userName);
                                                startActivity(chatIntent);
                                            }
                                            else
                                            {
                                                usersReference.child(list_user_Id).child("online").
                                                        setValue(ServerValue.TIMESTAMP)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid)
                                                    {
                                                        Intent chatIntent = new Intent(getContext() , ChatActivity.class);
                                                        chatIntent.putExtra("visit_user_id" , list_user_Id);
                                                        chatIntent.putExtra("user_name" , userName);
                                                        startActivity(chatIntent);
                                                    }
                                                });
                                            }
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        myFriendsList.setHasFixedSize(true);
        myFriendsList.setAdapter(FBRA);


    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate (String date){
            TextView sinceFriendDate = mView.findViewById(R.id.all_users_status);
            sinceFriendDate.setText("Friends Since : \n" + date);
        }
        public void setUserName (String name){
            TextView userNameText = mView.findViewById(R.id.all_users_username);
            userNameText.setText(name);
        }
        public void setThumbImage (final Context context , final String user_thumb_image){
            final CircleImageView circleImageViewThumb = mView.findViewById(R.id.all_users_profile_image);

            Picasso.with(context).load(user_thumb_image).
                    networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image)
                    .into(circleImageViewThumb );

            Picasso.with(context).load(user_thumb_image)
                    .placeholder(R.drawable.default_image)
                    .into(circleImageViewThumb, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(context).load(user_thumb_image).
                                    networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image)
                                    .into(circleImageViewThumb );
                        }
                    });
        }

        public void setUserOnLine(String online_status) {
            ImageView imageView = mView.findViewById(R.id.online_status);

            if (online_status.equals("true"))
            {
              imageView.setVisibility(View.VISIBLE);
            } else
                {
                    imageView.setVisibility(View.INVISIBLE);

                }
        }
    }

}

package fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import de.hdodenhof.circleimageview.CircleImageView;
import model.Chats;
import model.Friends;
import musictag.hytham1.com.mychat.ChatActivity;
import musictag.hytham1.com.mychat.ProfileActivity;
import musictag.hytham1.com.mychat.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment
{
    private RecyclerView myChatList;
    private View myMainView;
    private DatabaseReference FriendsReference ;
    private DatabaseReference UsersReference;
    private FirebaseAuth mAuth;


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        myMainView = inflater.inflate(R.layout.fragment_chat , container , false);

        myChatList = myMainView.findViewById(R.id.chats_list);

        mAuth = FirebaseAuth.getInstance();
        String online_user_id = mAuth.getCurrentUser().getUid();

        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);

        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        myChatList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        myChatList.setLayoutManager(linearLayoutManager);


        return myMainView ;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Chats, ChatFragment.ChatsViewHolder> FBRA = new FirebaseRecyclerAdapter<Chats, ChatFragment.ChatsViewHolder>(
                Chats.class, R.layout.all_users_display_out , ChatFragment.ChatsViewHolder.class , FriendsReference
        ) {
            @Override
            protected void populateViewHolder(final ChatFragment.ChatsViewHolder viewHolder, Chats model, int position) {


                final String list_user_Id = getRef(position).getKey();
                UsersReference.child(list_user_Id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        String image = dataSnapshot.child("user_thumb_image").getValue().toString();

                        String userStatus = dataSnapshot.child("user_status").getValue().toString();


                        if (dataSnapshot.hasChild("online"))
                        {
                            String online_status = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnLine(online_status);
                        }

                        viewHolder.setUserName(userName);
                        viewHolder.setThumbImage(getContext(),image);
                        viewHolder.setUserStatus(userStatus);






                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (dataSnapshot.child("online").exists())
                                {
                                    Intent chatIntent = new Intent(getContext() , ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id" , list_user_Id);
                                    chatIntent.putExtra("user_name" , userName);
                                    startActivity(chatIntent);
                                }
                                else
                                {
                                    UsersReference.child(list_user_Id).child("online").
                                            setValue(ServerValue.TIMESTAMP)
                                            .addOnSuccessListener(new OnSuccessListener<Void>()
                                            {
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
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        myChatList.setAdapter(FBRA);
        myChatList.setHasFixedSize(true);

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public ChatsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
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

        public void setUserStatus(String userStatus) {
            TextView user_status = mView.findViewById(R.id.all_users_status);
            user_status.setText(userStatus);
        }
    }

}

package fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.ServerValue;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import model.Requests;
import musictag.hytham1.com.mychat.ChatActivity;
import musictag.hytham1.com.mychat.ProfileActivity;
import musictag.hytham1.com.mychat.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment
{
    private RecyclerView myRequestsList;
    private View myMainView;
    private DatabaseReference friendRequestReference;
    private DatabaseReference userReference;
    private FirebaseAuth mAuth;
    private DatabaseReference FriendsDatabaseRef;
    private DatabaseReference FriendsReqDatabaseRef;

   private String user_online ;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myMainView = inflater.inflate(R.layout.fragment_requests , container , false);
        // Inflate the layout for this fragment

        myRequestsList = myMainView.findViewById(R.id.requests_list);

        mAuth = FirebaseAuth.getInstance();
         user_online = mAuth.getCurrentUser().getUid();

        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Request").child(user_online);
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");

        FriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsReqDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friend_Request");

        myRequestsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        myRequestsList.setLayoutManager(linearLayoutManager);
        return myMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Requests , RequestViewHolder> FBRA = new FirebaseRecyclerAdapter<Requests, RequestViewHolder>
                (
                        Requests.class ,
                        R.layout.friend_request_all_users_layout,
                        RequestViewHolder.class,
                        friendRequestReference

                ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Requests model, int position)
            {
                final String list_users_id = getRef(position).getKey();
                DatabaseReference get_type_ref = getRef(position).child("request_type").getRef();

                get_type_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            String request_type = dataSnapshot.getValue().toString();

                            if (request_type.equals("received"))
                            {

                                userReference.child(list_users_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {
                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        String image = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        String userStatus = dataSnapshot.child("user_status").getValue().toString();

                                        viewHolder.setUserName(userName);
                                        viewHolder.setThumbImage(getContext() , image);
                                        viewHolder.setUserStatus(userStatus);

                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                CharSequence options []  = new CharSequence[]
                                                        {
                                                                "Accept Friend Request",
                                                                "Cancel Friend Request"

                                                        };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Friend Req Options");
                                                builder.setItems(options , new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int position) {

                                                        if (position == 0)
                                                        {
                                                            Calendar calendar = Calendar.getInstance();
                                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
                                                            final String saveDate = simpleDateFormat.format(calendar.getTime());

                                                            FriendsDatabaseRef.child(user_online).child(list_users_id).child("date").setValue(saveDate)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            FriendsDatabaseRef.child(list_users_id).child(user_online).child("date").setValue(saveDate)
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {

                                                                                            FriendsReqDatabaseRef.child(user_online).child(list_users_id).removeValue().
                                                                                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                FriendsReqDatabaseRef.child(list_users_id).child(user_online).removeValue().
                                                                                                                        addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                if (task.isSuccessful()) {
                                                                                                                                    Toast.makeText(getContext(), "Friend Request Accepted Successfully", Toast.LENGTH_SHORT).show();


                                                                                                                                }

                                                                                                                            }
                                                                                                                        });

                                                                                                            }

                                                                                                        }
                                                                                                    });

                                                                                        }
                                                                                    });
                                                                        }
                                                                    });
                                                        }
                                                        if (position == 1)
                                                        {
                                                            FriendsReqDatabaseRef.child(user_online).child(list_users_id).removeValue().
                                                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                FriendsReqDatabaseRef.child(list_users_id).child(user_online).removeValue().
                                                                                        addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    Toast.makeText(getContext(), "Friend Request Cancelled Successfully.!", Toast.LENGTH_SHORT).show();
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
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else if (request_type.equals("sent"))
                            {
                                Button req_sent_btn = viewHolder.mView.findViewById(R.id.request_accept_btn);
                                req_sent_btn.setText("Req Sent");

                                viewHolder.mView.findViewById(R.id.request_decline_btn).setVisibility(View.INVISIBLE);

                                userReference.child(list_users_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {
                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        String image = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        String userStatus = dataSnapshot.child("user_status").getValue().toString();

                                        viewHolder.setUserName(userName);
                                        viewHolder.setThumbImage(getContext() , image);
                                        viewHolder.setUserStatus(userStatus);

                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                CharSequence options []  = new CharSequence[]
                                                        {
                                                                "Cancel Friend Request" ,

                                                        };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Friend Req Sent");
                                                builder.setItems(options , new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int position) {

                                                        if (position == 0)
                                                        {
                                                            FriendsReqDatabaseRef.child(user_online).child(list_users_id).removeValue().
                                                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                FriendsReqDatabaseRef.child(list_users_id).child(user_online).removeValue().
                                                                                        addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    Toast.makeText(getContext(), "Friend Request Cancelled Successfully.!", Toast.LENGTH_SHORT).show();
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
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }
        };

        myRequestsList.setAdapter(FBRA);

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
   {
        View mView;
       public RequestViewHolder(View itemView) {
           super(itemView);
           mView = itemView;
       }


       public void setUserName(String userName)
       {
           TextView user_text = mView.findViewById(R.id.request_profile_name);
           user_text.setText(userName);
       }

       public void setThumbImage(final Context context, final String image)
       {
           final CircleImageView circleImageViewThumb = mView.findViewById(R.id.request_profile_image);

           Picasso.with(context).load(image).
                   networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image)
                   .into(circleImageViewThumb );

           Picasso.with(context).load(image)
                   .placeholder(R.drawable.default_image)
                   .into(circleImageViewThumb, new Callback() {
                       @Override
                       public void onSuccess() {

                       }

                       @Override
                       public void onError() {
                           Picasso.with(context).load(image).
                                   networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image)
                                   .into(circleImageViewThumb );
                       }
                   });
       }

       public void setUserStatus(String userStatus)
       {
           TextView status_text = mView.findViewById(R.id.request_profile_status);
           status_text.setText(userStatus);

       }
   }
}

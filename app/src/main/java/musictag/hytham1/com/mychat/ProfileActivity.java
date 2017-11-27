package musictag.hytham1.com.mychat;

import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity {
    private Button sendFriendRequestButton;
    private Button declineFriendRequestButton;
    private TextView profileName;
    private TextView profileStatus;
    private ImageView profileImage;
    private DatabaseReference UserReference;
    private String CURRANT_STATE ;
    private DatabaseReference friendRequestReference;
    private String sender_user_id;
    private String receiver_user_id;
    private FirebaseAuth mAuth;
    private DatabaseReference FriendsReference;
    private DatabaseReference NotificationsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        friendRequestReference.keepSynced(true);

        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsReference.keepSynced(true);

        NotificationsReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        NotificationsReference.keepSynced(true);
        UserReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();


         receiver_user_id = getIntent().getExtras().get("visit_user_id").toString();


        sendFriendRequestButton = findViewById(R.id.btn_profile_visit_sentRequest);
        declineFriendRequestButton = findViewById(R.id.btn_profile_visit_declineRequest);

        profileName = findViewById(R.id.profile_visit_userName);
        profileStatus = findViewById(R.id.profile_visit_userStatus);
        profileImage = findViewById(R.id.profile_visit_user_image);

        CURRANT_STATE = "not_friends";

        UserReference.child(receiver_user_id).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                final String image = dataSnapshot.child("user_image").getValue().toString();
                String image_thumb = dataSnapshot.child("user_thumb_image").getValue().toString();

                profileName.setText(name);
                profileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_image).into(profileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_image).into(profileImage);
                    }
                });

                friendRequestReference.child(sender_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(receiver_user_id)){
                                String req_type = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();

                                if (req_type.equals("sent")){
                                    CURRANT_STATE = "request_sent";
                                    sendFriendRequestButton.setText("Cancel Friend Request");

                                    declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                    declineFriendRequestButton.setEnabled(false);
                                } else if
                                        (req_type.equals("received"))
                                {
                                    CURRANT_STATE = "request_received";
                                    sendFriendRequestButton.setText("Accept Friend Request");

                                    declineFriendRequestButton.setVisibility(View.VISIBLE);
                                    declineFriendRequestButton.setEnabled(true);

                                    declineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            DeclineFriendRequest();
                                        }
                                    });


                                }
                            }


                        else
                        {
                            FriendsReference.child(sender_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(receiver_user_id)){
                                        CURRANT_STATE = "friends";
                                        sendFriendRequestButton.setText("UnFriend This Person");

                                        declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                        declineFriendRequestButton.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        declineFriendRequestButton.setVisibility(View.INVISIBLE);
        declineFriendRequestButton.setEnabled(false);

         if (!sender_user_id.equals(receiver_user_id)){
         sendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 sendFriendRequestButton.setEnabled(false);


                 if (CURRANT_STATE.equals("not_friends")){
                     sendFriendRequestToPerson();
                 }
                 if (CURRANT_STATE.equals("request_sent")){
                     cancelFriendRequest();
                 }
                 if (CURRANT_STATE.equals("request_received")){
                     acceptFriendRequest();
                 }
                 if (CURRANT_STATE.equals("friends"))
                 {
                     unFriendAFriend();
                 }
             }

         });
     } else {
             sendFriendRequestButton.setVisibility(View.INVISIBLE);
             declineFriendRequestButton.setVisibility(View.INVISIBLE);
         }

    }

    private void DeclineFriendRequest() {
        friendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue().
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue().
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendFriendRequestButton.setEnabled(true);
                                                CURRANT_STATE = "not_friends";
                                                sendFriendRequestButton.setText("Send Friend Request");

                                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                declineFriendRequestButton.setEnabled(false);
                                            }

                                        }
                                    });

                        }

                    }
                });
    }

    private void unFriendAFriend() {
        FriendsReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    FriendsReference.child(receiver_user_id).child(sender_user_id).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendFriendRequestButton.setEnabled(true);
                                CURRANT_STATE = "not_friends";
                                sendFriendRequestButton.setText("Send Friend Request");

                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                declineFriendRequestButton.setEnabled(false);
                            }
                        }
                    });
                }

            }
        });

    }

    private void acceptFriendRequest() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveDate = simpleDateFormat.format(calendar.getTime());

        FriendsReference.child(sender_user_id).child(receiver_user_id).child("date").setValue(saveDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FriendsReference.child(receiver_user_id).child(sender_user_id).child("date").setValue(saveDate)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                friendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue().
                                        addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    friendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue().
                                                            addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        sendFriendRequestButton.setEnabled(true);
                                                                        CURRANT_STATE = "friends";
                                                                        sendFriendRequestButton.setText("UnFriend This Person");

                                                                        declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                        declineFriendRequestButton.setEnabled(false);


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

    private void cancelFriendRequest()
    {

        friendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue().
                addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue().
                            addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendFriendRequestButton.setEnabled(true);
                                CURRANT_STATE = "not_friends";
                                sendFriendRequestButton.setText("Send Friend Request");

                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                declineFriendRequestButton.setEnabled(false);
                            }

                        }
                    });

                }

            }
        });

    }

    private void sendFriendRequestToPerson() {
        friendRequestReference.child(sender_user_id).child(receiver_user_id).child("request_type")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendRequestReference.child(receiver_user_id).child(sender_user_id).child("request_type")
                            .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            HashMap<String , String> notificationData = new HashMap<>();
                            notificationData.put("from" , sender_user_id);
                            notificationData.put("type" , "request" );

                            NotificationsReference.child(receiver_user_id).push().setValue(notificationData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {

                                                sendFriendRequestButton.setEnabled(true);
                                                CURRANT_STATE = "request_sent";
                                                sendFriendRequestButton.setText("Cancel Friend Request");
                                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                declineFriendRequestButton.setEnabled(false);

                                             }
                                        }
                                    });
                        }
                    });
                }

            }
        });
    }


}

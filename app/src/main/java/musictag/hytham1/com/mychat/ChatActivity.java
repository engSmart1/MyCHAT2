package musictag.hytham1.com.mychat;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import model.Messages;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId;
    private String messageReceiverName;
    private Toolbar chatToolBar;

    private TextView userNameTitle;
    private TextView userLastSeen;
    private CircleImageView userChatProfileImage;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private String messageSenderId;

    private ImageButton sendMessageButton;
    private ImageButton sendImageButton;
    private EditText inputMessageText;
    private RecyclerView userMessagesList;
    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();

        messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("user_name").toString();

        chatToolBar = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(chatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar , null);
        actionBar.setCustomView(action_bar_view);

        userNameTitle = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_lastSeen);
        userChatProfileImage = findViewById(R.id.custom_profile_image);

        sendImageButton = findViewById(R.id.select_image);
        sendMessageButton = findViewById(R.id.send_message_btn);
        inputMessageText = findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messageList);
        userMessagesList = findViewById(R.id.messages_list_of_users);

        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setHasFixedSize(true);

        userMessagesList.setLayoutManager(linearLayoutManager);

        userMessagesList.setAdapter(messageAdapter);

        FetchMessages();

        userNameTitle.setText(messageReceiverName);

        rootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                //String userName = dataSnapshot.child("user_name").getValue().toString();
                String online = dataSnapshot.child("online").getValue().toString();
                final String userThumb = dataSnapshot.child("user_thumb_image").getValue().toString();



                Picasso.with(ChatActivity.this).load(userThumb)
                        .placeholder(R.drawable.default_image)
                        .into(userChatProfileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(ChatActivity.this).load(userThumb).
                                        networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image)
                                        .into(userChatProfileImage);
                            }
                        });

                if (online.equals("true"))
                {
                    userLastSeen.setText("online");
                }
                else
                {
                    LastSeenTime getTime = new LastSeenTime();
                    Long last_seen = Long.parseLong(online);
                    String lastSeenLastTime = getTime.getTimeAgo(last_seen , getApplicationContext()).toString();

                    userLastSeen.setText(lastSeenLastTime);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();
            }
        });


    }

    private void FetchMessages()
    {
        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
              Messages messages = dataSnapshot.getValue(Messages.class);
              messageList.add(messages);

              messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage()
    {
        String message_text = inputMessageText.getText().toString().trim();
        if (TextUtils.isEmpty(message_text))
        {
            Toast.makeText(this, "Please write your message", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String message_sender_reference = "Messages/" + messageSenderId + "/" + messageReceiverId;
            String message_receiver_reference = "Messages/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference user_message_key = rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();
            String message_push_id = user_message_key.getKey();

            Map messageTextBody = new HashMap();

            messageTextBody.put("message" , message_text);
            messageTextBody.put("seen" , false);
            messageTextBody.put("type" , "text");
            messageTextBody.put("time" , ServerValue.TIMESTAMP);
            messageTextBody.put("from" , messageSenderId);

            Map messageBodyDetail = new HashMap();
            messageBodyDetail.put(message_sender_reference + "/" + message_push_id ,  messageTextBody);
            messageBodyDetail.put(message_receiver_reference + "/" + message_push_id ,  messageTextBody);

            rootRef.updateChildren(messageBodyDetail, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                {
                    if (databaseError != null)
                    {
                        Log.d("chat_log" , databaseError.getMessage().toString());
                    }

                    inputMessageText.setText("");

                }
            });
        }
    }
}

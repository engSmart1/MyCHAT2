package musictag.hytham1.com.mychat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import model.AllUsers;


public class AllUsersActvity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;

    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users_actvity);
        mToolbar = findViewById(R.id.all_users_app_bar);


        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = findViewById(R.id.all_users_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersRef.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<AllUsers , ViewHolder> FBRA = new FirebaseRecyclerAdapter<AllUsers, ViewHolder>(
                AllUsers.class,
                R.layout.all_users_display_out,
                ViewHolder.class,
                UsersRef

        ) {
            @Override
            protected void populateViewHolder(ViewHolder viewHolder, AllUsers model, final int position) {
                viewHolder.setUserName(model.getUser_name());
                viewHolder.setUserStatus(model.getUser_status());
                viewHolder.setUser_thumb_image(getApplicationContext() , model.getUser_thumb_image());
               // viewHolder.setImage(getApplicationContext() , model.getUser_image());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String visit_user_id = getRef(position).getKey();
                        Intent intent = new Intent(AllUsersActvity.this , ProfileActivity.class);
                        intent.putExtra("visit_user_id" , visit_user_id);
                        startActivity(intent);
                    }
                });

            }
        };
        mRecyclerView.setAdapter(FBRA);
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{


         View mView;
        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView ;
        }
        public void setUserName (String userName){
            TextView userName_text = mView.findViewById(R.id.all_users_username);
            userName_text.setText(userName);
        }
        public void setUserStatus(String userStatus){
            TextView status_text = mView.findViewById(R.id.all_users_status);
            status_text.setText(userStatus);
        }

        public void setUser_thumb_image(final Context context , final String user_thumb_image){
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
    }
}

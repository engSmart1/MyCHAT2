package musictag.hytham1.com.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private static final int GALL_REQ = 1;
    private CircleImageView settingsDisplayProfileImage;
    private TextView settingsDisplayName;
    private TextView settingsDisplayStatus;
    private Button settingsChangeProfileImage;
    private Button settingsChangeStatus;
    private FirebaseAuth mAuth;
    private DatabaseReference getUserDataReference;
    private StorageReference storeProfileImageStorage;
    private Toolbar mToolbar;
    private Bitmap thumb_bitmap = null;
    private StorageReference thumb_refrence;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mAuth = FirebaseAuth.getInstance();
        String currant_user_onLine = mAuth.getCurrentUser().getUid();
        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currant_user_onLine);
        getUserDataReference.keepSynced(true);
        storeProfileImageStorage = FirebaseStorage.getInstance().getReference().child("Profile_Images");
        thumb_refrence = FirebaseStorage.getInstance().getReference().child("Thumb_Images");

        loadingBar = new ProgressDialog(this);

        settingsDisplayProfileImage = findViewById(R.id.settings_profile_image);
        settingsDisplayName = findViewById(R.id.settings_userName);
        settingsDisplayStatus = findViewById(R.id.settings_userStatus);
        settingsChangeProfileImage = findViewById(R.id.btn_settins_profileImage);
        settingsChangeStatus = findViewById(R.id.btn_settings_userStuts);

        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();
                final String image = dataSnapshot.child("user_image").getValue().toString();


//                Glide.with(SettingsActivity.this)
//                        .load(image)
//                        .into(settingsDisplayProfileImage);

                settingsDisplayName.setText(name);
                settingsDisplayStatus.setText(status);

                if (!image.equals("default_profile")) {
                    Picasso.with(SettingsActivity.this).load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_image)
                            .into(settingsDisplayProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_image).into(settingsDisplayProfileImage);
                                }
                            });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        settingsChangeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent gallReq = new Intent(Intent.ACTION_GET_CONTENT);
                gallReq.setType("image/*");

                startActivityForResult(gallReq, GALL_REQ);

            }
        });

        settingsChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity( new Intent(SettingsActivity.this , StatusActivity.class));
                String old_status = settingsDisplayStatus.getText().toString();

                Intent intent = new Intent(SettingsActivity.this, StatusActivity.class);
                intent.putExtra("user_status", old_status);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==  GALL_REQ  && resultCode == RESULT_OK && data !=null) {
            Uri image_uri = data.getData();
            //settingsDisplayProfileImage.setImageURI(image_uri);


            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Updating Profile Image");
                loadingBar.setMessage("Please wait , while updating profile image....");
                loadingBar.show();

                Uri resultUri = result.getUri();


                File thumb_filePath = new File(resultUri.getPath());


                String user_id = mAuth.getCurrentUser().getUid();


                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();


                StorageReference filePath = storeProfileImageStorage.child(user_id + ".jpg");
                final StorageReference thumb_file = thumb_refrence.child(user_id + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(SettingsActivity.this, "Saving your profile image to Firebase storage..",
                                    Toast.LENGTH_SHORT).show();

                            final String download_image = task.getResult().getDownloadUrl().toString();


                            UploadTask uploadTask = thumb_file.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_downLoadUrl = thumb_task.getResult().getDownloadUrl().toString();
                                    if (task.isSuccessful()) {
                                        Map update_user_data = new HashMap();
                                        update_user_data.put("user_image", download_image);
                                        update_user_data.put("user_thumb_image", thumb_downLoadUrl);

                                        getUserDataReference.updateChildren(update_user_data)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(SettingsActivity.this, "Profile Image Updated Successfully", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                });
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(SettingsActivity.this, "Error occurred , while uploading your profile image ",
                                    Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(SettingsActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }
}



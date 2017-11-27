package musictag.hytham1.com.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.system.StructUtsname;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class StatusActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Button saveChangesButton;
    private EditText statusInput;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private ProgressDialog mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        mToolbar = findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        String user_id = mAuth.getCurrentUser().getUid();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        saveChangesButton = findViewById(R.id.btn_save_status_change);
        statusInput = findViewById(R.id.status_input);
        mLoadingBar = new ProgressDialog(this);

        String old_status = getIntent().getExtras().get("user_status").toString();
        statusInput.setText(old_status);

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status_text = statusInput.getText().toString();

                ChangeProfileStatus(status_text);
            }
        });
    }

    private void ChangeProfileStatus(String status_text) {
        
        if (TextUtils.isEmpty(status_text))
        {
            Toast.makeText(this, "Please Write your Status ....", Toast.LENGTH_SHORT).show();
        }
        else {
            mLoadingBar.setTitle("Change Profile Status");
            mLoadingBar.setMessage("Please wait , While we are updating your profile status.... ");
            mLoadingBar.show();
            DatabaseReference filePath = mRef;
            filePath.child("user_status").setValue(status_text).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        mLoadingBar.dismiss();

                        startActivity(new Intent(StatusActivity.this, SettingsActivity.class));

                        Toast.makeText(StatusActivity.this, "Profile Status Updated Successfully..", Toast.LENGTH_SHORT).show();
                    } else
                        {
                        Toast.makeText(StatusActivity.this, "Error occurred...", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


}

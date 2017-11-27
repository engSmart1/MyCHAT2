package musictag.hytham1.com.mychat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference userOnLineRef;
    private Toolbar mToolBar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsPageAdapter mTabsPageAdapter;
    private FirebaseUser current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        current_user = mAuth.getCurrentUser();

        if (current_user != null)
        {
            String user_onLine = mAuth.getCurrentUser().getUid();
            userOnLineRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_onLine);
        }

        mToolBar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("My Chat");

        mViewPager = findViewById(R.id.main_tabs_pager);
        mTabsPageAdapter = new TabsPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsPageAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();
         current_user = mAuth.getCurrentUser();
        if (current_user == null){
           logOutUser();
        } else if(current_user !=null)
        {
         userOnLineRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(current_user != null)
        {
            userOnLineRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void logOutUser() {
        Intent intent = new Intent(MainActivity.this , StartPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.main_menu , menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         if (item.getItemId() == R.id.main_logout_button){
             if (current_user != null)
             {
                 userOnLineRef.child("online").setValue(ServerValue.TIMESTAMP);
             }

             mAuth.signOut();
             logOutUser();
         } if (item.getItemId() == R.id.main_account_settings_button){
             startActivity( new Intent(MainActivity.this , SettingsActivity.class));
        }
        if (item.getItemId() == R.id.main_all_users_button){
             startActivity( new Intent(MainActivity.this , AllUsersActvity.class));
        }
         return true;
    }
}

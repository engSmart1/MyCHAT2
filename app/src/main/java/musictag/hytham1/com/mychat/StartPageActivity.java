package musictag.hytham1.com.mychat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartPageActivity extends AppCompatActivity {

    Button mAlreadyHaveAccount , mNeedNewAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        mAlreadyHaveAccount = findViewById(R.id.already_have_account_button);
        mNeedNewAccount = findViewById(R.id.need_new_account);

        mNeedNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartPageActivity.this , RegisterActivity.class));
            }
        });
        mAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent(StartPageActivity.this , LoginActivity.class));
            }
        });
    }
}

package pht.eatit;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import info.hoang8f.widget.FButton;

public class Welcome extends AppCompatActivity {

    TextView txtSlogan;
    FButton btnSignIn, btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        txtSlogan = findViewById(R.id.txtSlogan);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/nabila.ttf");
        txtSlogan.setTypeface(typeface);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signIn = new Intent(Welcome.this, SignIn.class);
                startActivity(signIn);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUp = new Intent(Welcome.this, SignUp.class);
                startActivity(signUp);
            }
        });
    }
}

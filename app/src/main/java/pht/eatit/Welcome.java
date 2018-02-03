package pht.eatit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import info.hoang8f.widget.FButton;
import io.paperdb.Paper;
import pht.eatit.global.Global;
import pht.eatit.model.User;

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

        // Init paper
        Paper.init(this);
        String phone = Paper.book().read(Global.PHONE);
        String password = Paper.book().read(Global.PASSWORD);

        if(phone != null && password != null){
            if(!phone.isEmpty() && !password.isEmpty()){
                signIn(phone, password);
            }
        }

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

    private void signIn(final String phone, final String password) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference user = database.getReference("User");

        if(Global.isConnectedToInternet(Welcome.this)){
            final ProgressDialog dialog = new ProgressDialog(Welcome.this);
            dialog.setMessage("Please wait...");
            dialog.show();

            user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Check if user existed
                    if(dataSnapshot.child(phone).exists()){
                        dialog.dismiss();

                        // Get user's values
                        User child = dataSnapshot.child(phone).getValue(User.class);
                        child.setPhone(phone);  // Set phone

                        if(child.getPassword().equals(password)){
                            Global.activeUser = child;
                            Intent home = new Intent(Welcome.this, Home.class);
                            startActivity(home);
                            finish();
                        }
                        else {
                            Toast.makeText(Welcome.this, "Wrong password !", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        dialog.dismiss();
                        Toast.makeText(Welcome.this, "User doesn't exist !", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else {
            Toast.makeText(Welcome.this, "Please check your Internet connection !", Toast.LENGTH_SHORT).show();
            return;
        }
    }
}

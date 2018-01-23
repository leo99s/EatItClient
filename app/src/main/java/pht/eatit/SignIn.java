package pht.eatit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import info.hoang8f.widget.FButton;
import pht.eatit.global.Global;
import pht.eatit.model.User;

public class SignIn extends AppCompatActivity {

    MaterialEditText edtPhone, edtPassword;
    FButton btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignIn = findViewById(R.id.btnSignIn);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference user = database.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog dialog = new ProgressDialog(SignIn.this);
                dialog.setMessage("Please wait...");
                dialog.show();

                user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Check if user existed
                        if(dataSnapshot.child(edtPhone.getText().toString()).exists()){
                            dialog.dismiss();

                            // Get user's values
                            User child = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);

                            if(child.getPassword().equals(edtPassword.getText().toString())){
                                Global.activeUser = child;
                                Intent home = new Intent(SignIn.this, Home.class);
                                startActivity(home);
                                finish();
                            }
                            else {
                                Toast.makeText(SignIn.this, "Wrong password !", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            dialog.dismiss();
                            Toast.makeText(SignIn.this, "User doesn't exist !", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}

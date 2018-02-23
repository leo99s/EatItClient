package pht.eatit;

import android.app.ProgressDialog;
import android.content.Context;
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
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignUp extends AppCompatActivity {

    MaterialEditText edtPhone, edtName, edtPassword, edtSecureCode;
    FButton btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtPhone = findViewById(R.id.edtPhone);
        edtName = findViewById(R.id.edtName);
        edtPassword = findViewById(R.id.edtPassword);
        edtSecureCode = findViewById(R.id.edtSecureCode);
        btnSignUp = findViewById(R.id.btnSignUp);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference user = database.getReference("User");

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Global.isConnectedToInternet(SignUp.this)){
                    final ProgressDialog dialog = new ProgressDialog(SignUp.this);
                    dialog.setMessage("Please wait...");
                    dialog.show();

                    user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Check if user existed
                            if(dataSnapshot.child(edtPhone.getText().toString()).exists()){
                                dialog.dismiss();
                                Toast.makeText(SignUp.this, "Phone number is already registered !", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                dialog.dismiss();
                                User child = new User(
                                        edtName.getText().toString(),
                                        edtPassword.getText().toString(),
                                        edtSecureCode.getText().toString());

                                user.child(edtPhone.getText().toString()).setValue(child);
                                Toast.makeText(SignUp.this, "Signed up successfully !", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(SignUp.this, "Please check your Internet connection !", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}

package pht.eatit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;
import info.hoang8f.widget.FButton;
import io.paperdb.Paper;
import pht.eatit.global.Global;
import pht.eatit.model.User;

public class SignIn extends AppCompatActivity {

    MaterialEditText edtPhone, edtPassword;
    CheckBox ckbRemember;
    TextView txtForgotPass;
    FButton btnSignIn;

    FirebaseDatabase database;
    DatabaseReference user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        ckbRemember = findViewById(R.id.ckbRemember);
        txtForgotPass = findViewById(R.id.txtForgotPass);
        btnSignIn = findViewById(R.id.btnSignIn);

        // Init paper
        Paper.init(this);

        database = FirebaseDatabase.getInstance();
        user = database.getReference("User");

        txtForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPassDialog();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Global.isConnectedToInternet(SignIn.this)){
                    final ProgressDialog dialog = new ProgressDialog(SignIn.this);
                    dialog.setMessage("Please wait...");
                    dialog.show();

                    user.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Check if user existed
                            if(dataSnapshot.child(edtPhone.getText().toString()).exists()){
                                dialog.dismiss();

                                // Get user's values
                                User child = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                child.setPhone(edtPhone.getText().toString());  // Set phone

                                if(child.getPassword().equals(edtPassword.getText().toString())){
                                    // Remember phone & password
                                    if(ckbRemember.isChecked()){
                                        Paper.book().write(Global.PHONE, edtPhone.getText().toString());
                                        Paper.book().write(Global.PASSWORD, edtPassword.getText().toString());
                                    }

                                    Global.activeUser = child;
                                    Intent home = new Intent(SignIn.this, Home.class);
                                    startActivity(home);
                                    finish();
                                    user.removeEventListener(this);
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
                else {
                    Toast.makeText(SignIn.this, "Please check your Internet connection !", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    private void showForgotPassDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setIcon(R.drawable.ic_security);
        alert.setTitle("Recover password");
        alert.setMessage("Enter your secure code :");

        LayoutInflater inflater = this.getLayoutInflater();
        View forgot_pass = inflater.inflate(R.layout.forgot_password, null);
        final MaterialEditText edtPhone = forgot_pass.findViewById(R.id.edtPhone);
        final MaterialEditText edtSecureCode = forgot_pass.findViewById(R.id.edtSecureCode);

        alert.setView(forgot_pass);

        alert.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                // Check if user is available
                user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User child = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);

                        if(child.getSecureCode().equals(edtSecureCode.getText().toString())){
                            Toast.makeText(SignIn.this, "Your password : " + child.getPassword(), Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(SignIn.this, "Wrong secure code !", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        alert.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }
}

package pht.eatit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.FacebookSdk;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import dmax.dialog.SpotsDialog;
import info.hoang8f.widget.FButton;
import pht.eatit.global.Global;
import pht.eatit.model.User;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Welcome extends AppCompatActivity {

    TextView txtSlogan;
    FButton btnContinue;

    FirebaseDatabase database;
    DatabaseReference user;

    private static final int ACCOUNT_KIT_REQUEST_CODE = 7171;

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        CalligraphyConfig.initDefault(
//                new CalligraphyConfig.Builder()
//                        .setDefaultFontPath("fonts/restaurant.otf")
//                        .setFontAttrId(R.attr.fontPath)
//                        .build()
//        );

        FacebookSdk.sdkInitialize(getApplicationContext());
        AccountKit.initialize(this);

        setContentView(R.layout.activity_welcome);

        printKeyHash();

        database = FirebaseDatabase.getInstance();
        user = database.getReference("User");

        txtSlogan = findViewById(R.id.txtSlogan);
        btnContinue = findViewById(R.id.btnContinue);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/nabila.ttf");
        txtSlogan.setTypeface(typeface);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginSystem();
            }
        });

        // Check session of Facebook Account Kit
        if(AccountKit.getCurrentAccessToken() != null){
            // Show dialog for waiting
            final AlertDialog waitDialog = new SpotsDialog(this);
            waitDialog.show();
            waitDialog.setMessage("Please wait...");
            waitDialog.setCancelable(false);

            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    // Start login
                    user.child(account.getPhoneNumber().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User child = dataSnapshot.getValue(User.class);
                            Intent home = new Intent(Welcome.this, Home.class);
                            Global.activeUser = child;
                            startActivity(home);
                            waitDialog.dismiss();
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onError(AccountKitError accountKitError) {

                }
            });
        }
    }

    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("pht.eatit", PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures){
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    private void startLoginSystem() {
        Intent accountKit = new Intent(Welcome.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configuration =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN
                );
        accountKit.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configuration.build());
        startActivityForResult(accountKit, ACCOUNT_KIT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ACCOUNT_KIT_REQUEST_CODE){
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);

            if(result.getError() != null){
                Toast.makeText(this, result.getError().getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            } else if(result.wasCancelled()){
                Toast.makeText(this, "You have canceled !", Toast.LENGTH_SHORT).show();
                return;
            } else {
                if(result.getAccessToken() != null){
                    // Show dialog for waiting
                    final AlertDialog waitDialog = new SpotsDialog(this);
                    waitDialog.show();
                    waitDialog.setMessage("Please wait...");
                    waitDialog.setCancelable(false);

                    // Get current phone
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            final String phone = account.getPhoneNumber().toString();

                            // Check if the phone was existed ?
                            user.orderByKey().equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.child(phone).exists()){
                                        // Create a new user
                                        User newUser = new User();
                                        newUser.setPhone(phone);
                                        newUser.setName(phone);
                                        newUser.setAddress("");
                                        newUser.setBalance(String.valueOf(0.0));

                                        user.child(phone).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(Welcome.this, "You have successfully registered !", Toast.LENGTH_SHORT).show();
                                                }

                                                // Start login
                                                user.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        User child = dataSnapshot.getValue(User.class);
                                                        Intent home = new Intent(Welcome.this, Home.class);
                                                        Global.activeUser = child;
                                                        startActivity(home);
                                                        waitDialog.dismiss();
                                                        finish();
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        // Start login
                                        user.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                User child = dataSnapshot.getValue(User.class);
                                                Intent home = new Intent(Welcome.this, Home.class);
                                                Global.activeUser = child;
                                                startActivity(home);
                                                waitDialog.dismiss();
                                                finish();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Toast.makeText(Welcome.this, accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }
}

package pht.eatit;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import info.hoang8f.widget.FButton;
import pht.eatit.database.Database;
import pht.eatit.global.Config;
import pht.eatit.global.Global;
import pht.eatit.model.Notification;
import pht.eatit.model.Order;
import pht.eatit.model.Request;
import pht.eatit.model.Response;
import pht.eatit.model.Sender;
import pht.eatit.model.Token;
import pht.eatit.remote.APIService;
import pht.eatit.viewholder.CartAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import static java.lang.System.currentTimeMillis;

public class Cart extends AppCompatActivity {

    TextView total_price;
    FButton btnOrder;
    RecyclerView rcvCart;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference request;

    List<Order> orderList = new ArrayList<>();
    CartAdapter adapter;

    APIService mService;

    static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX) // Using SandBox for testing, else ENVIRONMENT_PRODUCTION
            .clientId(Config.PAYPAL_ID);

    String address, comment;
    private static final int PAYPAL_REQUEST_CODE = 9999;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(
                new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        setContentView(R.layout.activity_cart);

        // Init PayPal
        Intent paypal = new Intent(this, PayPalService.class);
        paypal.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(paypal);

        mService = Global.getFCMService();

        total_price = findViewById(R.id.total_price);
        btnOrder = findViewById(R.id.btnOrder);
        rcvCart = findViewById(R.id.rcvCart);
        rcvCart.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rcvCart.setLayoutManager(layoutManager);

        database = FirebaseDatabase.getInstance();
        request = database.getReference("Request");

        loadCart();

        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(orderList.size() != 0){
                    showAlert();
                }
                else {
                    Toast.makeText(Cart.this, "Your cart is empty !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(Cart.this);
        alert.setIcon(R.drawable.ic_shopping_cart);
        alert.setTitle("One more step !");
        alert.setMessage("Enter your address :");

        LayoutInflater inflater = this.getLayoutInflater();
        View comment_order = inflater.inflate(R.layout.comment_order, null);
        final MaterialEditText edtAddress = comment_order.findViewById(R.id.edtAddress);
        final MaterialEditText edtComment = comment_order.findViewById(R.id.edtComment);

        alert.setView(comment_order);

        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                // Show PayPal
                address = edtAddress.getText().toString();
                comment = edtComment.getText().toString();
                String fmtPrice = total_price.getText().toString()
                        .replace("$", "")
                        .replace(",", "");

                PayPalPayment payment = new PayPalPayment(
                        new BigDecimal(fmtPrice),
                        "USD",
                        "Eat It Order",
                        PayPalPayment.PAYMENT_INTENT_SALE);

                Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
                startActivityForResult(intent, PAYPAL_REQUEST_CODE);
            }
        });

        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PAYPAL_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                if(confirmation != null){
                    try {
                        String detail = confirmation.toJSONObject().toString(4);
                        JSONObject object = new JSONObject(detail);

                        // Create new request
                        Request newRequest = new Request(
                                Global.activeUser.getPhone(),
                                Global.activeUser.getName(),
                                address,
                                total_price.getText().toString(),
                                "0",
                                comment,
                                object.getJSONObject("response").getString("state"),
                                orderList
                        );

                        // Use System.currentTimeMillis to key and submit to Firebase
                        String id_order = String.valueOf(currentTimeMillis());
                        request.child(id_order).setValue(newRequest);
                        sendNotification(id_order);

                        // Clear the cart
                        new Database(getBaseContext()).clearCart();

                        Toast.makeText(this, "Thanks for your order !", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (JSONException e) {

                    }
                }
            } else if(resultCode == PaymentActivity.RESULT_EXTRAS_INVALID){
                Toast.makeText(this, "Invalid payment !", Toast.LENGTH_SHORT).show();
            } else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Payment was canceled !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendNotification(final String id_order) {
        DatabaseReference reference = database.getInstance().getReference("Token");
        Query tokens = reference.orderByChild("serverToken").equalTo(true);

        tokens.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childDataSnapshot : dataSnapshot.getChildren()){
                    Token serverToken = childDataSnapshot.getValue(Token.class);

                    // Create raw payload to send
                    Notification notification = new Notification("Hoàng Tâm", "You have a new order " + id_order);
                    Sender content = new Sender(serverToken.getToken(), notification);
                    mService.sendNotification(content)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    if(response.code() == 200){
                                        if(response.body().success == 1){
                                            Toast.makeText(Cart.this, "Thanks for your order !", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                        else {
                                            Toast.makeText(Cart.this, "Sorry, something was wrong !", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadCart() {
        orderList = new Database(this).loadOrder();
        adapter = new CartAdapter(this, orderList);
        adapter.notifyDataSetChanged();
        rcvCart.setAdapter(adapter);

        // Calculate total price
        int total = 0;

        for (Order order : orderList){
            total += Integer.parseInt(order.getPrice()) * Integer.parseInt(order.getQuantity());
        }

        Locale locale = new Locale("en", "US");
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
        total_price.setText(numberFormat.format(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Global.DELETE)){
            deleteOrder(item.getOrder());
        }
        
        return true;
    }

    private void deleteOrder(int position) {
        orderList.remove(position);
        new Database(this).clearCart();
        
        for (Order order : orderList){
            new Database(this).addToCart(order);
        }
        
        loadCart();
    }
}

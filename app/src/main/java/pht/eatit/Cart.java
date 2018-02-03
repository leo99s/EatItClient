package pht.eatit;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import info.hoang8f.widget.FButton;
import pht.eatit.database.Database;
import pht.eatit.global.Global;
import pht.eatit.model.Order;
import pht.eatit.model.Request;
import pht.eatit.viewholder.CartAdapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

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
        alert.setTitle("One more step !");
        alert.setMessage("Enter your address :");

        final EditText edtAddress = new EditText(Cart.this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        edtAddress.setLayoutParams(layoutParams);
        alert.setView(edtAddress);  // Add edtAddress to Alert
        alert.setIcon(R.drawable.ic_shopping_cart);

        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                Request newRequest = new Request(
                        Global.activeUser.getPhone(),
                        Global.activeUser.getName(),
                        edtAddress.getText().toString(),
                        total_price.getText().toString(),
                        orderList
                        );

                // Use System.currentTimeMillis to key and submit to Firebase
                request.child(String.valueOf(currentTimeMillis())).setValue(newRequest);

                // Clear the cart
                new Database(getBaseContext()).clearCart();
                Toast.makeText(Cart.this, "Thank for your order !", Toast.LENGTH_SHORT).show();
                finish();
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

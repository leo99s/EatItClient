package pht.eatit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import pht.eatit.global.Global;
import pht.eatit.model.Request;
import pht.eatit.viewholder.OrderViewHolder;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OrderList extends AppCompatActivity {

    RecyclerView rcvOrder;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference request;
    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

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

        setContentView(R.layout.activity_order_list);

        rcvOrder = findViewById(R.id.rcvOrder);

        database = FirebaseDatabase.getInstance();
        request = database.getReference("Request");

        rcvOrder.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rcvOrder.setLayoutManager(layoutManager);

        // getIntent != null : Có String Extra
        if(getIntent() == null){    // Start Order Activity when click Order on Menu Navigation
            loadOrder(Global.activeUser.getPhone());
        }
        else {  // Start Order Activity when click notification of order status
            loadOrder(getIntent().getStringExtra("phone"));
        }
    }

    private void loadOrder(final String phone) {
        Query query = request.orderByChild("phone").equalTo(phone);

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(query, Request.class).build();

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_order, parent, false);

                return new OrderViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, int position, @NonNull Request model) {
                holder.id_order.setText(adapter.getRef(position).getKey());
                holder.phone_order.setText(model.getPhone());
                holder.address_order.setText(model.getAddress());
                holder.status_order.setText(Global.convertCodeToStatus(model.getStatus()));
            }
        };

        adapter.startListening();
        rcvOrder.setAdapter(adapter);
    }
}

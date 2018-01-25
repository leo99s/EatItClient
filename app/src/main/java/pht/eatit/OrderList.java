package pht.eatit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import pht.eatit.global.Global;
import pht.eatit.model.Request;
import pht.eatit.viewholder.OrderViewHolder;

public class OrderList extends AppCompatActivity {

    RecyclerView rcvOrder;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference request;
    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        rcvOrder = findViewById(R.id.rcvOrder);

        database = FirebaseDatabase.getInstance();
        request = database.getReference("Request");

        rcvOrder.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rcvOrder.setLayoutManager(layoutManager);

        loadOrder(Global.activeUser.getPhone());
    }

    private void loadOrder(final String phone) {
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.item_order,
                OrderViewHolder.class,
                request.orderByChild("phone").equalTo(phone)) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, Request model, int position) {
                viewHolder.id_order.setText(adapter.getRef(position).getKey());
                viewHolder.phone_order.setText(model.getPhone());
                viewHolder.address_order.setText(model.getAddress());
                viewHolder.status_order.setText(convertCodeToStatus(model.getStatus()));
            }
        };

        rcvOrder.setAdapter(adapter);
    }

    private String convertCodeToStatus(String status) {
        if(status.equals("0")){
            return "Placed";
        }
        else if(status.equals("1")){
            return "Shipping";
        }
        else {
            return "Shipped";
        }
    }

}

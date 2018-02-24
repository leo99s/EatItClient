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
import pht.eatit.viewholder.RequestViewHolder;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RequestList extends AppCompatActivity {

    RecyclerView rcvRequest;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference request;
    FirebaseRecyclerAdapter<Request, RequestViewHolder> adapter;

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

        setContentView(R.layout.activity_request_list);

        rcvRequest = findViewById(R.id.rcvRequest);

        database = FirebaseDatabase.getInstance();
        request = database.getReference("Request");

        rcvRequest.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rcvRequest.setLayoutManager(layoutManager);

        // getIntent != null : Có String Extra
        if(getIntent() == null){    // Start Order Activity when click Order on Menu Navigation
            loadRequest(Global.activeUser.getPhone());
        }
        else {  // Start Order Activity when click notification of order status
            loadRequest(getIntent().getStringExtra("phone"));
        }
    }

    private void loadRequest(final String phone) {
        Query query = request.orderByChild("phone").equalTo(phone);

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(query, Request.class).build();

        adapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>(options) {
            @Override
            public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_request, parent, false);

                return new RequestViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Request model) {
                holder.id_request.setText(adapter.getRef(position).getKey());
                holder.phone_request.setText(model.getPhone());
                holder.address_request.setText(model.getAddress());
                holder.delivery_status_request.setText(Global.getDeliveryStatus(model.getDeliveryStatus()));
            }
        };

        adapter.startListening();
        rcvRequest.setAdapter(adapter);
    }
}

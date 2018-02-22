package pht.eatit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
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

import pht.eatit.model.Rating;
import pht.eatit.viewholder.CommentViewHolder;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Comment extends AppCompatActivity {

    SwipeRefreshLayout swipe_layout;
    RecyclerView rcvComment;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference rating;

    FirebaseRecyclerAdapter<Rating, CommentViewHolder> adapter;

    String food_id = "";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(adapter != null){
            adapter.stopListening();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurent.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_comment);

        swipe_layout = findViewById(R.id.swipe_layout);
        rcvComment = findViewById(R.id.rcvComment);
        layoutManager = new LinearLayoutManager(this);
        rcvComment.setLayoutManager(layoutManager);

        database = FirebaseDatabase.getInstance();
        rating = database.getReference("Rating");

        // Load comments on the first time
        swipe_layout.post(new Runnable() {
            @Override
            public void run() {
                swipe_layout.setRefreshing(true);

                if(getIntent() != null){
                    food_id = getIntent().getStringExtra("food_id");
                }

                if(food_id != null && !food_id.isEmpty()){
                    Query query = rating.orderByChild("food_id").equalTo(food_id);

                    FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query, Rating.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<Rating, CommentViewHolder>(options) {
                        @Override
                        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.item_comment, parent, false);

                            return new CommentViewHolder(view);
                        }

                        @Override
                        protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Rating model) {
                            holder.txtPhone.setText(model.getPhone());
                            holder.rating_bar.setRating(Float.parseFloat(model.getRating()));
                            holder.txtComment.setText(model.getComment());
                        }
                    };

                    loadComment(food_id);
                }
            }
        });

        swipe_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(getIntent() != null){
                    food_id = getIntent().getStringExtra("food_id");
                }

                if(food_id != null && !food_id.isEmpty()){
                    Query query = rating.orderByChild("food_id").equalTo(food_id);

                    FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query, Rating.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<Rating, CommentViewHolder>(options) {
                        @Override
                        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.item_comment, parent, false);

                            return new CommentViewHolder(view);
                        }

                        @Override
                        protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Rating model) {
                            holder.txtPhone.setText(model.getPhone());
                            holder.rating_bar.setRating(Float.parseFloat(model.getRating()));
                            holder.txtComment.setText(model.getComment());
                        }
                    };

                    loadComment(food_id);
                }
            }
        });
    }

    private void loadComment(String food_id) {
        adapter.startListening();
        rcvComment.setAdapter(adapter);
        swipe_layout.setRefreshing(false);
    }
}

package pht.eatit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.util.ArrayList;
import java.util.List;
import pht.eatit.database.Database;
import pht.eatit.global.Global;
import pht.eatit.model.Favorite;
import pht.eatit.model.Food;
import pht.eatit.model.Order;
import pht.eatit.onclick.ItemClickListener;
import pht.eatit.viewholder.FoodViewHolder;

public class FoodList extends AppCompatActivity {

    String category_id = "";

    SwipeRefreshLayout swipe_layout;
    MaterialSearchBar bar_search;
    RecyclerView rcvFood;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference food;

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;

    List<String> suggestedList = new ArrayList<>();

    Database favorite;

    // Share to Facebook
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    // Create target from Picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // Create image from bitmap
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap).build();

            if(ShareDialog.canShow(SharePhotoContent.class)){
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo).build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        // Init Facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        database = FirebaseDatabase.getInstance();
        food = database.getReference("Food");

        favorite = new Database(this);

        swipe_layout = findViewById(R.id.swipe_layout);

        swipe_layout.setColorSchemeResources(
                R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );

        rcvFood = findViewById(R.id.rcvFood);
        rcvFood.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rcvFood.setLayoutManager(layoutManager);
        bar_search = findViewById(R.id.bar_search);

        // Loading for the first time by default
        swipe_layout.post(new Runnable() {
            @Override
            public void run() {
                // Get Category_ID from the previous activity
                if(getIntent() != null){
                    category_id = getIntent().getStringExtra("category_id");
                }

                if(!category_id.isEmpty() && category_id != null){
                    if(Global.isConnectedToInternet(FoodList.this)){
                        loadFood(category_id);
                    }
                    else {
                        Toast.makeText(FoodList.this, "Please check your Internet connection !", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Search after getting category_id
                bar_search.setHint("Enter a food name...");
                bar_search.setSpeechMode(false);
                bar_search.setCardViewElevation(10);
                loadSuggestion();
                bar_search.setLastSuggestions(suggestedList);

                bar_search.addTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // Changing suggestions when typing
                        List<String> suggestions = new ArrayList<>();

                        for (String item : suggestedList){
                            if(item.toLowerCase().contains(bar_search.getText().toLowerCase())){
                                suggestions.add(item);
                            }
                        }

                        bar_search.setLastSuggestions(suggestions);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                bar_search.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                    @Override
                    public void onSearchStateChanged(boolean enabled) {
                        // When searching is closed, restore original adapter
                        if(!enabled){
                            rcvFood.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onSearchConfirmed(CharSequence text) {
                        // When searching is done, show results
                        search(text);
                    }

                    @Override
                    public void onButtonClicked(int buttonCode) {

                    }
                });
            }
        });

        swipe_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Get Category_ID from the previous activity
                if(getIntent() != null){
                    category_id = getIntent().getStringExtra("category_id");
                }

                if(!category_id.isEmpty() && category_id != null){
                    if(Global.isConnectedToInternet(FoodList.this)){
                        loadFood(category_id);
                    }
                    else {
                        Toast.makeText(FoodList.this, "Please check your Internet connection !", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void loadFood(String category_id) {
        Query query = food.orderByChild("category_id").equalTo(category_id);

        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(query, Food.class).build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_food, parent, false);

                return new FoodViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder holder, final int position, @NonNull final Food model) {
                holder.name_food.setText(model.getName());
                holder.price_food.setText(String.format("$ %s", model.getPrice()));
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.image_food);

                if(favorite.isFavorite(Global.activeUser.getPhone(), adapter.getRef(position).getKey())){
                    holder.image_favorite.setImageResource(R.drawable.ic_favorite);
                }

                // Click to change the state of favorite
                holder.image_favorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Favorite newFavorite = new Favorite();
                        newFavorite.setPhone(Global.activeUser.getPhone());
                        newFavorite.setFood_id(adapter.getRef(position).getKey());
                        newFavorite.setCategory_id(model.getCategory_id());
                        newFavorite.setName(model.getName());
                        newFavorite.setImage(model.getImage());
                        newFavorite.setDescription(model.getDescription());
                        newFavorite.setPrice(model.getPrice());
                        newFavorite.setDiscount(model.getDiscount());

                        if(!favorite.isFavorite(Global.activeUser.getPhone(), adapter.getRef(position).getKey())){
                            favorite.addToFavorite(newFavorite);
                            holder.image_favorite.setImageResource(R.drawable.ic_favorite);
                            Toast.makeText(FoodList.this, model.getName() + " was added to your favorite !", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            favorite.removeFromFavorite(Global.activeUser.getPhone(), adapter.getRef(position).getKey());
                            holder.image_favorite.setImageResource(R.drawable.ic_favorite_border);
                            Toast.makeText(FoodList.this, model.getName() + " was deleted from your favorite !", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // Click to share to Facebook
                holder.image_share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.with(getApplicationContext()).load(model.getImage()).into(target);
                    }
                });

                // Quick cart
                holder.image_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final boolean isFoodExisted = new Database(FoodList.this).isFoodExisted(Global.activeUser.getPhone(), adapter.getRef(position).getKey());

                        if (!isFoodExisted) {
                            new Database(getBaseContext()).addOrder(new Order(
                                    Global.activeUser.getPhone(),
                                    adapter.getRef(position).getKey(),
                                    model.getImage(),
                                    model.getName(),
                                    model.getPrice(),
                                    "1",
                                    model.getDiscount()
                            ));
                        } else {
                            new Database(FoodList.this).increaseOrder(
                                    Global.activeUser.getPhone(),
                                    adapter.getRef(position).getKey());
                        }

                        Toast.makeText(FoodList.this, "Added to your cart !", Toast.LENGTH_SHORT).show();
                    }
                });

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("food_id", adapter.getRef(position).getKey());
                        startActivity(foodDetail);
                        finish();
                    }
                });
            }
        };

        adapter.startListening();
        rcvFood.setAdapter(adapter);
        swipe_layout.setRefreshing(false);
    }

    private void loadSuggestion() {
        food.orderByChild("category_id").equalTo(category_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    Food child = childSnapshot.getValue(Food.class);
                    suggestedList.add(child.getName()); // Add food names to suggestedList
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void search(CharSequence text) {
        Query query = food.orderByChild("name").equalTo(text.toString());

        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(query, Food.class).build();

        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_food, parent, false);

                return new FoodViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                holder.name_food.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.image_food);

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("food_id", searchAdapter.getRef(position).getKey());
                        startActivity(foodDetail);
                        finish();
                    }
                });
            }
        };

        searchAdapter.startListening();
        rcvFood.setAdapter(searchAdapter);
    }
}

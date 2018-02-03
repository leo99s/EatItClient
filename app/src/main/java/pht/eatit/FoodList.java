package pht.eatit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

import pht.eatit.global.Global;
import pht.eatit.model.Food;
import pht.eatit.onclick.ItemClickListener;
import pht.eatit.viewholder.FoodViewHolder;

public class FoodList extends AppCompatActivity {

    String category_id = "";

    MaterialSearchBar bar_search;

    RecyclerView rcvFood;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference food;
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        bar_search = findViewById(R.id.bar_search);

        rcvFood = findViewById(R.id.rcvFood);
        rcvFood.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rcvFood.setLayoutManager(layoutManager);

        database = FirebaseDatabase.getInstance();
        food = database.getReference("Food");

        // Get Category_ID from the previous activity
        if(getIntent() != null){
            category_id = getIntent().getStringExtra("category_id");
        }

        if(!category_id.isEmpty() && category_id != null){
            if(Global.isConnectedToInternet(FoodList.this)){
                loadFood(category_id);
            }
            else {
                Toast.makeText(this, "Please check your Internet connection !", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Search
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

    private void search(CharSequence text) {
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.item_food,
                FoodViewHolder.class,
                food.orderByChild("Name").equalTo(text.toString())
        ) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                viewHolder.name_food.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.image_food);

                viewHolder.setItemClickListener(new ItemClickListener() {
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

        rcvFood.setAdapter(searchAdapter);
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

    private void loadFood(String category_id) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                pht.eatit.model.Food.class,
                R.layout.item_food,
                FoodViewHolder.class,
                food.orderByChild("category_id").equalTo(category_id)) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, pht.eatit.model.Food model, int position) {
                viewHolder.name_food.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.image_food);

                viewHolder.setItemClickListener(new ItemClickListener() {
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

        rcvFood.setAdapter(adapter);
    }
}

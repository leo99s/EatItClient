package pht.eatit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import pht.eatit.model.*;
import pht.eatit.onclick.ItemClickListener;
import pht.eatit.viewholder.FoodViewHolder;

public class FoodList extends AppCompatActivity {

    String Category_ID = "";

    RecyclerView rcvFood;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference food;
    FirebaseRecyclerAdapter<pht.eatit.model.Food, FoodViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        rcvFood = findViewById(R.id.rcvFood);
        rcvFood.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rcvFood.setLayoutManager(layoutManager);

        database = FirebaseDatabase.getInstance();
        food = database.getReference("Food");

        // Get Category_ID from the previous activity
        if(getIntent() != null){
            Category_ID = getIntent().getStringExtra("Category_ID");
        }

        if(!Category_ID.isEmpty() && Category_ID != null){
            loadFood(Category_ID);
        }
    }

    private void loadFood(String category_id) {
        adapter = new FirebaseRecyclerAdapter<pht.eatit.model.Food, FoodViewHolder>(
                pht.eatit.model.Food.class,
                R.layout.item_food,
                FoodViewHolder.class,
                food.orderByChild("Category_ID").equalTo(category_id)) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, pht.eatit.model.Food model, int position) {
                viewHolder.name_food.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.image_food);

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("Food_ID", adapter.getRef(position).getKey());
                        startActivity(foodDetail);
                        finish();
                    }
                });
            }
        };

        rcvFood.setAdapter(adapter);
    }
}

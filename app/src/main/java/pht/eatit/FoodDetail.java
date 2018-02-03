package pht.eatit;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import pht.eatit.database.Database;
import pht.eatit.global.Global;
import pht.eatit.model.Food;
import pht.eatit.model.Order;

public class FoodDetail extends AppCompatActivity {

    String food_id = "";

    CollapsingToolbarLayout collapsing_toolbar;
    ImageView image_food;
    TextView name_food, price_food, description_food;
    FloatingActionButton btnAdd;
    ElegantNumberButton btnQuantity;

    FirebaseDatabase database;
    DatabaseReference food;
    Food currentFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        image_food = findViewById(R.id.image_food);
        name_food = findViewById(R.id.name_food);
        price_food = findViewById(R.id.price_food);
        description_food = findViewById(R.id.description_food);
        btnAdd = findViewById(R.id.btnAdd);
        btnQuantity = findViewById(R.id.btnQuantity);

        collapsing_toolbar.setCollapsedTitleTextAppearance(R.style.collapsedApBar);
        collapsing_toolbar.setExpandedTitleTextAppearance(R.style.expandedApBar);

        database = FirebaseDatabase.getInstance();
        food = database.getReference("Food");

        // Get Food_ID from the previous activity
        if(getIntent() != null){
            food_id = getIntent().getStringExtra("food_id");
        }

        if(!food_id.isEmpty() && food_id != null){
            if(Global.isConnectedToInternet(FoodDetail.this)){
                getFoodDetail(food_id);
            }
            else {
                Toast.makeText(this, "Please check your Internet connection !", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addToCart(new Order(
                        food_id,
                        currentFood.getName(),
                        currentFood.getPrice(),
                        btnQuantity.getNumber(),
                        currentFood.getDiscount()
                ));

                Toast.makeText(FoodDetail.this, "Added to your cart !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFoodDetail(String food_id) {
        food.child(food_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);
                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(image_food);
                collapsing_toolbar.setTitle(currentFood.getName());
                name_food.setText(currentFood.getName());
                price_food.setText(currentFood.getPrice());
                description_food.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

package pht.eatit;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;
import java.util.Arrays;
import info.hoang8f.widget.FButton;
import pht.eatit.database.Database;
import pht.eatit.global.Global;
import pht.eatit.model.Food;
import pht.eatit.model.Order;
import pht.eatit.model.Rating;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

    String food_id = "";

    CollapsingToolbarLayout collapsing_toolbar;
    ImageView image_food;
    TextView name_food, price_food, description_food;
    FloatingActionButton btnRating;
    CounterFab btnAdd;
    ElegantNumberButton btnQuantity;
    RatingBar rating_bar;
    FButton btnShowComment;

    FirebaseDatabase database;
    DatabaseReference food;
    DatabaseReference rating;
    Food currentFood;

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

        setContentView(R.layout.activity_food_detail);

        collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        image_food = findViewById(R.id.image_food);
        name_food = findViewById(R.id.name_food);
        price_food = findViewById(R.id.price_food);
        description_food = findViewById(R.id.description_food);
        btnRating = findViewById(R.id.btnRating);
        btnAdd = findViewById(R.id.btnAdd);
        btnQuantity = findViewById(R.id.btnQuantity);
        rating_bar = findViewById(R.id.rating_bar);
        btnShowComment = findViewById(R.id.btnShowComment);

        collapsing_toolbar.setCollapsedTitleTextAppearance(R.style.collapsedApBar);
        collapsing_toolbar.setExpandedTitleTextAppearance(R.style.expandedApBar);

        database = FirebaseDatabase.getInstance();
        food = database.getReference("Food");
        rating = database.getReference("Rating");

        // Get Food_ID from the previous activity
        if(getIntent() != null){
            food_id = getIntent().getStringExtra("food_id");
        }

        if(!food_id.isEmpty() && food_id != null){
            if(Global.isConnectedToInternet(FoodDetail.this)){
                getFoodDetail(food_id);
                getRatingFood(food_id);
            }
            else {
                Toast.makeText(this, "Please check your Internet connection !", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addOrder(new Order(
                        food_id,
                        currentFood.getImage(),
                        currentFood.getName(),
                        currentFood.getPrice(),
                        btnQuantity.getNumber(),
                        currentFood.getDiscount()
                ));

                Toast.makeText(FoodDetail.this, "Added to your cart !", Toast.LENGTH_SHORT).show();
            }
        });

        btnShowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentList = new Intent(FoodDetail.this, CommentList.class);
                commentList.putExtra("food_id", food_id);
                startActivity(commentList);
                finish();
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

    private void getRatingFood(String food_id) {
        Query rating_food = rating.orderByChild("food_id").equalTo(food_id);
        
        rating_food.addValueEventListener(new ValueEventListener() {
            int count = 0, sum = 0;
            
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childDataSnapshot : dataSnapshot.getChildren()){
                    Rating child = childDataSnapshot.getValue(Rating.class);
                    sum += Integer.parseInt(child.getRating());
                    count++;
                }
                
                if(count != 0){
                    float avg = sum/count;
                    rating_bar.setRating(avg);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not Good", "Quite OK", "Very Good", "Excellent"))
                .setDefaultRating(3)
                .setTitle("Rate this food")
                .setDescription("Please give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here...")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.ratingDialogFadeAnim)
                .create(FoodDetail.this).show();
                
    }

    @Override
    public void onPositiveButtonClicked(int star, String comment) {
        // Get star rating and upload to Firebase
        final Rating child = new Rating(
                Global.activeUser.getPhone(),
                food_id,
                String.valueOf(star),
                comment
        );

        // User can rate multiple times
        rating.push().setValue(child).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(FoodDetail.this, "Thanks for your feedback !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }
}

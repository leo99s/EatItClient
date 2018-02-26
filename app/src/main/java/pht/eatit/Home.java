package pht.eatit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.facebook.accountkit.AccountKit;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import pht.eatit.database.Database;
import pht.eatit.global.Global;
import pht.eatit.model.Banner;
import pht.eatit.model.Category;
import pht.eatit.model.Token;
import pht.eatit.onclick.ItemClickListener;
import pht.eatit.service.FirebaseMessaging;
import pht.eatit.viewholder.CategoryViewHolder;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView txtUserName;
    SwipeRefreshLayout swipe_layout;
    SliderLayout slider;
    RecyclerView rcvCategory;
    RecyclerView.LayoutManager layoutManager;
    CounterFab btnCart;

    FirebaseDatabase database;
    DatabaseReference category;
    FirebaseRecyclerAdapter<Category, CategoryViewHolder> adapter;

    HashMap<String, String> imageList;

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

        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        Paper.init(this);

        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        btnCart = findViewById(R.id.btnCart);
        btnCart.setCount(new Database(this).getCartCount(Global.activeUser.getPhone()));

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cart = new Intent(Home.this, Cart.class);
                startActivity(cart);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set name for header
        View header = navigationView.getHeaderView(0);
        txtUserName = header.findViewById(R.id.txtUserName);
        txtUserName.setText(Global.activeUser.getName());

        swipe_layout = findViewById(R.id.swipe_layout);
        swipe_layout.setColorSchemeResources(
                R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );

        // Loading for the first time by default
        swipe_layout.post(new Runnable() {
            @Override
            public void run() {
                if(Global.isConnectedToInternet(Home.this)){
                    setSlider();
                    loadCategory();
                }
                else {
                    Toast.makeText(Home.this, "Please check your Internet connection !", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        swipe_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Global.isConnectedToInternet(Home.this)){
                    setSlider();
                    loadCategory();
                }
                else {
                    Toast.makeText(Home.this, "Please check your Internet connection !", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        rcvCategory = findViewById(R.id.rcvCategory);
        rcvCategory.setLayoutManager(new GridLayoutManager(Home.this, 2));

        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(
                rcvCategory.getContext(),
                R.anim.layout_fall_down
        );

        rcvCategory.setLayoutAnimation(controller);

        if(Global.isConnectedToInternet(Home.this)){
            setSlider();
            loadCategory();
        } else {
            Toast.makeText(this, "Please check your Internet connection !", Toast.LENGTH_SHORT).show();
            return;
        }

        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnCart.setCount(new Database(this).getCartCount(Global.activeUser.getPhone()));

        if(adapter != null){
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        slider.stopAutoCycle();
    }

    private void updateToken(String token) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Token");
        Token child = new Token(token, false);
        reference.child(Global.activeUser.getPhone()).setValue(child);
    }

    private void setSlider() {
        slider = findViewById(R.id.slider);
        imageList = new HashMap<>();

        final DatabaseReference banner = database.getReference("Banner");

        banner.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()){
                    Banner child = childDataSnapshot.getValue(Banner.class);
                    imageList.put(child.getFood_id() + "_" + child.getName(), child.getImage());
                }

                for (String key : imageList.keySet()){
                    String[] regex = key.split("_");
                    String food_id = regex[0];
                    String name = regex[1];

                    // Create banner slider
                    final TextSliderView sliderView = new TextSliderView(Home.this);
                    sliderView.bundle(new Bundle());
                    sliderView.getBundle().putString("food_id", food_id);

                    sliderView.description(name)
                            .image(imageList.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent foodDetail = new Intent(Home.this, FoodDetail.class);
                                    foodDetail.putExtras(sliderView.getBundle());
                                    startActivity(foodDetail);
                                    finish();
                                }
                            });

                    slider.addSlider(sliderView);

                    // Remove event after done
                    banner.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        slider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        slider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        slider.setCustomAnimation(new DescriptionAnimation());
        slider.setDuration(3000);
    }

    private void loadCategory() {
        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category, Category.class).build();

        adapter = new FirebaseRecyclerAdapter<Category, CategoryViewHolder>(options) {
            @Override
            public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_category, parent, false);

                return new CategoryViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CategoryViewHolder holder, int position, @NonNull Category model) {
                holder.name_category.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.image_category);

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        // Get Category_ID and send to new activity
                        Intent foodList = new Intent(Home.this, FoodList.class);

                        // Category_ID = Key of Category table
                        foodList.putExtra("category_id", adapter.getRef(position).getKey());
                        startActivity(foodList);
                    }
                });
            }
        };

        adapter.startListening();
        rcvCategory.setAdapter(adapter);
        swipe_layout.setRefreshing(false);

        // Animation
        rcvCategory.getAdapter().notifyDataSetChanged();
        rcvCategory.scheduleLayoutAnimation();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.search){
            Intent search = new Intent(Home.this, Search.class);
            startActivity(search);
        } else if(item.getItemId() == R.id.settings){
            AlertDialog.Builder alert = new AlertDialog.Builder(Home.this);
            alert.setTitle("Settings");

            LayoutInflater inflater = LayoutInflater.from(this);
            View layout_settings = inflater.inflate(R.layout.layout_settings, null);

            final CheckBox ckbSubscribe = layout_settings.findViewById(R.id.ckbSubscribe);

            // Remember the state of ckbSubscribe
            Paper.init(this);
            String isChecked = Paper.book().read("subscribed");

            if(isChecked == null || TextUtils.isEmpty(isChecked) || isChecked.equals("false")){
                ckbSubscribe.setChecked(false);
            } else {
                ckbSubscribe.setChecked(true);
            }

            alert.setView(layout_settings);

            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();

                    if(ckbSubscribe.isChecked()){
                        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("News");
                        Paper.book().write("subscribed", "true");
                    } else {
                        com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic("News");
                        Paper.book().write("subscribed", "false");
                    }
                }
            });

            alert.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_category) {
            Intent home = new Intent(Home.this, Home.class);
            startActivity(home);
        } else if(id == R.id.nav_profile){
            showProfileDialog();
        } else if(id == R.id.nav_favorite){
            Intent favoriteList = new Intent(Home.this, FavoriteList.class);
            startActivity(favoriteList);
        } else if (id == R.id.nav_cart) {
            Intent cart = new Intent(Home.this, Cart.class);
            startActivity(cart);
        } else if (id == R.id.nav_order) {
            Intent requestList = new Intent(Home.this, RequestList.class);
            startActivity(requestList);
        } else if (id == R.id.nav_sign_out) {
            AccountKit.logOut();
            Intent welcome = new Intent(Home.this, Welcome.class);
            welcome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(welcome);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showProfileDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(Home.this);
        alert.setTitle("Update profile");
        alert.setMessage("Please fill all info :");

        LayoutInflater inflater = LayoutInflater.from(this);
        View update_profile = inflater.inflate(R.layout.update_profile, null);

        final MaterialEditText edtName = update_profile.findViewById(R.id.edtName);
        final MaterialEditText edtAddress = update_profile.findViewById(R.id.edtAddress);

        edtName.setText(Global.activeUser.getName().toString());
        edtAddress.setText(Global.activeUser.getAddress().toString());

        alert.setView(update_profile);

        alert.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                // Use android.app for SpotsDialog
                final android.app.AlertDialog waitDialog = new SpotsDialog(Home.this);
                waitDialog.show();

                // Update profile
                HashMap<String, Object> object = new HashMap<>();
                object.put("name", edtName.getText().toString());
                object.put("address", edtAddress.getText().toString());

                DatabaseReference user = database.getReference("User");
                user.child(Global.activeUser.getPhone()).updateChildren(object).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        waitDialog.dismiss();

                        if(task.isSuccessful()){
                            Global.activeUser.setAddress(edtAddress.getText().toString());
                            Toast.makeText(Home.this, "Your profile was updated !", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });

        alert.show();
    }
}

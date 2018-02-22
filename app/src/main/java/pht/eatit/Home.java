package pht.eatit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Map;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import pht.eatit.database.Database;
import pht.eatit.global.Global;
import pht.eatit.model.Category;
import pht.eatit.model.Token;
import pht.eatit.onclick.ItemClickListener;
import pht.eatit.viewholder.CategoryViewHolder;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView txtUserName;
    SwipeRefreshLayout swipe_layout;
    RecyclerView rcvCategory;
    RecyclerView.LayoutManager layoutManager;
    CounterFab fab;

    FirebaseDatabase database;
    DatabaseReference category;
    FirebaseRecyclerAdapter<Category, CategoryViewHolder> adapter;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        Paper.init(this);

        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

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

        fab = findViewById(R.id.fab);
        //fab.setCount(new Database(this).getCartCount());

        fab.setOnClickListener(new View.OnClickListener() {
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
                    loadCategory();
                }
                else {
                    Toast.makeText(Home.this, "Please check your Internet connection !", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        rcvCategory = findViewById(R.id.rcvCategory);
        rcvCategory.setLayoutManager(new GridLayoutManager(Home.this,2));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(
                rcvCategory.getContext(),
                R.anim.layout_fall_down
        );

        rcvCategory.setLayoutAnimation(controller);

        if(Global.isConnectedToInternet(Home.this)){
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
        fab.setCount(new Database(this).getCartCount());

        if(adapter != null){
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void updateToken(String token) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Token");
        Token child = new Token(token, false);
        reference.child(Global.activeUser.getPhone()).setValue(child);
    }

    private void loadCategory() {
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
        if(item.getItemId() == R.id.refresh){
            loadCategory();
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
        } else if (id == R.id.nav_cart) {
            Intent cart = new Intent(Home.this, Cart.class);
            startActivity(cart);
        } else if (id == R.id.nav_order) {
            Intent requestList = new Intent(Home.this, RequestList.class);
            startActivity(requestList);
        } else if(id == R.id.nav_address){
            showAddressDialog();
        } else if(id == R.id.nav_pass){
            showPassDialog();
        } else if (id == R.id.nav_sign_out) {
            // Delete remembered user
            Paper.book().destroy();

            Intent signIn = new Intent(Home.this, SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(signIn);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showAddressDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(Home.this);
        alert.setTitle("Change home address");
        alert.setMessage("Please fill all info :");

        LayoutInflater inflater = LayoutInflater.from(this);
        View home_address = inflater.inflate(R.layout.home_address, null);

        final MaterialEditText edtAddress = home_address.findViewById(R.id.edtAddress);

        alert.setView(home_address);

        alert.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();

                Global.activeUser.setAddress(edtAddress.getText().toString());

                database.getReference("User")
                        .child(Global.activeUser.getPhone())
                        .setValue(Global.activeUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Home.this, "Update home address successfully !", Toast.LENGTH_SHORT).show();
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

    private void showPassDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(Home.this);
        alert.setTitle("Change password");
        alert.setMessage("Please fill all info :");

        LayoutInflater inflater = LayoutInflater.from(this);
        View change_password = inflater.inflate(R.layout.change_password, null);

        final MaterialEditText edtOldPass = change_password.findViewById(R.id.edtOldPass);
        final MaterialEditText edtNewPass1 = change_password.findViewById(R.id.edtNewPass1);
        final MaterialEditText edtNewPass2 = change_password.findViewById(R.id.edtNewPass2);

        alert.setView(change_password);

        alert.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                // Use android.app for SpotsDialog
                final android.app.AlertDialog waitDialog = new SpotsDialog(Home.this);
                waitDialog.show();

                if(edtOldPass.getText().toString().equals(Global.activeUser.getPassword())){
                    if(edtNewPass1.getText().toString().equals(edtNewPass2.getText().toString())){
                        Map<String, Object> update = new HashMap<>();
                        update.put("Password", edtNewPass2.getText().toString());

                        // Update password
                        DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                        user.child(Global.activeUser.getPhone())
                                .updateChildren(update)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        waitDialog.dismiss();
                                        Toast.makeText(Home.this, "Password was updated !", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        waitDialog.dismiss();
                        Toast.makeText(Home.this, "New password doesn't match !", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    waitDialog.dismiss();
                    Toast.makeText(Home.this, "Wrong old password !", Toast.LENGTH_SHORT).show();
                }
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

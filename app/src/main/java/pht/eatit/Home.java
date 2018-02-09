package pht.eatit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
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
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import pht.eatit.global.Global;
import pht.eatit.model.Category;
import pht.eatit.model.Token;
import pht.eatit.onclick.ItemClickListener;
import pht.eatit.viewholder.CategoryViewHolder;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView txtUserName;
    RecyclerView rcvCategory;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference category;
    FirebaseRecyclerAdapter<Category, CategoryViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        Paper.init(this);

        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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

        rcvCategory = findViewById(R.id.rcvCategory);
        rcvCategory.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rcvCategory.setLayoutManager(layoutManager);

        if(Global.isConnectedToInternet(Home.this)){
            loadCategory();
        }
        else {
            Toast.makeText(this, "Please check your Internet connection !", Toast.LENGTH_SHORT).show();
            return;
        }

        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void updateToken(String token) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Token");
        Token child = new Token(token, false);
        reference.child(Global.activeUser.getPhone()).setValue(child);
    }

    private void loadCategory() {
        adapter = new FirebaseRecyclerAdapter<Category, CategoryViewHolder>(Category.class, R.layout.item_category, CategoryViewHolder.class, category) {
            @Override
            protected void populateViewHolder(CategoryViewHolder viewHolder, Category model, int position) {
                viewHolder.name_category.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.image_category);

                viewHolder.setItemClickListener(new ItemClickListener() {
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

        rcvCategory.setAdapter(adapter);
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
            Intent orderList = new Intent(Home.this, OrderList.class);
            startActivity(orderList);
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

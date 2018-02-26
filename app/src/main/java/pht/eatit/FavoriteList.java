package pht.eatit;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.RelativeLayout;
import pht.eatit.database.Database;
import pht.eatit.global.Global;
import pht.eatit.helper.ItemTouch;
import pht.eatit.model.Favorite;
import pht.eatit.onclick.ItemSwipeListener;
import pht.eatit.viewholder.FavoriteAdapter;
import pht.eatit.viewholder.FavoriteViewHolder;

public class FavoriteList extends AppCompatActivity implements ItemSwipeListener {

    RelativeLayout root_layout;
    RecyclerView rcvFavorite;
    RecyclerView.LayoutManager layoutManager;
    ItemTouchHelper.SimpleCallback itemTouch;

    FavoriteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);

        root_layout = findViewById(R.id.root_layout);
        rcvFavorite = findViewById(R.id.rcvFavorite);
        layoutManager = new LinearLayoutManager(this);
        rcvFavorite.setLayoutManager(layoutManager);

        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(
                rcvFavorite.getContext(),
                R.anim.layout_from_left
        );

        rcvFavorite.setLayoutAnimation(controller);

        itemTouch = new ItemTouch(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouch).attachToRecyclerView(rcvFavorite);

        loadFavorite();
    }

    private void loadFavorite() {
        adapter = new FavoriteAdapter(this, new Database(this).loadFavorite(Global.activeUser.getPhone()));
        rcvFavorite.setAdapter(adapter);
    }

    @Override
    public void onSwipe(RecyclerView.ViewHolder viewHolder, int position, int direction) {
        if(viewHolder instanceof FavoriteViewHolder){
            final Favorite deleted_food = ((FavoriteAdapter) rcvFavorite.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int index = viewHolder.getAdapterPosition();
            String name_food = ((FavoriteAdapter) rcvFavorite.getAdapter()).getItem(viewHolder.getAdapterPosition()).getName();
            adapter.removeItem(index);
            new Database(FavoriteList.this).removeFromFavorite(Global.activeUser.getPhone(), deleted_food.getFood_id());
            loadFavorite();

            // Snackbar
            Snackbar snackbar = Snackbar.make(root_layout, name_food + " was removed from your favorite !", Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(Color.GREEN);

            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.restoreItem(deleted_food, index);
                    new Database(FavoriteList.this).addToFavorite(deleted_food);
                    loadFavorite();
                }
            });

            snackbar.show();
        }
    }
}

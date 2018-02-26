package pht.eatit.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import pht.eatit.R;
import pht.eatit.onclick.ItemClickListener;

public class FavoriteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public RelativeLayout row_background;
    public LinearLayout row_foreground;
    public TextView name_food, price_food;
    public ImageView image_food, image_favorite, image_share, image_cart;

    private ItemClickListener itemClickListener;

    public FavoriteViewHolder(View view) {
        super(view);
        row_background = view.findViewById(R.id.row_background);
        row_foreground = view.findViewById(R.id.row_foreground);
        image_food = view.findViewById(R.id.image_food);
        name_food = view.findViewById(R.id.name_food);
        price_food = view.findViewById(R.id.price_food);
        image_share = view.findViewById(R.id.image_share);
        image_cart = view.findViewById(R.id.image_cart);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}
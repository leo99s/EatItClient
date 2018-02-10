package pht.eatit.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import pht.eatit.R;
import pht.eatit.onclick.ItemClickListener;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView name_food, price_food;
    public ImageView image_food, image_favorite, image_share;

    private ItemClickListener itemClickListener;

    public FoodViewHolder(View view) {
        super(view);
        image_food = view.findViewById(R.id.image_food);
        name_food = view.findViewById(R.id.name_food);
        price_food = view.findViewById(R.id.price_food);
        image_favorite = view.findViewById(R.id.image_favorite);
        image_share = view.findViewById(R.id.image_share);
        view.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}
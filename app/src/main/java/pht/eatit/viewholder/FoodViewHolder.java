package pht.eatit.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import pht.eatit.R;
import pht.eatit.onclick.ItemClickListener;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView name_food;
    public ImageView image_food;

    private ItemClickListener itemClickListener;

    public FoodViewHolder(View view) {
        super(view);
        name_food = view.findViewById(R.id.name_food);
        image_food = view.findViewById(R.id.image_food);
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
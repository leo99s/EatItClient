package pht.eatit.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import pht.eatit.R;
import pht.eatit.onclick.ItemClickListener;

public class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView name_category;
    public ImageView image_category;

    private ItemClickListener itemClickListener;

    public CategoryViewHolder(View view) {
        super(view);
        name_category = view.findViewById(R.id.name_category);
        image_category = view.findViewById(R.id.image_category);
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
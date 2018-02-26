package pht.eatit.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import pht.eatit.R;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    public RelativeLayout row_background;
    public LinearLayout row_foreground;
    public ImageView image_food;
    public TextView name_food, price_food;
    public ElegantNumberButton quantity_food;

    public OrderViewHolder(View view) {
        super(view);
        row_background = view.findViewById(R.id.row_background);
        row_foreground = view.findViewById(R.id.row_foreground);
        image_food = view.findViewById(R.id.image_food);
        name_food = view.findViewById(R.id.name_food);
        price_food = view.findViewById(R.id.price_food);
        quantity_food = view.findViewById(R.id.quantity_food);
    }
}
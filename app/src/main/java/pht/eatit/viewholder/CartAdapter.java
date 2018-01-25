package pht.eatit.viewholder;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import pht.eatit.R;
import pht.eatit.model.Order;
import pht.eatit.onclick.ItemClickListener;

class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView name_food, price_food;
    public ImageView quantity_food;

    private ItemClickListener itemClickListener;

    public CardViewHolder(View view) {
        super(view);
        name_food = view.findViewById(R.id.name_food);
        price_food = view.findViewById(R.id.price_food);
        quantity_food = view.findViewById(R.id.quantity_food);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {

    }
}

public class CartAdapter extends RecyclerView.Adapter<CardViewHolder> {

    private Context context;
    private List<Order> orderList = new ArrayList<>();

    public CartAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_cart, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        holder.name_food.setText(orderList.get(position).getName());

        Locale locale = new Locale("en", "US");
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
        int price = Integer.parseInt(orderList.get(position).getPrice()) * Integer.parseInt(orderList.get(position).getQuantity());
        holder.price_food.setText(numberFormat.format(price));

        TextDrawable drawable = TextDrawable.builder().buildRound(orderList.get(position).getQuantity(), Color.RED);
        holder.quantity_food.setImageDrawable(drawable);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }
}
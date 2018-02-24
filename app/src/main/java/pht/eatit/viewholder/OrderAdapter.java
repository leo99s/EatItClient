package pht.eatit.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.squareup.picasso.Picasso;
import pht.eatit.Cart;
import pht.eatit.R;
import pht.eatit.database.Database;
import pht.eatit.model.Order;

class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

    public ImageView image_food;
    public TextView name_food, price_food;
    public ElegantNumberButton quantity_food;

    public OrderViewHolder(View view) {
        super(view);
        image_food = view.findViewById(R.id.image_food);
        name_food = view.findViewById(R.id.name_food);
        price_food = view.findViewById(R.id.price_food);
        quantity_food = view.findViewById(R.id.quantity_food);
        view.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select an action");
        menu.add(0, 0, getAdapterPosition(), "Delete");
    }
}

public class OrderAdapter extends RecyclerView.Adapter<OrderViewHolder> {

    private Cart cart;
    private List<Order> orderList = new ArrayList<>();

    public OrderAdapter(Cart cart, List<Order> orderList) {
        this.cart = cart;
        this.orderList = orderList;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(cart);
        View view = inflater.inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final OrderViewHolder holder, final int position) {
        Picasso.with(cart).load(orderList
                .get(position)
                .getImage())
                .resize(70, 70)
                .centerCrop()
                .into(holder.image_food);

        holder.name_food.setText(orderList.get(position).getName());

        Locale locale = new Locale("en", "US");
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
        int price = Integer.parseInt(orderList.get(position).getPrice()) * Integer.parseInt(orderList.get(position).getQuantity());
        holder.price_food.setText(numberFormat.format(price));

        //TextDrawable drawable = TextDrawable.builder().buildRound(orderList.get(position).getQuantity(), Color.GREEN);
        //holder.quantity_food.setImageDrawable(drawable);
        holder.quantity_food.setNumber(orderList.get(position).getQuantity());
        holder.quantity_food.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order = orderList.get(position);
                order.setQuantity(String.valueOf(newValue));
                new Database(cart).updateOrder(order);

                // Calculate total price
                int total = 0;

                for (Order item : orderList){
                    total += Integer.parseInt(item.getPrice()) * Integer.parseInt(item.getQuantity());
                }

                Locale locale = new Locale("en", "US");
                NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
                cart.total_price.setText(numberFormat.format(total));
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }
}
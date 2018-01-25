package pht.eatit.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import pht.eatit.R;
import pht.eatit.onclick.ItemClickListener;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView id_order, phone_order, address_order, status_order;

    private ItemClickListener itemClickListener;

    public OrderViewHolder(View view) {
        super(view);
        id_order = view.findViewById(R.id.id_order);
        phone_order = view.findViewById(R.id.phone_order);
        address_order = view.findViewById(R.id.address_order);
        status_order = view.findViewById(R.id.status_order);

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
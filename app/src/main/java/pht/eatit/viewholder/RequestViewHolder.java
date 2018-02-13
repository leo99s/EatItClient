package pht.eatit.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import pht.eatit.R;
import pht.eatit.onclick.ItemClickListener;

public class RequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView id_request, phone_request, address_request, status_request;

    private ItemClickListener itemClickListener;

    public RequestViewHolder(View view) {
        super(view);
        id_request = view.findViewById(R.id.id_request);
        phone_request = view.findViewById(R.id.phone_request);
        address_request = view.findViewById(R.id.address_request);
        status_request = view.findViewById(R.id.status_request);

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
package pht.eatit.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import pht.eatit.R;

public class RequestViewHolder extends RecyclerView.ViewHolder {

    public TextView id_request, phone_request, address_request, delivery_status_request;

    public RequestViewHolder(View view) {
        super(view);
        id_request = view.findViewById(R.id.id_request);
        phone_request = view.findViewById(R.id.phone_request);
        address_request = view.findViewById(R.id.address_request);
        delivery_status_request = view.findViewById(R.id.delivery_status_request);
    }
}
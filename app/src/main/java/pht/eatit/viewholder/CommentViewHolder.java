package pht.eatit.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import pht.eatit.R;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    public TextView txtPhone, txtComment;
    public RatingBar rating_bar;

    public CommentViewHolder(View view) {
        super(view);
        txtPhone = view.findViewById(R.id.txtPhone);
        txtComment = view.findViewById(R.id.txtComment);
        rating_bar = view.findViewById(R.id.rating_bar);
    }
}
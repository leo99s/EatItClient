package pht.eatit.onclick;

import android.support.v7.widget.RecyclerView;

public interface ItemSwipeListener {

    void onSwipe(RecyclerView.ViewHolder viewHolder, int position, int direction);
} 
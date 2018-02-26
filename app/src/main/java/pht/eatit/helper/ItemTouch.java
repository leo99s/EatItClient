package pht.eatit.helper;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import pht.eatit.onclick.ItemSwipeListener;
import pht.eatit.viewholder.FavoriteViewHolder;
import pht.eatit.viewholder.OrderViewHolder;

public class ItemTouch extends ItemTouchHelper.SimpleCallback {

    private ItemSwipeListener itemSwipeListener;

    public ItemTouch(int dragDirs, int swipeDirs, ItemSwipeListener itemSwipeListener) {
        super(dragDirs, swipeDirs);
        this.itemSwipeListener = itemSwipeListener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if(itemSwipeListener != null){
            itemSwipeListener.onSwipe(viewHolder, viewHolder.getAdapterPosition(), direction);
        }
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if(viewHolder instanceof OrderViewHolder) {
            View row_foreground = ((OrderViewHolder) viewHolder).row_foreground;
            getDefaultUIUtil().clearView(row_foreground);
        } else if(viewHolder instanceof FavoriteViewHolder) {
            View row_foreground = ((FavoriteViewHolder) viewHolder).row_foreground;
            getDefaultUIUtil().clearView(row_foreground);
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (viewHolder instanceof OrderViewHolder) {
            View row_foreground = ((OrderViewHolder) viewHolder).row_foreground;
            getDefaultUIUtil().onDraw(c, recyclerView, row_foreground, dX, dY, actionState, isCurrentlyActive);
        } else if(viewHolder instanceof FavoriteViewHolder) {
            View row_foreground = ((FavoriteViewHolder) viewHolder).row_foreground;
            getDefaultUIUtil().onDraw(c, recyclerView, row_foreground, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (viewHolder instanceof OrderViewHolder) {
            View row_foreground = ((OrderViewHolder) viewHolder).row_foreground;
            getDefaultUIUtil().onDrawOver(c, recyclerView, row_foreground, dX, dY, actionState, isCurrentlyActive);
        } else if(viewHolder instanceof FavoriteViewHolder) {
            View row_foreground = ((FavoriteViewHolder) viewHolder).row_foreground;
            getDefaultUIUtil().onDrawOver(c, recyclerView, row_foreground, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if(viewHolder != null){
            if (viewHolder instanceof OrderViewHolder) {
                View row_foreground = ((OrderViewHolder) viewHolder).row_foreground;
                getDefaultUIUtil().onSelected(row_foreground);
            } else if(viewHolder instanceof FavoriteViewHolder) {
                View row_foreground = ((FavoriteViewHolder) viewHolder).row_foreground;
                getDefaultUIUtil().onSelected(row_foreground);
            }
        }
    }
}
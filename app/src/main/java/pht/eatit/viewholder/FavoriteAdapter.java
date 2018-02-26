package pht.eatit.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.util.List;
import pht.eatit.FoodDetail;
import pht.eatit.R;
import pht.eatit.database.Database;
import pht.eatit.global.Global;
import pht.eatit.model.Favorite;
import pht.eatit.model.Order;
import pht.eatit.onclick.ItemClickListener;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteViewHolder> {

    private Context context;
    private List<Favorite> favoriteList;

    // Share to Facebook
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    // Create target from Picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // Create image from bitmap
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap).build();

            if(ShareDialog.canShow(SharePhotoContent.class)){
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo).build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    public FavoriteAdapter(Context context, List<Favorite> favoriteList) {
        this.context = context;
        this.favoriteList = favoriteList;

        // Init Facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog((Activity) context);
    }

    @Override
    public FavoriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_favorite, parent, false);

        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FavoriteViewHolder holder, final int position) {
        holder.name_food.setText(favoriteList.get(position).getName());
        holder.price_food.setText(String.format("$ %s", favoriteList.get(position).getPrice()));
        Picasso.with(context).load(favoriteList.get(position).getImage()).into(holder.image_food);

        // Click to share to Facebook
        holder.image_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Picasso.with(context.getApplicationContext()).load(favoriteList.get(position).getImage()).into(target);
            }
        });

        // Quick cart
        holder.image_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean isFoodExisted = new Database(context).isFoodExisted(Global.activeUser.getPhone(), favoriteList.get(position).getFood_id());

                if (!isFoodExisted) {
                    new Database(context).addOrder(new Order(
                            Global.activeUser.getPhone(),
                            favoriteList.get(position).getFood_id(),
                            favoriteList.get(position).getImage(),
                            favoriteList.get(position).getName(),
                            favoriteList.get(position).getPrice(),
                            "1",
                            favoriteList.get(position).getDiscount()
                    ));
                } else {
                    new Database(context).increaseOrder(
                            Global.activeUser.getPhone(),
                            favoriteList.get(position).getFood_id());
                }

                Toast.makeText(context, "Added to your cart !", Toast.LENGTH_SHORT).show();
            }
        });

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                Intent foodDetail = new Intent(context, FoodDetail.class);
                foodDetail.putExtra("food_id", favoriteList.get(position).getFood_id());
                context.startActivity(foodDetail);
            }
        });
    }

    public Favorite getItem(int position){
        return favoriteList.get(position);
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public void removeItem(int position){
        favoriteList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Favorite item, int position){
        favoriteList.add(position, item);
        notifyItemInserted(position);
    }
}
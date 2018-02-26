package pht.eatit.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import java.util.ArrayList;
import java.util.List;

import pht.eatit.model.Favorite;
import pht.eatit.model.Order;

public class Database extends SQLiteAssetHelper {

    private static final String DB_NAME = "EatIt.db";
    private static final int DB_VERSION = 1;

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Load all orders
    public List<Order> loadOrder(String phone){
        final List<Order> orderList = new ArrayList<>();

        SQLiteDatabase database = getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String table = "OrderDetail";
        String[] columns = { "phone", "food_id", "image", "name", "price", "quantity", "discount" };

        queryBuilder.setTables(table);
        Cursor cursor = queryBuilder.query(database, columns, "phone=?", new String[] { phone }, null, null, null);

        if(cursor.moveToFirst()){
            do {
                orderList.add(new Order(
                        cursor.getString(cursor.getColumnIndex("phone")),
                        cursor.getString(cursor.getColumnIndex("food_id")),
                        cursor.getString(cursor.getColumnIndex("image")),
                        cursor.getString(cursor.getColumnIndex("name")),
                        cursor.getString(cursor.getColumnIndex("price")),
                        cursor.getString(cursor.getColumnIndex("quantity")),
                        cursor.getString(cursor.getColumnIndex("discount"))));
            } while (cursor.moveToNext());
        }

        return orderList;
    }

    // Add a new order
    public void addOrder(Order order){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("INSERT OR REPLACE INTO OrderDetail(phone, food_id, image, name, price, quantity, discount) VALUES('%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                order.getPhone(),
                order.getFood_id(),
                order.getImage(),
                order.getName(),
                order.getPrice(),
                order.getQuantity(),
                order.getDiscount());

        database.execSQL(query);
    }

    // Remove a food from the cart
    public void deleteOrder(String phone, String food_id){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail WHERE phone = '%s' AND food_id = '%s';", phone, food_id);
        database.execSQL(query);
    }

    // Clear all orders from the cart
    public void clearCart(String phone){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail WHERE phone = '%s';", phone);
        database.execSQL(query);
    }

    // Count the number of orders
    public int getCartCount(String phone) {
        int count = 0;
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("SELECT COUNT (*) FROM OrderDetail WHERE phone = '%s';", phone);
        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            do {
                count = cursor.getInt(0);
            } while(cursor.moveToNext());
        }

        cursor.close();
        return count;
    }

    // Increase quantity of a food to 1
    public void increaseOrder(String phone, String food_id){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("UPDATE OrderDetail SET quantity = quantity + 1 WHERE phone = '%s' AND food_id = '%s';", phone, food_id);
        database.execSQL(query);
    }

    // Change quantity of a food
    public void updateOrder(Order order) {
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("UPDATE OrderDetail SET quantity = '%s' WHERE phone = '%s' AND food_id = '%s';", order.getQuantity(), order.getPhone(), order.getFood_id());
        database.execSQL(query);
    }

    // Check if a food existed in the cart
    public boolean isFoodExisted(String phone, String food_id){
        boolean flag = false;
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("SELECT * FROM OrderDetail WHERE phone = '%s' AND food_id = '%s';", phone, food_id);
        Cursor cursor = database.rawQuery(query, null);

        if(cursor.getCount() > 0){
            flag = true;
        } else {
            flag = false;
        }

        cursor.close();
        return flag;
    }



    // Load all favorites
    public List<Favorite> loadFavorite(String phone){
        final List<Favorite> favoriteList = new ArrayList<>();

        SQLiteDatabase database = getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String table = "Favorite";
        String[] columns = { "phone", "food_id", "category_id", "name", "image", "description", "price", "discount" };

        queryBuilder.setTables(table);
        Cursor cursor = queryBuilder.query(database, columns, "phone=?", new String[] { phone }, null, null, null);

        if(cursor.moveToFirst()){
            do {
                favoriteList.add(new Favorite(
                        cursor.getString(cursor.getColumnIndex("phone")),
                        cursor.getString(cursor.getColumnIndex("food_id")),
                        cursor.getString(cursor.getColumnIndex("category_id")),
                        cursor.getString(cursor.getColumnIndex("name")),
                        cursor.getString(cursor.getColumnIndex("image")),
                        cursor.getString(cursor.getColumnIndex("description")),
                        cursor.getString(cursor.getColumnIndex("price")),
                        cursor.getString(cursor.getColumnIndex("discount"))));
            } while (cursor.moveToNext());
        }

        return favoriteList;
    }

    // Is favorite
    public boolean isFavorite(String phone, String food_id){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("SELECT * FROM Favorite WHERE phone = '%s' AND food_id = '%s';", phone, food_id);
        Cursor cursor = database.rawQuery(query, null);

        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }

        cursor.close();
        return true;
    }

    // Add to favorite
    public void addToFavorite(Favorite item){
        SQLiteDatabase database = getReadableDatabase();

        String query = String.format("INSERT INTO Favorite (phone, food_id, category_id, name, image, description, price, discount) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                item.getPhone(),
                item.getFood_id(),
                item.getCategory_id(),
                item.getName(),
                item.getImage(),
                item.getDescription(),
                item.getPrice(),
                item.getDiscount());

        database.execSQL(query);
    }

    // Remove from favorite
    public void removeFromFavorite(String phone, String food_id){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("DELETE FROM Favorite WHERE phone = '%s' AND food_id = '%s';", phone, food_id);
        database.execSQL(query);
    }
}
package pht.eatit.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import java.util.ArrayList;
import java.util.List;
import pht.eatit.model.Order;

public class Database extends SQLiteAssetHelper {

    private static final String DB_NAME = "EatIt_DB.db";
    private static final int DB_VERSION = 1;

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public List<Order> loadOrder(){
        final List<Order> orderList = new ArrayList<>();

        SQLiteDatabase database = getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String table = "OrderDetail";
        String[] column = { "id", "food_id", "name", "price", "quantity", "discount" };

        queryBuilder.setTables(table);
        Cursor cursor = queryBuilder.query(database, column, null, null, null, null, null);

        if(cursor.moveToFirst()){
            do {
                orderList.add(new Order(
                        cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("food_id")),
                        cursor.getString(cursor.getColumnIndex("name")),
                        cursor.getString(cursor.getColumnIndex("price")),
                        cursor.getString(cursor.getColumnIndex("quantity")),
                        cursor.getString(cursor.getColumnIndex("discount"))));
            } while (cursor.moveToNext());
        }

        return orderList;
    }

    public void addOrder(Order order){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("INSERT INTO OrderDetail(food_id, name, price, quantity, discount) VALUES('%s', '%s', '%s', '%s', '%s');",
                order.getFood_ID(),
                order.getName(),
                order.getPrice(),
                order.getQuantity(),
                order.getDiscount());

        database.execSQL(query);
    }

    public void clearCart(){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail");
        database.execSQL(query);
    }

    // Is favorite
    public boolean isFavorite(String food_id){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("SELECT * FROM Favorite WHERE food_id = '%s';", food_id);
        Cursor cursor = database.rawQuery(query, null);

        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }

        cursor.close();
        return true;
    }

    // Add to favorite
    public void addToFavorite(String food_id){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("INSERT INTO Favorite (food_id) VALUES ('%s');", food_id);
        database.execSQL(query);
    }

    // Delete from favorite
    public void deleteFromFavorite(String food_id){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("DELETE FROM Favorite WHERE food_id = '%s';", food_id);
        database.execSQL(query);
    }

    public int getCartCount() {
        int count = 0;
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("SELECT COUNT(*) FROM OrderDetail");
        Cursor cursor = database.rawQuery(query, null);

        if(cursor.moveToFirst()){
            do {
                count = cursor.getInt(0);
            } while(cursor.moveToNext());
        }

        cursor.close();
        return count;
    }

    public void updateOrder(Order order) {
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("UPDATE OrderDetail SET Quantity = %s WHERE id = %d", order.getQuantity(), order.getID());
        database.execSQL(query);
    }
}
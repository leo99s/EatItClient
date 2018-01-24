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
        String[] column = { "Food_ID", "Name", "Price", "Quantity", "Discount" };

        queryBuilder.setTables(table);
        Cursor cursor = queryBuilder.query(database, column, null, null, null, null, null);

        if(cursor.moveToFirst()){
            do {
                orderList.add(new Order(cursor.getString(cursor.getColumnIndex("Food_ID")),
                        cursor.getString(cursor.getColumnIndex("Name")),
                        cursor.getString(cursor.getColumnIndex("Price")),
                        cursor.getString(cursor.getColumnIndex("Quantity")),
                        cursor.getString(cursor.getColumnIndex("Discount"))));
            } while (cursor.moveToNext());
        }

        return orderList;
    }

    public void addToCart(Order order){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("INSERT INTO OrderDetail(Food_ID, Name, Price, Quantity, Discount) VALUES('%s', '%s', '%s', '%s', '%s');",
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
} 
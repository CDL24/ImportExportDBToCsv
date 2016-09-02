package database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by C.limbachiya on 9/2/2016.
 */
public class DBController extends SQLiteOpenHelper {

    private static final String LOGCAT = null;

    // Database Name
    public static final String DATABASE_NAME = "PrdouctDB.db";

    public DBController(Context applicationcontext) {
        super(applicationcontext, DATABASE_NAME, null, 1);  // creating DATABASE
        Log.d(LOGCAT, "Created");
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String query;
        query = "CREATE TABLE IF NOT EXISTS proinfo ( Id INTEGER PRIMARY KEY, Company TEXT,Name TEXT,Price TEXT)";
        database.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i1) {
        String query;
        query = "DROP TABLE IF EXISTS proinfo";
        database.execSQL(query);
        onCreate(database);
    }

    public ArrayList<HashMap<String, String>> getAllProducts() {
        ArrayList<HashMap<String, String>> proList;
        proList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM proinfo";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                //Id, Company,Name,Price
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Id", cursor.getString(0));
                map.put("Company", cursor.getString(1));
                map.put("Name", cursor.getString(2));
                map.put("Price", cursor.getString(3));
                proList.add(map);
            } while (cursor.moveToNext());
        }

        return proList;
    }
}

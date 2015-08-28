package scps.nyu.edu.nycrealestate;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// This class stores the methods for creating the SQLite database
// and for selecting data to load/update in the SQLite database
public class SQLHelper extends SQLiteOpenHelper {

    private String databaseName;
    private final String tableName = "ZIPCODE_STATS";

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    public SQLHelper(Context context, String databaseName) {
        super(context, databaseName, null, 2);
        this.databaseName = databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    // this method only fires when the database is missing
    @Override
    public void onCreate(SQLiteDatabase db) {

        String[] statements = {
                "CREATE TABLE " + tableName + " ("
                        + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "ZIPCODE INTEGER,"
                        + "NBRBEDROOMS INTEGER,"
                        + "AVGPRICE REAL,"
                        + "AVGSQFT REAL,"
                        + "AVGWOM REAL"
                        + ");",
        };

        for (String statement: statements) {
            Log.d("sql", "" + statement);
            db.execSQL(statement);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // This method gets the cursor for loading/saving data
    public Cursor getCursor(String zipCode, int nbrBedrooms) {
        SQLiteDatabase db = getReadableDatabase(); // the db passed to onCreate
        //can say "_id, name" instead of "*", but _id must be included.
        Cursor cursor;
        if (nbrBedrooms > 0 ) {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE ZIPCODE = '" + zipCode + "' AND NBRBEDROOMS = '" + nbrBedrooms + "'  ORDER BY _id;", null);
        } else {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE ZIPCODE = '" + zipCode + "'  ORDER BY _id;", null);
        }
        return cursor;
    }
}


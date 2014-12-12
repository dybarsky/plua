package dmax.words.persist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 10.12.14 at 15:26
 */
public class DataBaseHelper extends SQLiteOpenHelper implements Constants {

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_UKRAINIAN_TABLE);
        db.execSQL(SQL_CREATE_POLISH_TABLE);
        db.execSQL(SQL_CREATE_LINK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

package my.edu.utar.individualassignment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SQLiteAdapter {

    private static final String MYDATABASE_NAME = "HISTORY";
    private static final String DATABASE_TABLE = "MY_TABLE"; //table
    public static final String ID="ID";
    public static final String KEY_CONTENT = "Name"; //column
    public static final String VALUE = "Amount";
    public static final String KEY_CONTENT_2 = "Date";
    private static final int MYDATABASE_VERSION = 1; //version

    private static final String SCRIPT_CREATE_DATABASE =
            "create table " + DATABASE_TABLE +
                    "(" +
                    ID + " integer PRIMARY KEY AUTOINCREMENT, " +
                    KEY_CONTENT + " text not null, " +
                    VALUE + " float, " +
                    KEY_CONTENT_2 + " text)";


    private Context context;
    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;

    public SQLiteAdapter(Context c)
    {
        context = c;
    }

    public SQLiteAdapter openToWrite() throws android.database.SQLException{

        sqLiteHelper = new SQLiteHelper(context,MYDATABASE_NAME,null,MYDATABASE_VERSION);

        // open to write
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();

        return this;
    }

    //open the database to read
    public  SQLiteAdapter openToRead() throws android.database.SQLException{

        sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME,null,MYDATABASE_VERSION);

        // open to read
        sqLiteDatabase = sqLiteHelper.getReadableDatabase();

        return this;
    }

    private String CurrentTime(){
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault());
            Date date = new Date();
            return dateFormat.format(date);

    }

    public  void Result(String name, double amount){
        ContentValues result = new ContentValues();
        result.put(KEY_CONTENT,name);
        result.put(VALUE,amount);
        result.put(KEY_CONTENT_2,CurrentTime());
        sqLiteDatabase.insert(DATABASE_TABLE, null, result);

    }

    // retrieve the data from the table
    public Cursor queueAll(){

        Cursor cursor = sqLiteDatabase.query(DATABASE_TABLE,null,null,
                null,null,null,KEY_CONTENT_2 + " DESC");

        return cursor;
    }

    public void clearAll() {
        sqLiteDatabase.delete(DATABASE_TABLE, null, null);
    }

    public void close(){
        sqLiteHelper.close();
    }

    public class SQLiteHelper extends SQLiteOpenHelper {

        //constructor with 4 parameters
        public SQLiteHelper(@Nullable Context context, @Nullable String name,
                            @Nullable SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        //to create the database
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(SCRIPT_CREATE_DATABASE);
        }

        //version control
        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

            sqLiteDatabase.execSQL(SCRIPT_CREATE_DATABASE);

        }
    }

}

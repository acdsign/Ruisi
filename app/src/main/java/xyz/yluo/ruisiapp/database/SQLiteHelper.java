package xyz.yluo.ruisiapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by free2 on 16-5-20.
 * 数据库操作类
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "xidianrs.db";

    //更改版本后数据库将重新创建
    private static final int DATABASE_VERSION = 4;


    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);//继承父类
    }


    /**
     * 该函数是在第一次创建数据库时执行，只有当其调用getreadabledatebase()
     */
    public void onCreate(SQLiteDatabase db) {

        /**
         * 浏览历史表
         */
        String sql = "CREATE TABLE " + MyDB.TABLE_READ_HISTORY + "("
                + "tid VARCHAR(10) primary key,"
                + "title VARCHAR(150) NOT NULL,"
                + "author VARCHAR(15) NOT NULL,"
                + "read_time DATETIME NOT NULL"
                + ")";
        db.execSQL(sql);
        Log.e("DATABASE", "TABLE_READ_HISTORY数据表创建成功");


        /**
         *  板块列表
         */
        String sql2 = "CREATE TABLE " + MyDB.TABLE_FORUM_LIST + "("
                + "name VARCHAR(20) primary key,"
                + "fid INT,"
                + "todayNew VARCHAR(5),"
                + "isHeader INT NOT NULL"
                + ")";
        db.execSQL(sql2);
        Log.e("DATABASE", "TABLE_FORUM_LIST数据表创建成功");

        /**
         *  板块收藏列表
         */
        String sql3 = "CREATE TABLE " + MyDB.TABLE_FORUM_STAR + "("
                + "star_id INTEGER primary key AUTOINCREMENT,"
                + "star_name VARCHAR(20),"
                + "star_fid INT UNIQUE"
                + ")";
        db.execSQL(sql3);
        Log.e("DATABASE", "TABLE_FORUM_STAR数据表创建成功");
    }


    /**
     * 数据库更新函数，当数据库更新时会执行此函数
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + MyDB.TABLE_READ_HISTORY;
        db.execSQL(sql);

        String sql2 = "DROP TABLE IF EXISTS " + MyDB.TABLE_FORUM_LIST;
        db.execSQL(sql2);

        String sql3 = "DROP TABLE IF EXISTS " + MyDB.TABLE_FORUM_STAR;
        db.execSQL(sql3);

        this.onCreate(db);
        Log.e("DATABASE", "数据库已更新");
    }

}
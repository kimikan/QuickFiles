package inc.funnydog.quickfiles.DB;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    
    private static final String DATABASE_NAME = "mymobile.db";
    
    private static final int DATABASE_VERSION = 1;
    
    private Dao<ApkGroup, Integer> apkGroupDao = null;
    
    private RuntimeExceptionDao<ApkGroup, Integer> ApkGroupRuntimeDao = null;
    
    private RuntimeExceptionDao<ApkItem, Integer> ApkItemRuntimeDao = null;
    
    private RuntimeExceptionDao<BookMark, Integer> BookMarkRuntimeDao = null;
    
    private static DatabaseHelper helper = null;
   
    public static void init(Context context) {
        if(helper == null) {
            helper = new DatabaseHelper(context);
        }
    }
    
    public static DatabaseHelper instance() {
        return helper;
    }
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION/*, R.raw.ormlite_config*/);
    }
    
    public DatabaseHelper(Context context, String databaseName,
            CursorFactory factory, int databaseVersion) {
        super(context, databaseName, factory, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
        
        try {
            TableUtils.createTableIfNotExists(connectionSource, ApkItem.class);
            TableUtils.createTableIfNotExists(connectionSource, ApkGroup.class);
            TableUtils.createTableIfNotExists(connectionSource, BookMark.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public int insert(ApkGroup group) {
        return getGroupDataDao().create(group);
    }
    
    public boolean update(ApkGroup group) {
        return getGroupDataDao().update(group) > 0;
    }
    
    public boolean delete(ApkGroup group) {
        return getGroupDataDao().delete(group) > 0;
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2,
            int arg3) {
        try {
            TableUtils.dropTable(connectionSource, ApkGroup.class, true);
            TableUtils.dropTable(connectionSource, ApkItem.class, true);
            TableUtils.dropTable(connectionSource, BookMark.class, true);
            
            // after we drop the old databases, we create the new ones
            onCreate(arg0, connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    
    public Dao<ApkGroup, Integer> getDao() {
        if (apkGroupDao == null) {
            try
            {
                apkGroupDao = getDao(ApkGroup.class);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        
        return apkGroupDao;
    }
    
    public RuntimeExceptionDao<ApkGroup, Integer> getGroupDataDao() {
        if (ApkGroupRuntimeDao == null) {
            ApkGroupRuntimeDao = getRuntimeExceptionDao(ApkGroup.class);
        }
        
        return ApkGroupRuntimeDao;
    }
    
    public RuntimeExceptionDao<ApkItem, Integer> getItemDataDao() {
        if (ApkItemRuntimeDao == null) {
            ApkItemRuntimeDao = getRuntimeExceptionDao(ApkItem.class);
        }
        
        return ApkItemRuntimeDao;
    }
    
    public RuntimeExceptionDao<BookMark, Integer> getBookMarkDataDao() {
        if (BookMarkRuntimeDao == null) {
            BookMarkRuntimeDao = getRuntimeExceptionDao(BookMark.class);
        }
        
        return BookMarkRuntimeDao;
    }
    
    @Override
    public void close() {
        super.close();
        ApkGroupRuntimeDao = null;
        ApkItemRuntimeDao = null;
        BookMarkRuntimeDao = null;
    }
}

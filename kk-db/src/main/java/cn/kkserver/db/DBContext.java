package cn.kkserver.db;

import android.database.Cursor;
import android.database.SQLException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

/**
 * Created by zhanghailong on 2016/11/3.
 */

public interface DBContext extends Closeable{

    public void build(Class<?> tableClass) throws SQLException,DBException;

    public void insert(Object object) throws SQLException,DBException;

    public void update(Object object) throws SQLException,DBException;

    public void update(Object object,Set<String> keys) throws SQLException,DBException;

    public void delete(Object object) throws SQLException,DBException;

    public void delete(Class<?> tableClass,long id) throws SQLException,DBException;

    public Cursor query(Class<?> tableClass,String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit) throws SQLException,DBException;

    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit) throws SQLException,DBException;

    public void execSQL(String sql,Object[] bindArgs) throws SQLException,DBException;

    public void execSQL(String sql) throws SQLException,DBException;

}

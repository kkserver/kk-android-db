package cn.kkserver.db;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Created by zhanghailong on 2016/11/3.
 */

public class DBSQLiteContext extends DBAbstractContext implements DBContext {

    private final SQLiteDatabase _db;
    private boolean _inited;

    public DBSQLiteContext(File file) {
        _db = SQLiteDatabase.openOrCreateDatabase(file,null);
    }

    private final static String DBTypeString(DBField fd) {

        if(fd.type() == DBFieldType.Integer) {
            if(fd.length() == 0) {
                return "INTEGER";
            }
            else {
                return "INTEGER(" +fd.length()+ ")";
            }
        }

        if(fd.type() == DBFieldType.Long) {
            if(fd.length() == 0) {
                return "LONG";
            }
            else {
                return "LONG(" +fd.length()+ ")";
            }
        }

        if(fd.type() == DBFieldType.Double) {
            if(fd.length() == 0) {
                return "DOUBLE";
            }
            else {
                return "DOUBLE(" +fd.length()+ ")";
            }
        }

        if(fd.type() == DBFieldType.Boolean) {
            return "INT(1)";
        }

        if(fd.type() == DBFieldType.String) {
            if(fd.length() == 0) {
                return "VARCHAR(45)";
            }
            else {
                return "VARCHAR(" +fd.length()+ ")";
            }
        }

        if(fd.type() == DBFieldType.Text) {
            if(fd.length() == 0) {
                return "TEXT";
            }
            else {
                return "TEXT(" +fd.length()+ ")";
            }
        }

        if(fd.type() == DBFieldType.LongText) {
            if(fd.length() == 0) {
                return "LONGTEXT";
            }
            else {
                return "LONGTEXT(" +fd.length()+ ")";
            }
        }

        if(fd.type() == DBFieldType.Blob) {
            if(fd.length() == 0) {
                return "BLOB";
            }
            else {
                return "BLOB(" +fd.length()+ ")";
            }
        }

        return "VARCHAR(45)";
    }

    @Override
    public void build(Class<?> tableClass) throws SQLException,DBException {
        super.build(tableClass);

        if(! _inited) {
            _db.execSQL("CREATE TABLE IF NOT EXISTS __scheme (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,name VARCHAR(64) NULL,scheme TEXT NULL)");
            _db.execSQL("CREATE UNIQUE INDEX __scheme  ON name (name ASC)");
            _inited = true;
        }

        DBTableScheme table = getTableScheme(tableClass);

        Cursor rs = _db.query("__scheme",new String[]{"id","scheme"}, "name=?",new String[]{table.name},null,null,null);

        try {

            if (rs.moveToNext()) {

                long id = rs.getLong(0);
                String text = rs.getString(1);
                JSONObject scheme = new JSONObject(text);

                boolean hasChanged = false;

                for(DBTableScheme.DBFieldScheme fd : table.fields) {

                    if(scheme.has(fd.name)) {

                        JSONObject v = scheme.getJSONObject(fd.name);

                        if(v.getInt("length") != fd.dbField.length()
                                || !v.getString("type").equals(fd.dbField.type().toString())) {
                            try {
                                _db.execSQL("ALTER TABLE " + table.name + " MODIFY " + fd.name + " " + DBTypeString(fd.dbField));
                            }
                            catch(SQLException ex) {
                                Log.d(DB.TAG,ex.getMessage(),ex);
                            }
                            hasChanged = true;
                        }

                        if(fd.dbField.index() != DBIndexType.None
                                && ! fd.dbField.index().toString().equals(v.getString("index"))) {
                            if(fd.dbField.unique()) {
                                try {
                                    _db.execSQL("CREATE UNIQUE INDEX " + table.name + " ON " + fd.name + "(" + fd.name + " "+ fd.dbField.index().toString() + ")");
                                }
                                catch(SQLException ex) {
                                    Log.d(DB.TAG,ex.getMessage(),ex);
                                }
                            }
                            else {
                                try {
                                    _db.execSQL("CREATE INDEX " + table.name + " ON " + fd.name + "(" + fd.name + " "+ fd.dbField.index().toString() + ")");
                                }
                                catch(SQLException ex) {
                                    Log.d(DB.TAG,ex.getMessage(),ex);
                                }
                            }
                            hasChanged = true;
                        }

                    }
                    else {
                        try {
                            _db.execSQL("ALTER TABLE " + table.name + " ADD COLUMN " + fd.name + " " + DBTypeString(fd.dbField) + "");
                        }
                        catch(SQLException ex) {
                            Log.d(DB.TAG,ex.getMessage(),ex);
                        }
                        hasChanged = true;

                        if(fd.dbField.index() != DBIndexType.None) {
                            if(fd.dbField.unique()) {
                                try {
                                    _db.execSQL("CREATE UNIQUE INDEX " + table.name + " ON " + fd.name + "(" + fd.name + " "+ fd.dbField.index().toString() + ")");
                                }
                                catch(SQLException ex) {
                                    Log.d(DB.TAG,ex.getMessage(),ex);
                                }
                            }
                            else {
                                try {
                                    _db.execSQL("CREATE INDEX " + table.name + " ON " + fd.name + "(" + fd.name + " "+ fd.dbField.index().toString() + ")");
                                }
                                catch(SQLException ex) {
                                    Log.d(DB.TAG,ex.getMessage(),ex);
                                }
                            }
                            hasChanged = true;
                        }
                    }

                }

                if(hasChanged) {

                    try {
                        _db.execSQL("UPDATE __scheme SET scheme=? WHERE id=?" ,new Object[]{id,table.toString()});
                    }
                    catch(SQLException ex) {
                        Log.d(DB.TAG,ex.getMessage(),ex);
                    }
                }
            }
            else {

                StringBuilder sql = new StringBuilder();
                int i = 0;

                sql.append("CREATE TABLE IF NOT EXISTS ").append(table.name).append("(");

                if(!"".equals(table.dbTable.key())) {
                    sql.append(table.dbTable.key()).append(" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT");
                    i ++;
                }

                for(DBTableScheme.DBFieldScheme fd : table.fields) {

                    if(i != 0) {
                        sql.append(",");
                    }

                    sql.append(fd.name).append(" ").append(DBTypeString(fd.dbField));

                    i ++;

                }

                sql.append(")");

                try {
                    _db.execSQL(sql.toString());
                }
                catch(SQLException ex) {
                    Log.d(DB.TAG,ex.getMessage(),ex);
                }

                for(DBTableScheme.DBFieldScheme fd : table.fields) {

                    if(fd.dbField.index() != DBIndexType.None) {

                        if(fd.dbField.unique()) {
                            try {
                                _db.execSQL("CREATE UNIQUE INDEX " + table.name + " ON " + fd.name + "(" + fd.name + " "+ fd.dbField.index().toString() + ")");
                            }
                            catch(SQLException ex) {
                                Log.d(DB.TAG,ex.getMessage(),ex);
                            }
                        }
                        else {
                            try {
                                _db.execSQL("CREATE INDEX " + table.name + " ON " + fd.name + "(" + fd.name + " "+ fd.dbField.index().toString() + ")");
                            }
                            catch(SQLException ex) {
                                Log.d(DB.TAG,ex.getMessage(),ex);
                            }
                        }
                    }

                }


                try {
                    _db.execSQL("INSERT INTO __scheme(name,scheme) VALUES(?,?)" ,new Object[]{table.name,table.toString()});
                }
                catch(SQLException ex) {
                    Log.d(DB.TAG,ex.getMessage(),ex);
                }

            }

        }
        catch (JSONException ex) {
            throw new DBException(ex.getMessage(),ex);
        }
        finally {
            rs.close();
        }

    }

    @Override
    public void insert(Object object) throws SQLException,DBException {

        DBTableScheme table = getTableScheme(object.getClass());

        StringBuilder sql = new StringBuilder();
        StringBuilder values = new StringBuilder();

        sql.append("INSERT INTO ").append(table.name).append("(");

        values.append(" VALUES(");

        int i = 0;

        for(DBTableScheme.DBFieldScheme fd : table.fields) {

            if(i != 0) {
                sql.append(",");
                values.append(",");
            }

            sql.append(fd.name);
            values.append("?");

            i ++ ;
        }

        values.append(")");

        sql.append(values.toString()).append("");

        SQLiteStatement st = _db.compileStatement(sql.toString());

        i = 1;

        for(DBTableScheme.DBFieldScheme fd : table.fields) {

            Object v;

            try {
                v = fd.field.get(object);
            } catch (IllegalAccessException e) {
                throw new DBException(e.getMessage(),e);
            }

            SQLiteStatementBindObject(st,i,v);

            i ++;
        }

        try {

            long id = st.executeInsert();

            if(table.key != null) {
                try {
                    table.key.field.set(object,id);
                } catch (IllegalAccessException e) {
                    throw new DBException(e.getMessage(),e);
                }
            }

        }
        finally {
            st.close();
        }

    }

    private static final void SQLiteStatementBindObject(SQLiteStatement st,int i,Object v) throws SQLException {
        if(v == null) {
            st.bindNull(i);
        }
        else if(v instanceof Double) {
            st.bindDouble(i,(Double)v);
        }
        else if(v instanceof Float) {
            st.bindDouble(i,((Float)v).doubleValue());
        }
        else if(v instanceof Integer) {
            st.bindLong(i,((Integer) v).longValue());
        }
        else if(v instanceof Long) {
            st.bindLong(i,((Long) v).longValue());
        }
        else if(v instanceof Short) {
            st.bindLong(i,((Short) v).longValue());
        }
        else if(v instanceof byte[]) {
            st.bindBlob(i,(byte[]) v);
        }
        else if(v instanceof String) {
            st.bindString(i,(String)v);
        }
        else {
            st.bindNull(i);
        }
    }

    @Override
    public void update(Object object,Set<String> keys) throws SQLException,DBException {

        DBTableScheme table = getTableScheme(object.getClass());

        if(table.key == null) {
            throw new DBException("Not Found Key " + object.getClass());
        }

        StringBuilder sql = new StringBuilder();

        sql.append("UPDATE ").append(table.name).append(" SET ");

        int i = 0;

        for(DBTableScheme.DBFieldScheme fd : table.fields) {

            if(keys == null || keys.contains(fd.name)) {

                if(i != 0) {
                    sql.append(",");
                }

                sql.append(fd.name).append("=?");

                i ++ ;
            }

        }

        if(i == 0) {
            throw new DBException("Not Found Update Fields");
        }

        sql.append(" WHERE ").append(table.key.name).append("=?");

        SQLiteStatement st = _db.compileStatement(sql.toString());

        i = 1;

        for(DBTableScheme.DBFieldScheme fd : table.fields) {

            if(keys == null || keys.contains(fd.name)) {

                Object v;

                try {
                    v = fd.field.get(object);
                } catch (IllegalAccessException e) {
                    throw new DBException(e.getMessage(),e);
                }

                SQLiteStatementBindObject(st,i,v);

                i ++;

            }

        }

        Object v;

        try {
            v = table.key.field.get(object);
        } catch (IllegalAccessException e) {
            throw new DBException(e.getMessage(),e);
        }

        SQLiteStatementBindObject(st,i,v);

        try {

            st.execute();

        }
        finally {
            st.close();
        }

    }

    @Override
    public void update(Object object) throws SQLException,DBException  {
        update(object,null);
    }

    @Override
    public void delete(Object object) throws SQLException,DBException {

        DBTableScheme table = getTableScheme(object.getClass());

        if(table.key == null) {
            throw new DBException("Not Found Key " + object.getClass());
        }

        StringBuilder sql = new StringBuilder();

        sql.append("DELETE FROM ").append(table.name).append(" WHERE ").append(table.key.name).append("=?");

        Object id;

        try {
            id = table.key.field.get(object);
        } catch (IllegalAccessException e) {
            throw new DBException(e.getMessage(),e);
        }

        _db.execSQL(sql.toString(),new Object[]{id});

    }

    @Override
    public void delete(Class<?> tableClass, long id) throws SQLException,DBException {

        DBTableScheme table = getTableScheme(tableClass);

        if(table.key == null) {
            throw new DBException("Not Found Key " +tableClass);
        }

        StringBuilder sql = new StringBuilder();

        sql.append("DELETE FROM ").append(table.name).append(" WHERE ").append(table.key.name).append("=?");

        _db.execSQL(sql.toString(),new Object[]{id});

    }

    @Override
    public Cursor query(Class<?> tableClass, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) throws SQLException,DBException {

        DBTableScheme table = getTableScheme(tableClass);

        String[] columns = new String[table.fields.length];

        for(int i=0;i<columns.length;i++) {
            columns[i] = table.fields[i].name;
        }

        return query(table.name,columns,selection,selectionArgs,groupBy,having,orderBy,limit);

    }

    @Override
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) throws SQLException {
        return _db.query(table,columns,selection,selectionArgs,groupBy,having,orderBy,limit);
    }

    @Override
    public void execSQL(String sql) throws SQLException {
        _db.execSQL(sql);
    }


    @Override
    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        _db.execSQL(sql,bindArgs);
    }

    @Override
    public void close() throws IOException {
        _db.close();
    }
}

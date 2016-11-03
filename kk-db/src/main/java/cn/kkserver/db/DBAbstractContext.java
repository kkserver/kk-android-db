package cn.kkserver.db;

import android.database.Cursor;
import android.database.SQLException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by zhanghailong on 2016/11/3.
 */

public abstract class DBAbstractContext implements DBContext {

    private final Map<Class<?>,DBTableScheme> _tableSchemes = new HashMap<>();

    public void build(Class<?> tableClass) throws DBException {
        DBTableScheme v = new DBTableScheme(tableClass);
        _tableSchemes.put(tableClass,v);
    }

    @Override
    public DBTableScheme getTableScheme(Class<?> tableClass) throws DBException {
        if(_tableSchemes.containsKey(tableClass)) {
            return _tableSchemes.get(tableClass);
        }
        throw new DBException("Not Found Table Scheme " + tableClass.getName());
    }

    @Override
    public void update(DBObject object,String ... keys) throws SQLException,DBException {
        if(keys == null) {
            update(object);
        }
        else {
            Set<String> v = new TreeSet<>();
            for(String key : keys) {
                v.add(key);
            }
            update(object,v);
        }
    }

    @Override
    public void toObject(Cursor cursor, DBObject object) throws SQLException,DBException {

        DBTableScheme table = getTableScheme(object.getClass());

        int count = cursor.getColumnCount();

        try {

            for(int i=0;i<count;i++) {

                String name = cursor.getColumnName(i);

                DBTableScheme.DBFieldScheme fd = table.field(name);

                if(fd != null) {

                    Class<?> type = fd.field.getType();

                    if(Integer.class == type || int.class == type) {
                        fd.field.set(object,cursor.getInt(i));
                    }
                    else if(Long.class == type || long.class == type) {
                        fd.field.set(object,cursor.getLong(i));
                    }
                    else if(Short.class == type || short.class == type) {
                        fd.field.set(object,cursor.getShort(i));
                    }
                    else if(Float.class == type || float.class == type) {
                        fd.field.set(object,cursor.getFloat(i));
                    }
                    else if(Double.class == type || double.class == type) {
                        fd.field.set(object,cursor.getDouble(i));
                    }
                    else if(Boolean.class == type || boolean.class == type) {
                        fd.field.set(object,cursor.getInt(i) == 1);
                    }
                    else if(String.class == type) {
                        fd.field.set(object,cursor.getString(i));
                    }
                    else if(byte[].class == type) {
                        fd.field.set(object,cursor.getBlob(i));
                    }
                }

            }

        } catch (IllegalAccessException e) {
            throw new DBException(e.getMessage(),e);
        }


    }
}

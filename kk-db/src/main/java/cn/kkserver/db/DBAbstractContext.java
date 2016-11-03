package cn.kkserver.db;

import java.util.TreeMap;

/**
 * Created by zhanghailong on 2016/11/3.
 */

public abstract class DBAbstractContext implements DBContext {

    private final TreeMap<Class<?>,DBTableScheme> _tableSchemes = new TreeMap<>();

    public void build(Class<?> tableClass) throws DBException {
        DBTableScheme v = new DBTableScheme(tableClass);
        _tableSchemes.put(tableClass,v);
    }

    public DBTableScheme getTableScheme(Class<?> tableClass) throws DBException {
        if(_tableSchemes.containsKey(tableClass)) {
            return _tableSchemes.get(tableClass);
        }
        throw new DBException("Not Found Table Scheme " + tableClass.getName());
    }

}

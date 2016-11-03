package cn.kkserver.db;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by zhanghailong on 2016/11/3.
 */

public class DBTableScheme extends Object {

    public final String name;
    public final DBTable dbTable;
    public final Class<?> tableClass;
    public final DBFieldScheme[] fields;
    public final DBFieldScheme key;

    public DBTableScheme(Class<?> tableClass) throws DBException {

        if(DBObject.class.isAssignableFrom(tableClass)) {
            throw new DBException(tableClass.getName() + " Not Implements DBObject");
        }

        this.dbTable = tableClass.getAnnotation(DBTable.class);

        if(dbTable == null) {
            throw new DBException(tableClass.getName() + " Not Implements DBTable");
        }
        this.name = "".equals(dbTable.name()) ? tableClass.getSimpleName().toLowerCase() : dbTable.name();
        this.tableClass = tableClass;

        Set<String> names = new TreeSet<>();

        names.add(dbTable.key());

        List<DBFieldScheme> fds = new ArrayList<>();

        Class<?> clazz = tableClass;

        DBFieldScheme keyv = null;

        while(DBObject.class.isAssignableFrom(clazz)) {

            for(Field fd : tableClass.getFields()) {

                DBField dbField = fd.getAnnotation(DBField.class);

                if(dbField != null) {

                    DBFieldScheme v = new DBFieldScheme(fd,dbField);

                    if(! names.contains(v.name)) {
                        names.add(v.name);
                        fds.add(v);
                    }
                    else if(dbTable.key().equals(v.name)) {
                        keyv = v;
                    }
                }

            }

            clazz = clazz.getSuperclass();
        }

        this.key = keyv;
        this.fields = fds.toArray(new DBFieldScheme[fds.size()]);
    }

    @Override
    public String toString() {

        JSONObject v = new JSONObject();

        try {
            for(DBFieldScheme f: fields) {
                JSONObject vv = new JSONObject();
                vv.put("length",f.dbField.length());
                vv.put("type",f.dbField.type().toString());
                vv.put("index",f.dbField.index().toString());
                vv.put("unique",f.dbField.unique());
                v.put(f.name,vv);
            }
        } catch (JSONException e) {}

        return v.toString();
    }

    public static class DBFieldScheme extends Object {

        public final String name;
        public final Field field;
        public final DBField dbField;

        public DBFieldScheme(Field field,DBField dbField) {
            this.name = "".equals(dbField.name()) ? field.getName().toLowerCase() : dbField.name();
            this.field = field;
            this.dbField = dbField;
        }

    }
}

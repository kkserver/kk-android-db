package cn.kkserver.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zhanghailong on 2016/11/3.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DBField {
    public String name() default "";
    public int length() default 0;
    public DBFieldType type();
    public DBIndexType index() default DBIndexType.None;
    public boolean unique() default false;
}

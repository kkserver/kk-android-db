package cn.kkserver.db;

/**
 * Created by zhanghailong on 2016/11/3.
 */

public class DBException extends Exception {

    public DBException() {
        super();
    }

    public DBException(String detailMessage) {
        super(detailMessage);
    }

    public DBException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}

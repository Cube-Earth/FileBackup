package earth.cube.tools.file_backup.commons;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.rowset.CachedRowSet;

public class CachedRowSetProxy implements java.lang.reflect.InvocationHandler {

    private Object obj;

    public static CachedRowSet newInstance(Object obj) {
        return (CachedRowSet) java.lang.reflect.Proxy.newProxyInstance(
            obj.getClass().getClassLoader(),
            obj.getClass().getInterfaces(),
            new CachedRowSetProxy(obj));
    }

    private CachedRowSetProxy(Object obj) {
        this.obj = obj;
    }

    public Object invoke(Object proxy, Method m, Object[] args)
        throws Throwable
    {
        Object result;
        try {
            if(m.getName().equals("getTimestamp")) {
            	/*
            	double dValue = ((CachedRowSet) proxy).getDouble((String) args[0]);
            	long milliseconds = (long) (dValue * 24 * 60 * 60 * 1000);
            	Time t = new Time(milliseconds);
            	result = t;
            	*/
            	String s = ((CachedRowSet) obj).getString((String) args[0]);
            	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            	Date d = df.parse(s);
            	Timestamp ts = new Timestamp(d.getTime());
            	result = ts;
            }
            else
                result = m.invoke(obj, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}

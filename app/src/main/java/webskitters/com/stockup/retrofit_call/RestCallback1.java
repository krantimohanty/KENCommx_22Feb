package webskitters.com.stockup.retrofit_call;

/**
 * Created by ams on 3/9/15.
 */
public interface RestCallback1<T> {
    public abstract void success(T o);
    public abstract void invalid(T o);
    public abstract void failure();
}
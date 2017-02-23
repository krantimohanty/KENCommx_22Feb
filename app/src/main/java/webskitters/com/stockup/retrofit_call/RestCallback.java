package webskitters.com.stockup.retrofit_call;

/**
 * Created by ams on 3/9/15.
 */
public interface RestCallback<T> {
    public abstract void success(T o);
    public abstract void invalid();
    public abstract void failure();
}
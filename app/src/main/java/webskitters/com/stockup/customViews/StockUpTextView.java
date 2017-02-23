package webskitters.com.stockup.customViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import webskitters.com.stockup.R;


/**
 * Created by Karan on 21-04-2016.
 */
public class StockUpTextView extends TextView {
    public StockUpTextView(Context context) {
        super(context);
    }

    /*public GlobalMileDatingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }*/

   /* private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.ViewStyle);
        String customFont = a.getString(R.styleable.ViewStyle_customFont);
        setCustomFont(ctx, customFont);
        a.recycle();
    }*/

    public boolean setCustomFont(Context ctx, String asset) {
        Typeface tf = null;
        try {
            tf = Typeface.createFromAsset(ctx.getAssets(), asset);
        } catch (Exception e) {
            return false;
        }
        setTypeface(tf);
        return true;
    }
}

package webskitters.com.stockup.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import webskitters.com.stockup.R;

/**
 * Created by android on 8/30/2016.
 */
public class Day2DayCatAdapter extends BaseAdapter {
    private Context mContext;
    ArrayList<String> arrCatName;
    private Typeface typeFaceSegoeuiReg;
    public Day2DayCatAdapter(Context c, ArrayList<String> arrCatName) {
        this.mContext = c;
        this.arrCatName=arrCatName;
        typeFaceSegoeuiReg = Typeface.createFromAsset(mContext.getAssets(),
                "Roboto-Regular.ttf");
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public class ViewHolder{

        ImageView imgCat;
        TextView tvCatName;
    }
    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;

        ViewHolder holder = null;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            convertView = inflater.inflate(R.layout.addview_drink_cat_item, null);
            holder = new ViewHolder();

            holder.tvCatName = (TextView) convertView.findViewById(R.id.tv_cat_name);
            holder.imgCat = (ImageView) convertView.findViewById(R.id.img_cat);

            //holder.imgCat.setLayoutParams(new GridView.LayoutParams(85, 85));
            holder.imgCat.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.imgCat.setPadding(8, 8, 8, 8);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvCatName.setTypeface(typeFaceSegoeuiReg);
        holder.tvCatName.setText(arrCatName.get(position).toString());
        holder.imgCat.setImageResource(mThumbIds[position]);



        return convertView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.day2day_cat_1, R.drawable.day_2_day_corn,
           /* R.drawable.champagne, R.drawable.champagne,
            R.drawable.champagne, R.drawable.champagne,
            R.drawable.champagne, R.drawable.champagne,
            R.drawable.champagne,*/
    };
}

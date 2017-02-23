package webskitters.com.stockup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import webskitters.com.stockup.R;

/**
 * Created by android on 8/30/2016.
 */
public class TipCatSpinnerAdapter extends BaseAdapter {
    private Context mContext;
    ArrayList<String> arrCatName;

    public TipCatSpinnerAdapter(Context c, ArrayList<String> arrCatName) {
        this.mContext = c;
        this.arrCatName=arrCatName;
    }

    public int getCount() {
        return arrCatName.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public class ViewHolder{

        TextView tvCatName;
    }
    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;

        ViewHolder holder = null;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            convertView = inflater.inflate(R.layout.addview_drink_cat_spinner_item, null);
            holder = new ViewHolder();

            holder.tvCatName = (TextView) convertView.findViewById(R.id.tv_cat_name);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvCatName.setText(arrCatName.get(position));


        return convertView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.champagne, R.drawable.champagne,
            R.drawable.champagne, R.drawable.champagne,
            R.drawable.champagne, R.drawable.champagne,
            R.drawable.champagne, R.drawable.champagne,
            R.drawable.champagne,
    };
}

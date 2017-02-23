package webskitters.com.stockup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import webskitters.com.stockup.R;
import webskitters.com.stockup.model.CardDetails;

/**
 * Created by webskitters on 11/4/2016.
 */

public class SavedCardAdapter extends BaseAdapter {
    private Context mContext;
    List<CardDetails> arrCatName;

    public SavedCardAdapter(Context c, List<CardDetails> arrCatName) {
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

        TextView txt_card_no, txt_card_exp, txt_card_nick;
        RelativeLayout rel_parent;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;

        ViewHolder holder = null;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            convertView = inflater.inflate(R.layout.listitem_payment_list, null);
            holder = new ViewHolder();
            holder.rel_parent=(RelativeLayout)convertView.findViewById(R.id.rel_parent);
            holder.txt_card_no = (TextView) convertView.findViewById(R.id.txt_card_no);
            holder.txt_card_exp = (TextView) convertView.findViewById(R.id.txt_card_exp);
            holder.txt_card_nick = (TextView) convertView.findViewById(R.id.txt_card_nick);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txt_card_no.setText("Card Number : ************"+arrCatName.get(position).getCardLastFour());
        holder.txt_card_exp.setText("Expiry Date : "+arrCatName.get(position).getExpire());
        holder.txt_card_nick.setText(arrCatName.get(position).getCardNickname());


        return convertView;
    }
}

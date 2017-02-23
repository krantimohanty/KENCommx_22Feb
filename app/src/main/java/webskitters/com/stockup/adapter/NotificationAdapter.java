package webskitters.com.stockup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import webskitters.com.stockup.R;
import webskitters.com.stockup.model.Notification;

/**
 * Created by webskitters on 11/7/2016.
 */

public class NotificationAdapter extends BaseAdapter {

    private Context mContext;
    List<Notification> arrNotification;

    public NotificationAdapter(Context c, List<Notification> arrNotification) {
        this.mContext = c;
        this.arrNotification=arrNotification;
    }

    public int getCount() {
        return arrNotification.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public class ViewHolder{
        TextView txt_noti, txt_noti_time;
        ImageView img_stat;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;

        ViewHolder holder = null;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            convertView = inflater.inflate(R.layout.listitem_notification, null);
            holder = new ViewHolder();

            holder.txt_noti = (TextView) convertView.findViewById(R.id.txt_noti);
            holder.txt_noti_time = (TextView) convertView.findViewById(R.id.txt_noti_time);

            holder.img_stat = (ImageView) convertView.findViewById(R.id.img_stat);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.txt_noti.setText(arrNotification.get(position).getMessage());
        holder.txt_noti_time.setText(arrNotification.get(position).getCreatedTime());

        if (arrNotification.get(position).getIsRead().equalsIgnoreCase("1")){
            holder.img_stat.setImageResource(R.drawable.read_noti);
        }
        else {
            holder.img_stat.setImageResource(R.drawable.unread_noti);
        }

        return convertView;
    }
}

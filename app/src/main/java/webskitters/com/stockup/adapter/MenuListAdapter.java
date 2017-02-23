package webskitters.com.stockup.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import webskitters.com.stockup.R;
import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.model.SlideMenuOption;

public class MenuListAdapter extends BaseExpandableListAdapter {

    private SharedPreferences sharedPreferenceUser;
    private Context _context;
    private ArrayList<SlideMenuOption> _listDataHeader; // header titles
    String customer_id="", customer_name="";
    // child data in format of header title, child title

    public MenuListAdapter(Context context, ArrayList<SlideMenuOption> slideMenuOptions) {
        this._context = context;
        this._listDataHeader = slideMenuOptions;
        sharedPreferenceUser=_context.getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
        customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        customer_name=sharedPreferenceUser.getString(Constants.strShPrefUserFname, "");
    }

    @Override
    public SlideMenuOption getChild(int groupPosition, int childPosititon) {
        return _listDataHeader.get(groupPosition).getSlideMenuOptionsChild().get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public class ViewHolder{
        ImageView imgList;
        TextView txt_indicator, lblListHeader,  txt_push_count;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.adapter_menu_child, null);
        }

        TextView txtListChild = (TextView) convertView .findViewById(R.id.txt_name);

        if(_listDataHeader.get(groupPosition).getSlideMenuOptionsChild().get(childPosition).getName().equalsIgnoreCase(Constants.strCatName)){
            txtListChild.setTextColor(Color.parseColor("#048bcd"));
        }else
        {
            txtListChild.setTextColor(Color.BLACK);
        }

        ImageView imgList=(ImageView)convertView.findViewById(R.id.img_menu);
        //imgList.setImageResource(_listDataHeader.get(groupPosition).getSlideMenuOptionsChild().get(childPosition).getImage());
        imgList.setImageResource(mThumbIds[_listDataHeader.get(groupPosition).getSlideMenuOptionsChild().get(childPosition).getId()+2]);
        txtListChild.setText(_listDataHeader.get(groupPosition).getSlideMenuOptionsChild().get(childPosition).getName());
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return (_listDataHeader.get(groupPosition).getSlideMenuOptionsChild())
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView( final int groupPosition, boolean isExpanded,View convertView, ViewGroup parent) {

        ViewHolder holder=null;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.adapter_menu_group, null);

            holder.txt_indicator = (TextView) convertView.findViewById(R.id.txt_indicator);
            holder.lblListHeader = (TextView) convertView.findViewById(R.id.txt_name);
            holder.imgList=(ImageView)convertView.findViewById(R.id.img_menu);
            holder.txt_push_count = (TextView) convertView.findViewById(R.id.txt_push_count);
            convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

        holder.imgList.setImageResource(mThumbIds[groupPosition]);
        holder.lblListHeader.setText(_listDataHeader.get(groupPosition).getName());

        if (_listDataHeader.get(groupPosition).getSlideMenuOptionsChild() != null && _listDataHeader.get(groupPosition).getSlideMenuOptionsChild().size() > 0) {
            holder.txt_indicator.setVisibility(View.VISIBLE);
        } else {
            holder.txt_indicator.setVisibility(View.GONE);
        }

        if(!customer_id.equalsIgnoreCase("")&&_listDataHeader.get(groupPosition).getName().equalsIgnoreCase("Sign In")){
            holder.lblListHeader.setText("Sign Out");
        }

        if(!customer_name.equalsIgnoreCase("")&&_listDataHeader.get(groupPosition).getName().equalsIgnoreCase("Profile")){
            holder.lblListHeader.setText(customer_name);
        }

        if (isExpanded) {
           // txt_indicator.setText("-");
            holder.txt_indicator.setBackgroundResource(R.drawable.minus_icon);


        } else {
            //txt_indicator.setText("+");
            holder.txt_indicator.setBackgroundResource(R.drawable.plus_icon);

        }
        if(!customer_id.equalsIgnoreCase("")&&_listDataHeader.get(groupPosition).getName().equalsIgnoreCase("Notifications")){
            //holder.lblListHeader.setText("Notifications ("+Constants.push_count+")");
            holder.txt_indicator.setVisibility(View.VISIBLE);
            //holder.txt_indicator.setBackgroundResource(R.drawable.minus_icon);
            if(Constants.push_count!=null){
                holder.txt_indicator.setBackgroundResource(R.drawable.noti_red_round);
                holder.txt_indicator.setText(Constants.push_count);
            }else{
                holder.txt_indicator.setBackgroundResource(R.drawable.noti_red_round);
                holder.txt_indicator.setText("0");
            }

            //customCheckBoxTextView(holder.lblListHeader, holder.lblListHeader.getText().toString());

        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {

        return true;
    }
    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.mainstore_icon, R.drawable.profile_icon,
            R.drawable.pastorder_icon,R.drawable.notification_icon,
            R.drawable.cart_icon, R.drawable.promo_icon,
            R.drawable.video_icon,R.drawable.side_menu_rate_us_icon,R.drawable.side_menu_rate_us_icon,
            R.drawable.side_menu_share_icon,R.drawable.support_icon,
            R.drawable.signin_icon, R.drawable.drinks_icon,
            R.drawable.foodnew_s_icon, R.drawable.day_2_day_icon,
            R.drawable.gift_icon, R.drawable.events_icon,
            R.drawable.profile_icon,
            R.drawable.address_icon,
            R.drawable.wishlist_icon, R.drawable.shopinglist_icon,
            R.drawable.pastorder_icon, R.drawable.address_icon,
            R.drawable.serve_icon,R.drawable.faq_icon,
            R.drawable.privacy_policy_icon, R.drawable.terms_icon,
            R.drawable.payment_icon
    };


    private void customCheckBoxTextView(TextView view, String text) {
        String init = "Notifications ";
        String terms = "("+Constants.push_count+")";


        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                init);
        spanTxt.append(terms);


        spanTxt.setSpan(new ForegroundColorSpan(Color.parseColor("#E11349")), init.length(), init.length() + terms.length(), 0);

        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }
    }

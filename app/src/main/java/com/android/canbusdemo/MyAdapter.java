package com.android.canbusdemo;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by zyz on 2020/04/14.
 */
public class MyAdapter extends BaseAdapter {
    private Context mContext = null;
    private List<String> mChannels;
    private List<String> mFormats;
    private List<String> mStartids;
    private List<String> mEndids;

    public MyAdapter(Context context, List<String> channels, List<String> formats, List<String> startids, List<String> endids){
        mContext = context;
        this.mChannels = channels;
        this.mFormats = formats;
        this.mStartids = startids;
        this.mEndids = endids;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mChannels.size();
    }
    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
//        System.out.println("position ------> "+position);
        ViewHolder holder;
        if (null == convertView){
            convertView = View.inflate(mContext, R.layout.filter_list, null);
            holder = new ViewHolder();
            holder.filter_list_channel = (TextView)convertView.findViewById(R.id.filter_list_channel);
            holder.filter_list_format = (TextView)convertView.findViewById(R.id.filter_list_format);
            holder.filter_list_startid = (TextView)convertView.findViewById(R.id.filter_list_startid);
            holder.filter_list_endid = (TextView)convertView.findViewById(R.id.filter_list_endid);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.filter_list_channel.setText(mChannels.get(position));
        holder.filter_list_format.setText(mFormats.get(position));
        holder.filter_list_startid.setText(mStartids.get(position));
        holder.filter_list_endid.setText(mEndids.get(position));
        //设置隔行变色
        int colorPos=position%2;
        if(colorPos==1)
            convertView.setBackgroundColor(Color.argb(250, 255, 255, 255));
        else
            convertView.setBackgroundColor(Color.argb(255, 224, 243, 250));

        return convertView;
    }

    public void remove(int position) {
        mChannels.remove(position);
        mFormats.remove(position);
        mStartids.remove(position);
        mEndids.remove(position);
        notifyDataSetChanged();
    }

    public int add(String channel, String format, String startid, String endid) {
        for (int i=0; i<mChannels.size(); i++){
            if (channel.equals(mChannels.get(i)) && format.equals(mFormats.get(i)) && startid.equals(mStartids.get(i)) && endid.equals(mEndids.get(i))){
                return -1;
            }
        }
        mChannels.add(channel);
        mFormats.add(format);
        mStartids.add(startid);
        mEndids.add(endid);
        notifyDataSetChanged();
        return 0;
    }

    public int add_mask(String channel, String format, String startid, String endid) {
        //System.out.println("zyz->add_mask->endid size= "+mEndids.size()+", str->"+endid);
        mChannels.add(channel);
        mFormats.add(format);
        mStartids.add(startid);
        mEndids.add(endid);
        notifyDataSetChanged();
        return 0;
    }

    private class ViewHolder{
        private TextView filter_list_channel;
        private TextView filter_list_format;
        private TextView filter_list_startid;
        private TextView filter_list_endid;
    }
}

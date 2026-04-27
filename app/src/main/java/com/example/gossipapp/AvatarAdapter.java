package com.example.gossipapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class AvatarAdapter extends BaseAdapter {
    private final Context context;
    private final int[] avatars;
    public AvatarAdapter(Context ctx, int[] avatarsRes) {
        this.context = ctx;
        this.avatars = avatarsRes;
    }
    @Override public int getCount() { return avatars.length; }
    @Override public Object getItem(int position) { return avatars[position]; }
    @Override public long getItemId(int position) { return position; }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView iv = (convertView instanceof ImageView)
                ? (ImageView)convertView
                : new ImageView(context);
        iv.setImageResource(avatars[position]);
        iv.setLayoutParams(new ViewGroup.LayoutParams(
                (int)context.getResources().getDimension(com.intuit.sdp.R.dimen._80sdp),
                (int)context.getResources().getDimension(com.intuit.sdp.R.dimen._80sdp)
        ));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return iv;
    }
}

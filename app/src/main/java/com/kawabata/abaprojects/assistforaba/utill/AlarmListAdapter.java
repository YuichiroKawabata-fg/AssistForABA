package com.kawabata.abaprojects.assistforaba.utill;

import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kawabata.abaprojects.assistforaba.AlarmActivity;
import com.kawabata.abaprojects.assistforaba.R;
import com.kawabata.abaprojects.assistforaba.listcomponent.ListItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class AlarmListAdapter  extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder> {

    private final ArrayList<ListItem> alarmList;
    private final Context context;



    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewAlarmName;
        private final TextView textViewAlarmTime;
        private final ImageView imageView;
        private final ImageView viewIcon;

        public ViewHolder(View view) {
            super(view);
            textViewAlarmName = view.findViewById(R.id.alarm_list_name);
            textViewAlarmTime = view.findViewById(R.id.alarm_list_time);
            imageView = view.findViewById(R.id.listIcon);
            viewIcon = view.findViewById(R.id.viewIcon);
        }

        public TextView getTextViewAlarmName(){
            return textViewAlarmName;
        }
        public TextView getTextViewAlarmTime(){
            return textViewAlarmTime;
        }
        public ImageView getImageView(){
            return imageView;
        }
        public ImageView getViewIcon(){
            return viewIcon;
        }

    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param alarmList ArrayList<ListItem>
     * by RecyclerView.
     */
    public AlarmListAdapter(Context context, ArrayList<ListItem> alarmList) {
        this.alarmList = alarmList;
        this.context = context;
    }


    // タップされたときに呼び出されるメソッドを定義
    protected void onClickedListItem(@NonNull ListItem listItem) {
    }

    // タップされたときに呼び出されるメソッドを定義
    protected void onClickedViewIcon(ListItem listItem) {
    }



    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_item, viewGroup, false);

        final ViewHolder holder = new ViewHolder(view);
        // onCreateViewHolder でリスナーをセット
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = holder.getAbsoluteAdapterPosition();
                onClickedListItem(alarmList.get(position));
            }
        });

        return holder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        ImageController imageController = new ImageController(this.context);
        viewHolder.getTextViewAlarmName().setText(alarmList.get(position).getAlarmName());
        viewHolder.getTextViewAlarmTime().setText(alarmList.get(position).getTime());
        if (alarmList.get(position).getUri() != null) {
            viewHolder.getImageView().setImageBitmap(imageController.getBitmap(Uri.parse(alarmList.get(position).getUri())));
        } else{
            viewHolder.getImageView().setImageBitmap(imageController.getBitmapFromAsset("no_image_square.jpg"));
        }

        //絵カード表示
        viewHolder.getViewIcon().setOnClickListener( v -> {
            onClickedViewIcon(alarmList.get(position));
        });
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return alarmList.size();
    }

}
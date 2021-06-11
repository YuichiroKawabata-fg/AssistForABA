package com.kawabata.abaprojects.assistforaba.utill;

import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kawabata.abaprojects.assistforaba.R;
import com.kawabata.abaprojects.assistforaba.listcomponent.ListItem;

import java.util.ArrayList;


public class AlarmListAdapter  extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder> {

    private final ArrayList<ListItem> alarmList;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.listTxt);
        }

        public TextView getTextView(){
            return textView;
        }

    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param alarmList ArrayList<ListItem>
     * by RecyclerView.
     */
    public AlarmListAdapter(ArrayList<ListItem> alarmList) {
        this.alarmList = alarmList;
    }


    // タップされたときに呼び出されるメソッドを定義
    protected void onClickedListItem(@NonNull ListItem listItem) {
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
        viewHolder.getTextView().setText(alarmList.get(position).getAlarmName() + "  " + alarmList.get(position).getHour().toString() + ":" + alarmList.get(position).getMinitsu().toString());
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return alarmList.size();
    }
}
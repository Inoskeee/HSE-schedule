package org.hse.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public final class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if(viewType == TYPE_ITEM){
            View contactView = inflater.inflate(R.layout.item_schedule, parent, false);
            return new ViewHolder(contactView, context, onItemClick);
        }
        else if(viewType == TYPE_HEADER){
            View contactView = inflater.inflate(R.layout.item_schedule_header, parent, false);
            return new ViewHolderHeader(contactView, context, onItemClick);
        }
        throw new IllegalArgumentException("Invalid view type");
    }

    @Override
    public int getItemViewType(int position) {
        ScheduleItem data = dataList.get(position);
        if(data instanceof ScheduleItemHeader){
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ScheduleItem data = dataList.get(position);
        if(viewHolder instanceof ViewHolder){
            ((ViewHolder)viewHolder).bind(data);
        }
        else if(viewHolder instanceof ViewHolderHeader){
            ((ViewHolderHeader)viewHolder).bind((ScheduleItemHeader) data);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    private final static int TYPE_ITEM = 0;
    private final static int TYPE_HEADER = 1;

    public List<ScheduleItem> dataList = new ArrayList<>();
    private OnItemClick onItemClick;

    public ItemAdapter(OnItemClick onItemClick) { this.onItemClick = onItemClick; }

    @SuppressLint("NotifyDataSetChanged")
    public void setDataList(List<ScheduleItem> list)
    {
        this.dataList = list;
        notifyDataSetChanged();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private Context context;
        private OnItemClick onItemClick;
        private TextView start;
        private TextView end;
        private TextView type;
        private TextView name;
        private TextView place;
        private TextView teacher;



        public ViewHolder(@NonNull View itemView, Context context, OnItemClick onItemClick) {
            super(itemView);
            this.context = context;
            this.onItemClick = onItemClick;
            start = itemView.findViewById(R.id.timeStart);
            end = itemView.findViewById(R.id.timeEnd);
            type = itemView.findViewById(R.id.scheduleType);
            name = itemView.findViewById(R.id.scheduleName);
            place = itemView.findViewById(R.id.scheduleCabinet);
            teacher = itemView.findViewById(R.id.scheduleTeacher);
        }
        public void bind(final ScheduleItem data){
            start.setText(data.getStart());
            end.setText(data.getEnd());
            type.setText(data.getType());
            name.setText(data.getName());
            place.setText(data.getPlace());
            teacher.setText(data.getTeacher());
        }
    }

    public static class ViewHolderHeader extends RecyclerView.ViewHolder {
        private Context context;
        private OnItemClick onItemClick;
        private TextView title;

        public ViewHolderHeader(@NonNull View itemView, Context context, OnItemClick onItemClick) {
            super(itemView);
            this.context = context;
            this.onItemClick = onItemClick;
            title = itemView.findViewById(R.id.title);
        }
        public void bind(final ScheduleItemHeader data){
            title.setText(data.getTitle());
        }
    }
}

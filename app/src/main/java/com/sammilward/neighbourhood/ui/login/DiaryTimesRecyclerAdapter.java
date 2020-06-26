package com.sammilward.neighbourhood.ui.login;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sammilward.neighbourhood.R;

import java.time.format.DateTimeFormatter;

public class DiaryTimesRecyclerAdapter extends RecyclerView.Adapter<DiaryTimesRecyclerAdapter.ViewHolder> {

    private final Context context;
    private final DiaryDay diaryDay;
    private final LayoutInflater layoutInflater;

    public DiaryTimesRecyclerAdapter(Context context, DiaryDay diaryDay) {
        this.context = context;
        this.diaryDay = diaryDay;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_diary_time_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiaryTime diaryTime = diaryDay.getTimes().get(position);
        holder.currentPosition = position;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        holder.lblDiaryTime.setText(diaryTime.getTime().format(dateTimeFormatter));
    }

    @Override
    public int getItemCount() {
        return diaryDay.getTimes().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView lblDiaryTime;
        public int currentPosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lblDiaryTime = itemView.findViewById(R.id.lblDiaryTime);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ViewEditDiaryTimeActivity.class);
                    intent.putExtra("DIARYDAY", diaryDay);
                    intent.putExtra("DIARYTIMEINDEX", currentPosition);
                    context.startActivity(intent);
                }
            });
        }
    }
}

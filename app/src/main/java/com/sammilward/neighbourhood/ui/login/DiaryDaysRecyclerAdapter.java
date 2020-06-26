package com.sammilward.neighbourhood.ui.login;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sammilward.neighbourhood.R;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DiaryDaysRecyclerAdapter extends RecyclerView.Adapter<DiaryDaysRecyclerAdapter.ViewHolder> {

    private final Context context;
    private final List<DiaryDay> diaryDays;
    private final LayoutInflater layoutInflater;

    public DiaryDaysRecyclerAdapter(Context context, List<DiaryDay> diaryDays) {
        this.context = context;
        this.diaryDays = diaryDays;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_diary_day_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiaryDay diaryDay = diaryDays.get(position);
        holder.currentPosition = position;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        holder.lblDiaryEntryDate.setText((diaryDay.getDate().format(dateTimeFormatter)));


        if (diaryDay.getRating() != null && diaryDay.getRating().equals("Sad"))
        {
            holder.imgDiaryEntryEmote.setImageResource(R.drawable.ic_sad_face_black_24dp);
        }
        else if (diaryDay.getRating() != null && diaryDay.getRating().equals("Meh"))
        {
            holder.imgDiaryEntryEmote.setImageResource(R.drawable.ic_meh_face_black_24dp);
        }
        else if (diaryDay.getRating() != null && diaryDay.getRating().equals("Happy"))
        {
            holder.imgDiaryEntryEmote.setImageResource(R.drawable.ic_happy_face_black_24dp);
        }
        else
        {
            holder.imgDiaryEntryEmote.setVisibility(View.INVISIBLE);
            holder.CVImageShaper.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return diaryDays.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView lblDiaryEntryDate;
        public ImageView imgDiaryEntryEmote;
        public CardView CVImageShaper;
        public int currentPosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lblDiaryEntryDate = itemView.findViewById(R.id.lblDiaryEntryDate);
            imgDiaryEntryEmote = itemView.findViewById(R.id.imgDiaryEntryEmote);
            CVImageShaper = itemView.findViewById(R.id.CVImageShaper);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DiaryDayActivity.class);
                    intent.putExtra("DIARYDAY", diaryDays.get(currentPosition));
                    context.startActivity(intent);
                }
            });
        }
    }
}

package com.sammilward.neighbourhood.ui.login;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sammilward.neighbourhood.R;

import java.util.List;

public class EventsRecyclerAdapter extends RecyclerView.Adapter<EventsRecyclerAdapter.ViewHolder> {

    private final Context context;
    private final List<Event> events;
    private final LayoutInflater layoutInflater;

    private StorageReference storageReference;

    public EventsRecyclerAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
        this.layoutInflater = LayoutInflater.from(context);
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_event_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.currentPosition = position;
        holder.lblEventName.setText(event.getName());
        holder.lblDate.setText(event.getEventDatesText());
        holder.lblDescription.setText(event.getDescription());
        displayEventPicture(holder.imgEventPicture, event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private void displayEventPicture(ImageView imageView, Event event)
    {
        StorageReference storageReference = this.storageReference.child(event.getImageURL());
        GlideApp.with(context).load(storageReference).signature(new ObjectKey(event.getImageUpdatedEpochTime())).into(imageView);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView lblEventName, lblDate, lblDescription;
        public ImageView imgEventPicture;
        public int currentPosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lblEventName = itemView.findViewById(R.id.lblEventsListName);
            lblDate = itemView.findViewById(R.id.lblEventsListStartDate);
            lblDescription = itemView.findViewById(R.id.lblEventsListDescription);
            imgEventPicture = itemView.findViewById(R.id.imgEventsListPicture);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ViewEditEventActivity.class);
                    intent.putExtra("EVENT", events.get(currentPosition));
                    context.startActivity(intent);
                }
            });
        }
    }
}

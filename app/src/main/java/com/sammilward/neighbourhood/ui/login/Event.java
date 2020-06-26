package com.sammilward.neighbourhood.ui.login;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Event implements Parcelable {
    private String Id;
    private String Name;
    private String CreatedByEmail;
    private String ImageURL;
    private String Description;
    private long ImageUpdatedEpochTime;
    private LatLng Location;
    private String City;
    private Date StartDate;
    private Date EndDate;

    protected Event(Parcel in) {
        Id = in.readString();
        Name = in.readString();
        CreatedByEmail = in.readString();
        ImageURL = in.readString();
        Description = in.readString();
        ImageUpdatedEpochTime = in.readLong();
        Location = in.readParcelable(LatLng.class.getClassLoader());
        StartDate = (Date) in.readValue(getClass().getClassLoader());
        EndDate = (Date) in.readValue(getClass().getClassLoader());
        City = in.readString();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public Event() {

    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCreatedByEmail() {
        return CreatedByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        CreatedByEmail = createdByEmail;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public long getImageUpdatedEpochTime() {
        return ImageUpdatedEpochTime;
    }

    public void setImageUpdatedEpochTime(long imageUpdatedEpochTime) {
        ImageUpdatedEpochTime = imageUpdatedEpochTime;
    }

    public LatLng getLocation() {
        return Location;
    }

    public void setLocation(LatLng location) {
        Location = location;
    }

    public Date getStartDate() {
        return StartDate;
    }

    public void setStartDate(Date startDate) {
        StartDate = startDate;
    }

    public Date getEndDate() {
        return EndDate;
    }

    public void setEndDate(Date endDate) {
        EndDate = endDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Id);
        parcel.writeString(Name);
        parcel.writeString(CreatedByEmail);
        parcel.writeString(ImageURL);
        parcel.writeString(Description);
        parcel.writeLong(ImageUpdatedEpochTime);
        parcel.writeParcelable(Location, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        parcel.writeValue(StartDate);
        parcel.writeValue(EndDate);
        parcel.writeString(City);
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getEventDatesText()
    {
        if (StartDate.equals(EndDate))
        {
            String message = new SimpleDateFormat("dd-MM-yyyy").format(StartDate.getTime());
            return message;
        }
        else
        {
            String start = new SimpleDateFormat("dd-MM-yyyy").format(StartDate.getTime());
            String end = new SimpleDateFormat("dd-MM-yyyy").format(EndDate.getTime());
            return start + " to " + end;
        }
    }
}

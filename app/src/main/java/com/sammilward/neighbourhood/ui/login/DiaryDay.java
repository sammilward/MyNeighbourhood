package com.sammilward.neighbourhood.ui.login;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDate;
import java.util.List;

public class DiaryDay implements Parcelable {
    private String id;
    private LocalDate Date;
    private String CreatedByEmail;
    private String rating;
    private List<DiaryTime> times;

    public DiaryDay() {
    }

    protected DiaryDay(Parcel in) {
        id = in.readString();
        Date = (LocalDate) in.readValue(getClass().getClassLoader());
        CreatedByEmail = in.readString();
        rating = in.readString();
        times = in.createTypedArrayList(DiaryTime.CREATOR);
    }

    public static final Creator<DiaryDay> CREATOR = new Creator<DiaryDay>() {
        @Override
        public DiaryDay createFromParcel(Parcel in) {
            return new DiaryDay(in);
        }

        @Override
        public DiaryDay[] newArray(int size) {
            return new DiaryDay[size];
        }
    };

    public String getCreatedByEmail() {
        return CreatedByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        CreatedByEmail = createdByEmail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return Date;
    }

    public void setDate(LocalDate date) {
        Date = date;
    }


    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public List<DiaryTime> getTimes() {
        return times;
    }

    public void setTimes(List<DiaryTime> times) {
        this.times = times;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeValue(Date);
        parcel.writeString(CreatedByEmail);
        parcel.writeString(rating);
        parcel.writeTypedList(times);
    }
}

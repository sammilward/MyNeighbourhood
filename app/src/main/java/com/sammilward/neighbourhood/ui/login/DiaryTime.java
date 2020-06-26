package com.sammilward.neighbourhood.ui.login;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDateTime;

public class DiaryTime implements Parcelable, Comparable<DiaryTime> {
    private LocalDateTime Time;
    private String ImageURL;
    private String AudioURL;
    private String Description;
    private long ImageUpdatedEpochTime;

    public DiaryTime() {
    }

    protected DiaryTime(Parcel in) {
        Time = (LocalDateTime) in.readValue(getClass().getClassLoader());
        ImageURL = in.readString();
        AudioURL = in.readString();
        Description = in.readString();
        ImageUpdatedEpochTime = in.readLong();
    }

    public static final Creator<DiaryTime> CREATOR = new Creator<DiaryTime>() {
        @Override
        public DiaryTime createFromParcel(Parcel in) {
            return new DiaryTime(in);
        }

        @Override
        public DiaryTime[] newArray(int size) {
            return new DiaryTime[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    public String getAudioURL() {
        return AudioURL;
    }

    public void setAudioURL(String audioURL) {
        AudioURL = audioURL;
    }

    public LocalDateTime getTime() {
        return Time;
    }

    public void setTime(LocalDateTime time) {
        Time = time;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeValue(Time);
        parcel.writeString(ImageURL);
        parcel.writeString(AudioURL);
        parcel.writeString(Description);
        parcel.writeLong(ImageUpdatedEpochTime);
    }

    @Override
    public int compareTo(DiaryTime diaryTime) {
        return Time.compareTo(diaryTime.getTime());
    }
}

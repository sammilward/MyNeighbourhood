package com.sammilward.neighbourhood.ui.login;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Date;
import java.util.Map;

import static com.sammilward.neighbourhood.ui.login.Constants.AUDIOURL;
import static com.sammilward.neighbourhood.ui.login.Constants.DESCRIPTION;
import static com.sammilward.neighbourhood.ui.login.Constants.DIARYTIME;
import static com.sammilward.neighbourhood.ui.login.Constants.IMAGEUPDATEDEPOCHTIME;
import static com.sammilward.neighbourhood.ui.login.Constants.IMAGEURL;

public class FirebaseDiaryTimeMapper {

    public DiaryTime HashMapToDiaryTime(HashMap hashMap)
    {
        DiaryTime dt = new DiaryTime();
        dt.setDescription((String) hashMap.get(DESCRIPTION));
        dt.setAudioURL((String) hashMap.get(AUDIOURL));
        dt.setImageURL((String) hashMap.get(IMAGEURL));
        dt.setImageUpdatedEpochTime((Long) hashMap.get(IMAGEUPDATEDEPOCHTIME));
        Date tempDate = (Date) hashMap.get(DIARYTIME);
        dt.setTime(tempDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        return dt;
    }

    public Map<String, Object> PlaceToHashMap(DiaryTime diaryTime)
    {
        Map<String, Object> diaryDayHashMap = new HashMap<>();
        diaryDayHashMap.put(DESCRIPTION, diaryTime.getDescription());
        diaryDayHashMap.put(AUDIOURL, diaryTime.getAudioURL());
        diaryDayHashMap.put(IMAGEURL, diaryTime.getImageURL());
        diaryDayHashMap.put(IMAGEUPDATEDEPOCHTIME, diaryTime.getImageUpdatedEpochTime());
        Date tempDate = Date.from(diaryTime.getTime().atZone(ZoneId.systemDefault()).toInstant());
        diaryDayHashMap.put(DIARYTIME, tempDate);
        return diaryDayHashMap;
    }
}

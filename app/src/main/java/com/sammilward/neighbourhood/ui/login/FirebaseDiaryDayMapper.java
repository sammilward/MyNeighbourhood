package com.sammilward.neighbourhood.ui.login;

import com.google.firebase.firestore.DocumentSnapshot;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sammilward.neighbourhood.ui.login.Constants.CREATEDBYEMAIL;
import static com.sammilward.neighbourhood.ui.login.Constants.DIARYDATE;
import static com.sammilward.neighbourhood.ui.login.Constants.DIARYTIMES;
import static com.sammilward.neighbourhood.ui.login.Constants.RATING;

public class FirebaseDiaryDayMapper {
    private FirebaseDiaryTimeMapper firebaseDiaryTimeMapper;

    public FirebaseDiaryDayMapper()
    {
        firebaseDiaryTimeMapper = new FirebaseDiaryTimeMapper();
    }

    public DiaryDay DocumentToDiaryDay(DocumentSnapshot document)
    {
        DiaryDay diaryDay = new DiaryDay();
        diaryDay.setId(document.getId());
        diaryDay.setCreatedByEmail(document.getString(CREATEDBYEMAIL));
        diaryDay.setRating(document.getString(RATING));
        Date tempDate = document.getDate(DIARYDATE);
        diaryDay.setDate(tempDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

        List<HashMap> diaryTimesDocs = (List<HashMap>) document.get(DIARYTIMES);

        if (diaryTimesDocs != null)
        {
            List<DiaryTime> diaryTimes = new ArrayList();
            for (HashMap hm : diaryTimesDocs) {
                diaryTimes.add(firebaseDiaryTimeMapper.HashMapToDiaryTime(hm));
            }
            Collections.sort(diaryTimes);
            diaryDay.setTimes(diaryTimes);
        } else diaryDay.setTimes(new ArrayList<DiaryTime>());

        return diaryDay;
    }

    public Map<String, Object> DiaryDayToHashMap(DiaryDay diaryDay)
    {
        Map<String, Object> diaryDayHashMap = new HashMap<>();
        diaryDayHashMap.put(CREATEDBYEMAIL, diaryDay.getCreatedByEmail());
        diaryDayHashMap.put(RATING, diaryDay.getRating());
        Date tempDate = Date.from(diaryDay.getDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        diaryDayHashMap.put(DIARYDATE, tempDate);

        if (diaryDay.getTimes() != null)
        {
            List<Map<String, Object>> timeMaps = new ArrayList();
            for (DiaryTime diaryTime : diaryDay.getTimes()) {
                timeMaps.add(firebaseDiaryTimeMapper.PlaceToHashMap(diaryTime));
            }
            diaryDayHashMap.put(DIARYTIMES, timeMaps);
        } else diaryDayHashMap.put(DIARYTIMES, null);

        return diaryDayHashMap;
    }
}

package com.sammilward.neighbourhood;

import android.provider.CalendarContract;

import com.sammilward.neighbourhood.ui.login.User;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class UserTests {

    private User _sut;

    @Before
    public void Setup()
    {
        _sut = new User();
    }

    @Test
    public void UserWithValidDOB_GetAge_ReturnsCorrectAge()
    {
        LocalDateTime ldt = LocalDateTime.now().minusDays(5).plusMonths(2).minusYears(25);
        ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
        Date modifiedDate = Date.from(zdt.toInstant());

        _sut.setDOB(modifiedDate);
        int expectedAge = 24;
        int actualAge = _sut.getAge();
        Assert.assertEquals(expectedAge, actualAge);
    }
}

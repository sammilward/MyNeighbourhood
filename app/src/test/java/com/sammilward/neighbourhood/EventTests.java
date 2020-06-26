package com.sammilward.neighbourhood;

import com.sammilward.neighbourhood.ui.login.Event;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;

public class EventTests {

    private Event _sut;

    @Before
    public void Setup()
    {
        _sut = new Event();
    }

    @Test
    public void GivenMatchingStartAndEndDate_getEventDatesText_ReturnsTextWithOnlyOneDateListed()
    {
        LocalDate date = LocalDate.of(2019,10,20);
        _sut.setStartDate(Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        _sut.setEndDate(Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        String expectedMessage = "20-10-2019";
        Assert.assertEquals(expectedMessage, _sut.getEventDatesText());
    }

    @Test
    public void GivenDifferentStartAndEndDate_getEventDatesText_ReturnsTextDisplayingBothDates()
    {
        LocalDate startDate = LocalDate.of(2019,10,20);
        LocalDate endDate = LocalDate.of(2019,10,25);

        _sut.setStartDate(Date.from(startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        _sut.setEndDate(Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        String expectedMessage = "20-10-2019 to 25-10-2019";
        Assert.assertEquals(expectedMessage, _sut.getEventDatesText());
    }
}

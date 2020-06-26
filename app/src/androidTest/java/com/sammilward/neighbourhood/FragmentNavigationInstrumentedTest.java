package com.sammilward.neighbourhood;

import android.content.Context;
import android.view.Gravity;

import androidx.test.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.sammilward.neighbourhood.ui.login.MainActivity;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class FragmentNavigationInstrumentedTest {

    @BeforeClass
    public static void classSetUp()
    {

    }

    //Needs this annotation to tell the test that this is the activity rule to be used for all tests
    @Rule
    public ActivityTestRule<MainActivity> rule  = new  ActivityTestRule<>(MainActivity.class);

    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getTargetContext();

        onView(withId(R.id.drawer_layout)).check(matches(not(isDisplayed())));

        assertEquals("com.sammilward.neighbourhood", appContext.getPackageName());
    }
}

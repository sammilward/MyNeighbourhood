package com.sammilward.neighbourhood;

import com.sammilward.neighbourhood.ui.login.ValidationHandler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ValidationHandlerTests {

    private ValidationHandler _sut;

    @Before
    public void Setup()
    {
        _sut = new ValidationHandler();
    }

    @Test
    public void GivenValidEmail_IsEmailValid_ReturnsTrue()
    {
        String testEmail = "test@gmail.com";
        boolean expected = true;
        boolean actual = _sut.isEmailValid(testEmail);
        assertEquals(expected, actual);
    }

    @Test
    public void GivenInvalidEmail_IsEmailValid_ReturnsFalse()
    {
        String testEmail = "testemail.com";
        boolean expected = false;
        boolean actual = _sut.isEmailValid(testEmail);
        assertEquals(expected, actual);
    }

    @Test
    public void GivenValidPassword_IsPasswordValid_ReturnsTrue()
    {
        String testPassword = "Password1";
        boolean expected = true;
        boolean actual = _sut.isPasswordValid(testPassword);
        assertEquals(expected, actual);
    }

    @Test
    public void GivenPasswordNoNumber_IsPasswordValid_ReturnsFalse()
    {
        String testPassword = "testtest";
        boolean expected = false;
        boolean actual = _sut.isPasswordValid(testPassword);
        assertEquals(expected, actual);
    }

    @Test
    public void GivenPasswordTooShort_IsPasswordValid_ReturnsFalse()
    {
        String testPassword = "test4";
        boolean expected = false;
        boolean actual = _sut.isPasswordValid(testPassword);
        assertEquals(expected, actual);
    }

    @Test
    public void GivenMatchingPasswords_DoPasswordsMatch_ReturnsTrue()
    {
        String testPassword = "testtest1";
        String testPassword2 = "testtest1";
        boolean expected = true;
        boolean actual = _sut.doPasswordsMatch(testPassword,testPassword2);
        assertEquals(expected, actual);
    }

    @Test
    public void GivenUnmatchingPasswords_DoPasswordsMatch_ReturnsFalse() {
        String testPassword = "testtest1";
        String testPassword2 = "testtest2";
        boolean expected = false;
        boolean actual = _sut.doPasswordsMatch(testPassword, testPassword2);
        assertEquals(expected, actual);
    }
}
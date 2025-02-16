package com.example.activity

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testLoginNavigationToHomeScreen() {

        Thread.sleep(5000)
        // Check if we are on the login screen
        onView(withId(R.id.loginsuccessful)).check(matches(isDisplayed()))

        // Click "Login Successful" button
        onView(withId(R.id.loginsuccessful)).perform(click())

        // Check if we are on the Home Screen (Fourth Screen)
        onView(withId(R.id.chats)).check(matches(isDisplayed()))
    }

    @Test
    fun testProfileNavigationToFollowers() {
        Thread.sleep(5000)

        // Check if we are on the login screen
        onView(withId(R.id.loginsuccessful)).check(matches(isDisplayed()))

        // Click "Login Successful" button
        onView(withId(R.id.loginsuccessful)).perform(click())

        // Navigate to Profile Screen first
        onView(withId(R.id.profile)).perform(click())

        // Check if profile screen is displayed
        onView(withId(R.id.followers)).check(matches(isDisplayed()))

        // Click "Followers" text
        onView(withId(R.id.followers)).perform(click())

        // Verify if we are now on the Followers screen
        onView(withId(R.id.gotoprofile)).check(matches(isDisplayed()))
    }
}

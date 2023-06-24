package edu.shape.postdraftsforsocialmedia

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.shape.postdraftsforsocialmedia.Controller.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testAlbumButton_launchesPhotoLibrary() {
        // Start monitoring intents
        Intents.init()

        // Perform click on the Album button
        onView(withId(R.id.button2)).perform(click())

        // Verify that the photo library is launched
        Intents.intended(hasAction(Intent.ACTION_PICK))
        Intents.intended(hasType("image/*"))

        // Stop monitoring intents
        Intents.release()
    }
}
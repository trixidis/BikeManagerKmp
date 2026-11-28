package com.bikemanager.ui.bikes

import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot tests of the bikes list previews.
 *
 * Record:  ./gradlew :shared:recordRoborazziDebug
 * Verify:  ./gradlew :shared:verifyRoborazziDebug
 * Images are written to shared/screenshots/.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
class BikesScreenScreenshotTest {

    @Test
    fun bikesListDark() {
        captureRoboImage(filePath = "screenshots/bikes_list_dark.png") {
            BikesListDarkPreview()
        }
    }

    @Test
    fun bikesListLight() {
        captureRoboImage(filePath = "screenshots/bikes_list_light.png") {
            BikesListLightPreview()
        }
    }
}

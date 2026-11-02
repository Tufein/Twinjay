package com.tsutsen.platformplayer.activities

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanionSleepPolicyTest {
    @Test fun sleepsOnlyWhenEnabledPlayingAndIdle() {
        assertTrue(CompanionSleepPolicy.shouldSleep(true, true, CompanionSleepPolicy.IDLE_MS))
        assertFalse(CompanionSleepPolicy.shouldSleep(false, true, 60_000))
        assertFalse(CompanionSleepPolicy.shouldSleep(true, false, 60_000))
        assertFalse(CompanionSleepPolicy.shouldSleep(true, true, CompanionSleepPolicy.IDLE_MS - 1))
    }

    @Test fun customDelayIsRespected() {
        assertFalse(CompanionSleepPolicy.shouldSleep(true, true, 30_000, delayMs = 60_000))
        assertTrue(CompanionSleepPolicy.shouldSleep(true, true, 60_000, delayMs = 60_000))
    }

    @Test fun sleepBrightnessFollowsLevel() {
        assertTrue(CompanionSleepPolicy.sleepBrightness(0) == 0f)
        assertTrue(CompanionSleepPolicy.sleepBrightness(15) == 0.15f)
        assertTrue(CompanionSleepPolicy.sleepBrightness(500) == 1f)
    }
}

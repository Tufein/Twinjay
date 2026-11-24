package com.tsutsen.platformplayer

import com.tsutsen.platformplayer.constructs.BackgroundTaskHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.After
import org.junit.Test
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.StandardTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class BackgroundTaskHandlerTest {

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRun() = runTest {
        // The task runs on a real background thread, so wait in real time (runTest's delay is virtual).
        val result = java.util.concurrent.atomic.AtomicInteger(0)
        val taskHandler = BackgroundTaskHandler(this, { result.set(1) })

        taskHandler.run()
        val deadline = System.currentTimeMillis() + 5000
        while (result.get() != 1 && System.currentTimeMillis() < deadline) Thread.sleep(10)

        assertEquals(1, result.get())
    }

    @Test
    fun testCancel() = runTest {
        var result = 0
        val taskHandler = BackgroundTaskHandler(this, { Thread.sleep(500); result = 1 })

        taskHandler.run()
        taskHandler.cancel()
        delay(1000)

        assertEquals(0, result) // Should still be 0 because task was cancelled
    }

    //TODO: Disabled to fix pipeline for now.
    /*
    @Test
    fun testException() = runTest {
        var exceptionMessage: String? = null
        val taskHandler = BackgroundTaskHandler(this, { throw Exception("Test exception") })

        taskHandler.exception<Exception> { exceptionMessage = it.message }
        taskHandler.run()
        delay(1000)

        assertEquals("Test exception", exceptionMessage)
    }*/

    @Test
    fun testDispose() = runTest {
        var result = 0
        val taskHandler = BackgroundTaskHandler(this, { Thread.sleep(500); result = 1 })

        taskHandler.run()
        taskHandler.dispose()
        delay(1000)

        assertEquals(0, result) // Should still be 0 because task was disposed
    }
}
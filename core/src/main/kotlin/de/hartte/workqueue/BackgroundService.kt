package de.hartte.workqueue

import org.slf4j.LoggerFactory
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class BackgroundService(threadName: String,
                        private val worker: CancelableWork,
                        private val backOffFactory: () -> BackOffStrategy,
                        private val waitAfterSuccessInMs: Long = 0) : AutoCloseable {

    @Volatile
    private var running: Boolean = true

    private val logger = LoggerFactory.getLogger(javaClass)

    private val lock = ReentrantLock()

    private val signal = lock.newCondition() // Wait condition for wakeup on external event

    private val thread = Thread(this::run, threadName)

    private fun run() {

        var failures = 0
        var backOff = backOffFactory()

        while (running) {

            // try to obtain a connection
            try {
                worker.work()

                // Reset back-off strategy after successfully performing the work
                if (failures > 0) {
                    backOff = backOffFactory()
                    failures = 0
                }

                // Wait after success or until notified of new work
                if (waitAfterSuccessInMs > 0) {
                    lock.withLock {
                        signal.await(waitAfterSuccessInMs, TimeUnit.MILLISECONDS)
                    }
                }
            } catch (e: Throwable) {
                logger.error("Failed performing background work", e)

                // Back off after failing to perform the background task
                failures++
                val waitPeriod = backOff.failAndBackOff()
                logger.info("Failed $failures time(s). Retrying after $waitPeriod ms.", e)
                lock.withLock {
                    signal.await(waitPeriod, TimeUnit.MILLISECONDS)
                }

            } catch (e: CancellationException) {
                // Ignore cancellation exceptions and just leave the thread
                break
            }
        }

    }

    fun start() {
        thread.start()
    }

    /**
     * Wakes up this background service in case it's currently asleep to make it perform more work.
     */
    fun wakeUp() {
        lock.withLock {
            signal.signal()
        }
    }

    /**
     * Stops this background service.
     */
    override fun close() {
        lock.withLock {
            running = false
            signal.signal()
        }
        thread.join()
    }

}

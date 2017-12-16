package de.hartte.workqueue

import org.postgresql.jdbc.PgConnection
import org.slf4j.LoggerFactory
import java.sql.DriverManager
import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

/**
 * Uses a PostgreSQL connection to wake up the actual worker threads to poll for work immediately after
 * new work arrives.
 */
internal class QueueWatchDog(private var connectionProvider: BackOffWorkWrapper<PgConnection>) : AutoCloseable {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val stopLatch = CountDownLatch(1)

    private val thread = Thread(this::run, "queue-watcher").apply {
        start()
    }

    private var openConnection: PgConnection? = null

    private fun run() {

        while (!stopLatch.await(0, TimeUnit.SECONDS)) {

            // try to obtain a connection
            val connection = connectionProvider.get()
            openConnection = connection // Store it in the class so we can close it

            try {

                connection.use {
                    it.autoCommit = true
                    it.createStatement().execute("LISTEN queuename")

                    // We have to expose the connection that we're currently waiting on, because
                    // we have to forecully close it to cancel the wait. Interrupting the thread
                    // sadly does nothing on newer versions of java.
                    val notifications = it.getNotifications(10_000) ?: return@use

                    for (notification in notifications) {
                        println("${notification.name} ${notification.parameter}")
                    }

                }

            } catch (e: CancellationException) {
                // Ignore cancellation exceptions
                break
            } catch (e: Throwable) {
                logger.warn("An error occurred while listening for DB notifications", e)
            } finally {
                openConnection = null
            }

        }

    }

    override fun close() {
        connectionProvider.abort()
        openConnection?.close() // This will cause an active call to getNotifications to return early
        thread.join()
    }

}

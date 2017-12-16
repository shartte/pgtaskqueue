package de.hartte.workqueue.examples

import de.hartte.workqueue.*
import de.hartte.workqueue.events.TaskEventSink
import de.hartte.workqueue.tasks.JacksonDataConverter
import de.hartte.workqueue.tasks.TaskHandler
import de.hartte.workqueue.tasks.TaskTypeRegistry
import org.postgresql.ds.PGSimpleDataSource
import org.postgresql.jdbc.PgConnection
import java.sql.DriverManager
import java.util.function.Supplier

fun main(args: Array<String>) {

    val settings = DbConnectionSettings(
            args[0],
            args[1],
            args[2]
    )

    val connectionProvider = BackOffWorkWrapper(
            Supplier { DriverManager.getConnection(settings.url, settings.username, settings.password) as PgConnection },
            {
                DecorrelatedJitterBackOffStrategy(
                        500,
                        30000
                )
            }
    )

    val dataSource = PGSimpleDataSource()
            .apply {
                url = settings.url
                user = settings.username
                password = settings.password
            }

    val taskTypeRegistry = TaskTypeRegistry()

    val eventSinkFactory = { taskId: Long ->
        object : TaskEventSink {
            override fun workStarted() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun info(message: String) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun warning(message: String) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun warning(message: String, e: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun error(message: String) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun error(message: String, e: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun progressStage(stage: String) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun progress(completedSteps: Int, totalSteps: Int) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun workSucceeded() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun workFailed(message: String) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun workFailed(message: String, e: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
    }

    val taskHandler = TaskHandler(taskTypeRegistry, JacksonDataConverter(), eventSinkFactory)

    val queue = TaskQueue(TaskQueueConfiguration(), JacksonDataConverter(), taskTypeRegistry, taskHandler, dataSource)

    val work = object : CancelableWork {
        override fun work() {
            queue.processTask()
        }

    }

    BackgroundService(
            "queue-worker",
            work,
            { DecorrelatedJitterBackOffStrategy(2_500, 30_000) },
            30_000
    ).apply { start() }


    System.`in`.read()
    print("Shutting down... ")

    println("Worker queue shut down.")

}

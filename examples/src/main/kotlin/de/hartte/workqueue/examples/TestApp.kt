package de.hartte.workqueue.examples

import de.hartte.workqueue.*
import de.hartte.workqueue.TaskDbConfiguration
import de.hartte.workqueue.events.JdbcEventSinkFactory
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

    val dataSource = PGSimpleDataSource()
            .apply {
                url = settings.url
                user = settings.username
                password = settings.password
            }

    val taskTypeRegistry = TaskTypeRegistry().apply {
        registerAmbientTasks()
    }

    val eventSinkFactory = JdbcEventSinkFactory(dataSource, TaskDbConfiguration())

    val taskHandler = TaskHandler(taskTypeRegistry, JacksonDataConverter(), eventSinkFactory::createEventSink)

    val queue = TaskQueue(TaskDbConfiguration(), JacksonDataConverter(), taskHandler, dataSource)

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

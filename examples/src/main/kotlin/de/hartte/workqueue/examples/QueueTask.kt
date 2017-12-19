package de.hartte.workqueue.examples

import de.hartte.workqueue.TaskDbConfiguration
import de.hartte.workqueue.TaskQueue
import de.hartte.workqueue.events.JdbcEventSinkFactory
import de.hartte.workqueue.tasks.JacksonDataConverter
import de.hartte.workqueue.tasks.TaskHandler
import de.hartte.workqueue.tasks.TaskTypeRegistry
import org.postgresql.ds.PGSimpleDataSource

fun main(args: Array<String>) {

    val settings = DbConnectionSettings(
            args[0],
            args[1],
            args[2]
    )

    val dataSource = PGSimpleDataSource().apply {
        applicationName = "Task Queue - Enqueue"
        url = settings.url
        user = settings.username
        password = settings.password
    }

    val config = TaskDbConfiguration()

    val typeRegistry = TaskTypeRegistry()

    val taskType = typeRegistry.register<GenerateReportArgs>("generate_report") { taskData, eventSink ->

    }

    val eventSinkFactory = JdbcEventSinkFactory(dataSource, config)
    val taskHandler = TaskHandler(typeRegistry, JacksonDataConverter(), eventSinkFactory::createEventSink)

    val queue = TaskQueue(
            config,
            JacksonDataConverter(),
            taskHandler,
            dataSource
    )

    queue.queueTask(taskType, GenerateReportArgs())

}

class GenerateReportArgs(

)

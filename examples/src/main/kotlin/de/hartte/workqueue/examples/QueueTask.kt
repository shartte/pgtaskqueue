package de.hartte.workqueue.examples

import de.hartte.workqueue.TaskQueue
import de.hartte.workqueue.TaskQueueConfiguration
import de.hartte.workqueue.events.TaskEventSink
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

    val config = TaskQueueConfiguration()

    val typeRegistry = TaskTypeRegistry()

    val taskType = typeRegistry.register<GenerateReportArgs>("generate_report") { taskData, eventSink ->

    }

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

    val taskHandler = TaskHandler(typeRegistry, JacksonDataConverter(), eventSinkFactory)

    val queue = TaskQueue(
            config,
            JacksonDataConverter(),
            typeRegistry,
            taskHandler,
            dataSource
    )

    queue.queueTask(taskType, GenerateReportArgs())

}

class GenerateReportArgs(

)

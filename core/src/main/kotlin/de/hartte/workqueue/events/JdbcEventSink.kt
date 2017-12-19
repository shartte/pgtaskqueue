package de.hartte.workqueue.events

import de.hartte.workqueue.TaskDbConfiguration
import javax.sql.DataSource

class JdbcEventSink(private val dataSource: DataSource,
                    config: TaskDbConfiguration,
                    private val taskId: Long) : TaskEventSink {

    private val insertEventSql = """INSERT INTO ${config.eventTableName}
        (task_id, type, data) VALUES (?, ?, ?)"""

    private fun insertEvent(eventType: EventType, eventInfo: String? = null) {

        dataSource.connection.use { connection ->

            connection.autoCommit = true // Make sure we're in auto commit mode

            connection.prepareStatement(insertEventSql.trimStart()).use { stmt ->
                stmt.setLong(1, taskId)
                stmt.setInt(2, eventToDb(eventType))
                stmt.setString(3, eventInfo)
                stmt.execute()
            }

        }

    }

    override fun workStarted() {
        insertEvent(EventType.WORK_START)
    }

    override fun info(message: String) {
        insertEvent(EventType.MESSAGE_INFO, message)
    }

    override fun warning(message: String) {
        insertEvent(EventType.MESSAGE_WARNING, message)
    }

    override fun warning(message: String, e: Throwable) {
        insertEvent(EventType.MESSAGE_WARNING, message + e.toString())
    }

    override fun error(message: String) {
        insertEvent(EventType.MESSAGE_ERROR, message)
    }

    override fun error(message: String, e: Throwable) {
        insertEvent(EventType.MESSAGE_ERROR, message + e.toString())
    }

    override fun progressStage(stage: String) {
        insertEvent(EventType.PROGRESS_STAGE, stage)
    }

    override fun progress(completedSteps: Int, totalSteps: Int) {
        insertEvent(EventType.PROGRESS_STAGE, "$completedSteps|$totalSteps")
    }

    override fun workSucceeded() {
        insertEvent(EventType.WORK_SUCCESS)
    }

    override fun workFailed(message: String) {
        insertEvent(EventType.WORK_FAILURE, message)
    }

    override fun workFailed(message: String, e: Throwable) {
        insertEvent(EventType.WORK_FAILURE, message + e.toString())
    }

    private fun eventToDb(eventType: EventType): Int {
        return when (eventType) {
            EventType.WORK_START -> 1
            EventType.MESSAGE_INFO -> 2
            EventType.MESSAGE_WARNING -> 3
            EventType.MESSAGE_ERROR -> 4
            EventType.PROGRESS_STAGE -> 5
            EventType.PROGRESS -> 6
            EventType.WORK_SUCCESS -> 7
            EventType.WORK_FAILURE -> 8
        }
    }

    private fun eventFromDb(eventType: Int): EventType {
        return when (eventType) {
            1 -> EventType.WORK_START
            2 -> EventType.MESSAGE_INFO
            3 -> EventType.MESSAGE_WARNING
            4 -> EventType.MESSAGE_ERROR
            5 -> EventType.PROGRESS_STAGE
            6 -> EventType.PROGRESS
            7 -> EventType.WORK_SUCCESS
            8 -> EventType.WORK_FAILURE
            else -> throw IllegalArgumentException("Unknown event type $eventType")
        }
    }

}

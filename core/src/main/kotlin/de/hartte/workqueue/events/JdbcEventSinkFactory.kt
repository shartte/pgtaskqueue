package de.hartte.workqueue.events

import de.hartte.workqueue.TaskDbConfiguration
import javax.sql.DataSource

class JdbcEventSinkFactory(private val dataSource: DataSource, private val config: TaskDbConfiguration) {

    fun createEventSink(taskId: Long) = JdbcEventSink(dataSource, config, taskId)

}

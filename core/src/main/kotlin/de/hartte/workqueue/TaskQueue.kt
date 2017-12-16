package de.hartte.workqueue

import de.hartte.workqueue.tasks.TaskDataConverter
import de.hartte.workqueue.tasks.TaskHandler
import de.hartte.workqueue.tasks.TaskType
import de.hartte.workqueue.tasks.TaskTypeRegistry
import org.postgresql.util.PGobject
import java.sql.SQLException
import javax.sql.DataSource

class TaskQueue(config: TaskQueueConfiguration,
                private val dataConverter: TaskDataConverter,
                private val typeRegistry: TaskTypeRegistry,
                private val taskHandler: TaskHandler,
                private val dataSource: DataSource) {

    private fun escapeIdentifier(identifier: String) = StringBuilder().let {
        org.postgresql.core.Utils.escapeIdentifier(it, identifier)
        it.toString()
    }

    private val insertSql = """INSERT INTO ${escapeIdentifier(config.queueTableName)}
        (task_type, task_data)
        VALUES (?, ?)
        RETURNING id""".trimStart()

    private val queryForWorkSql = """
        DELETE FROM ${escapeIdentifier(config.queueTableName)}
        WHERE id = (
          SELECT id FROM ${escapeIdentifier(config.queueTableName)}
           ORDER BY queued DESC
           FOR UPDATE SKIP LOCKED
           LIMIT 1
        )
        RETURNING *
        """.trimStart()

    fun <T> queueTask(taskType: TaskType<T>, taskData: T): Long {

        return dataSource.connection.use { connection ->
            connection.prepareStatement(insertSql).use { stmt ->

                val jsonObject = PGobject().apply {
                    type = "jsonb"
                    value = dataConverter.toJson(taskData)
                }

                stmt.setObject(1, taskType.typeId)
                stmt.setObject(2, jsonObject)

                stmt.executeQuery().use { rs ->
                    if (!rs.next()) {
                        throw SQLException("Expected the DB to return the task id after inserting a task, but it " +
                                "didn't.")
                    }
                    rs.getLong(1)
                }
            }
        }

    }

    fun processTask() {

        dataSource.connection.use { connection ->

            connection.prepareStatement(queryForWorkSql).use { stmt ->

                stmt.executeQuery().use { resultSet ->

                    if (!resultSet.next()) {
                        return // Nothing to do
                    }

                    val taskId = resultSet.getLong("id")
                    val taskTypeId = resultSet.getString("task_type")
                    val taskData = resultSet.getString("task_data")

                    taskHandler.workOnTask(taskId, taskTypeId, taskData)

                    return

                }

            }

        }

    }

}

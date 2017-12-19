package de.hartte.workqueue

import de.hartte.workqueue.tasks.TaskDataConverter
import de.hartte.workqueue.tasks.TaskHandler
import de.hartte.workqueue.tasks.TaskType
import org.intellij.lang.annotations.Language
import org.postgresql.util.PGobject
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

class TaskQueue(config: TaskDbConfiguration,
                private val dataConverter: TaskDataConverter,
                private val taskHandler: TaskHandler,
                private val dataSource: DataSource) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private fun escapeIdentifier(identifier: String) = StringBuilder().let {
        org.postgresql.core.Utils.escapeIdentifier(it, identifier)
        it.toString()
    }

    @Language("PostgreSQL")
    private val queueTaskSql = """WITH inserted_task AS (INSERT INTO ${escapeIdentifier(config.taskTableName)}
        (type, data)
        VALUES (?, ?)
        RETURNING id),
        insert_work AS (INSERT INTO ${escapeIdentifier(config.queueTableName)} (id)
        SELECT id FROM inserted_task)
        SELECT id FROM inserted_task
        """.trimStart()

    @Language("PostgreSQL")
    private val dequeueTaskSql = """
        WITH dequeued_task AS (
        DELETE FROM ${escapeIdentifier(config.queueTableName)}
        WHERE id = (
          SELECT id FROM ${escapeIdentifier(config.queueTableName)}
           ORDER BY queued DESC
           FOR UPDATE SKIP LOCKED
           LIMIT 1
        )
        RETURNING id)
        SELECT id, type, data FROM dequeued_task JOIN ${escapeIdentifier(config.taskTableName)} USING (id)
        """.trimStart()

    init {
        logger.trace("SQL for queueing tasks: {}", queueTaskSql)
        logger.trace("SQL for dequeueing tasks: {}", dequeueTaskSql)
    }

    fun <T> queueTask(taskType: TaskType<T>, taskData: T): Long {

        return dataSource.connection.use { connection ->
            connection.prepareStatement(queueTaskSql).use { stmt ->

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

            connection.prepareStatement(dequeueTaskSql).use { stmt ->

                connection.autoCommit = false // This starts a transaction

                try {

                    stmt.executeQuery().use { resultSet ->

                        if (resultSet.next()) {
                            val taskId = resultSet.getLong(1)
                            val taskTypeId = resultSet.getString(2)
                            val taskData = resultSet.getString(3)

                            taskHandler.workOnTask(taskId, taskTypeId, taskData)
                        }

                    }

                    connection.commit()

                } catch (e: Throwable) {
                    connection.rollback()
                    throw e
                } finally {
                    connection.autoCommit = true
                }

            }

        }

    }

}

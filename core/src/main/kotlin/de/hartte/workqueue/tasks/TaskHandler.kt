package de.hartte.workqueue.tasks

import de.hartte.workqueue.events.TaskEventSink
import org.slf4j.LoggerFactory
import kotlin.system.measureTimeMillis

/**
 * Handles execution of tasks retrieved from the queue.
 */
class TaskHandler(private val taskTypeRegistry: TaskTypeRegistry,
                  private val taskDataConverter: TaskDataConverter,
                  private val taskEventSinkFactory: (Long) -> TaskEventSink) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun workOnTask(taskId: Long, taskTypeId: String, taskDataJson: String) {

        val taskType = taskTypeRegistry[taskTypeId]

        workOnTask(taskId, taskType, taskDataJson)
    }

    private fun <T> workOnTask(taskId: Long, taskType: TaskType<T>, taskDataJson: String) {

        logger.debug("Working on {} task with id {}", taskType.typeId, taskId)

        val elapsed = measureTimeMillis {

            val taskData = taskDataConverter.fromJson(taskDataJson, taskType.dataType)

            val eventSink = taskEventSinkFactory(taskId)

            eventSink.workStarted()

            taskType.handler(taskData, eventSink)

            eventSink.workSucceeded()

        }

        logger.debug("Finished working on task {} after {} ms", taskId, elapsed)

    }

}

package de.hartte.workqueue.tasks

import de.hartte.workqueue.events.TaskEventSink

/**
 * Handles execution of tasks retrieved from the queue.
 */
class TaskHandler(private val taskTypeRegistry: TaskTypeRegistry,
                  private val taskDataConverter: TaskDataConverter,
                  private val taskEventSinkFactory: (Long) -> TaskEventSink) {

    fun workOnTask(taskId: Long, taskTypeId: String, taskDataJson: String) {

        val taskType = taskTypeRegistry[taskTypeId]

        workOnTask(taskId, taskType, taskDataJson)
    }

    private fun <T> workOnTask(taskId: Long, taskType: TaskType<T>, taskDataJson: String) {

        val taskData = taskDataConverter.fromJson(taskDataJson, taskType.dataType)

        val eventSink = taskEventSinkFactory(taskId)

        taskType.handler(taskData, eventSink)

    }

}

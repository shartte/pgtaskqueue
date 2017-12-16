package de.hartte.workqueue.tasks

import de.hartte.workqueue.events.TaskEventSink

typealias TaskHandlerFunc<T> = (data: T, eventSink: TaskEventSink) -> Unit

/**
 * Represents a type of task that the system can execute.
 */
class TaskType<T>(
        val typeId: String,
        val dataType: Class<T>,
        val handler: TaskHandlerFunc<T>
)

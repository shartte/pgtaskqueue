package de.hartte.workqueue

class TaskQueueConfiguration(
        val queueTableName: String = "task_queue",
        val eventTableName: String = "task_events"
)

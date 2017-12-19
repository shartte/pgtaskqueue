package de.hartte.workqueue

class TaskDbConfiguration(
        val taskTableName: String = "task",
        val queueTableName: String = "task_queue",
        val eventTableName: String = "task_events"
)

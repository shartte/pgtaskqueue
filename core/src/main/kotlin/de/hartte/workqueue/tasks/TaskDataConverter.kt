package de.hartte.workqueue.tasks

interface TaskDataConverter {

    fun <T> toJson(taskData: T): String

    fun <T> fromJson(taskDataJson: String, dataType: Class<T>): T

}

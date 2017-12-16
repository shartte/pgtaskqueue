package de.hartte.workqueue.tasks

import com.fasterxml.jackson.databind.ObjectMapper

private val defaultMapper = ObjectMapper()

class JacksonDataConverter(private val mapper: ObjectMapper = defaultMapper) : TaskDataConverter {

    override fun <T> toJson(taskData: T) = mapper.writeValueAsString(taskData)!!

    override fun <T> fromJson(taskDataJson: String, dataType: Class<T>): T {
        return mapper.readValue<T>(taskDataJson, dataType)
    }

}

package de.hartte.workqueue.tasks

import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

/**
 * Maintains a registry of registered task types, their properties and the strategy used for working on such tasks.
 */
class TaskTypeRegistry {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val lock = ReentrantReadWriteLock()

    private val readLock = lock.readLock()

    private val writeLock = lock.writeLock()

    private val registry = HashMap<String, TaskType<*>>()

    inline fun <reified T> register(typeId: String, noinline handler: TaskHandlerFunc<T>) =
            TaskType(typeId, T::class.java, handler).also {
                register(it)
            }

    fun registerAmbientTasks() {

        val serviceLoader = ServiceLoader.load(TaskTypeRegistrator::class.java)

        for (registrator in serviceLoader) {
            logger.debug("Registering types from $registrator")
            registrator.registerTaskTypes(this)
        }

    }

    fun register(taskType: TaskType<*>) {

        writeLock.withLock {
            val registeredTask = registry[taskType.typeId]
            if (registeredTask != null && registeredTask != taskType) {
                throw IllegalStateException("A task type with id ${taskType.typeId} is already registered.")
            }

            registry[taskType.typeId] = taskType
        }

    }

    val taskTypes: List<TaskType<*>>
        get() {
            readLock.withLock {
                return registry.values.toList()
            }
        }

    operator fun get(typeId: String): TaskType<*> {
        readLock.withLock {
            return registry[typeId] ?: throw IllegalStateException("Unregistered task type: '$typeId'")
        }
    }

}

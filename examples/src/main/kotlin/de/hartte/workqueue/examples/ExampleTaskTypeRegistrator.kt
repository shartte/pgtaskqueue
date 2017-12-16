package de.hartte.workqueue.examples

import de.hartte.workqueue.tasks.TaskTypeRegistrator
import de.hartte.workqueue.tasks.TaskTypeRegistry

class ExampleTaskTypeRegistrator : TaskTypeRegistrator {

    override fun registerTaskTypes(registry: TaskTypeRegistry) {

        registry.register<GenerateReportArgs>("generate_report") { taskData, eventSink ->

        }

    }

}

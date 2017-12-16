package de.hartte.workqueue.events

interface TaskEventSink {

    fun workStarted()

    fun info(message: String)

    fun warning(message: String)

    fun warning(message: String, e: Throwable)

    fun error(message: String)

    fun error(message: String, e: Throwable)

    fun progressStage(stage: String)

    fun progress(completedSteps: Int, totalSteps: Int)

    fun workSucceeded()

    fun workFailed(message: String)

    fun workFailed(message: String, e: Throwable)

}

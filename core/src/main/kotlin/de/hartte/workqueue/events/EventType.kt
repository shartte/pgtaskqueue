package de.hartte.workqueue.events

enum class EventType {

    WORK_START,
    MESSAGE_INFO,
    MESSAGE_WARNING,
    MESSAGE_ERROR,
    PROGRESS_STAGE,
    PROGRESS,
    WORK_SUCCESS,
    WORK_FAILURE

}
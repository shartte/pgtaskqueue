package de.hartte.workqueue

interface CancelableWork {

    fun work()

    fun abort() {
    }

}

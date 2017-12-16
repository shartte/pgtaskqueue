package de.hartte.workqueue

import java.util.*

interface BackOffStrategy {

    fun failAndBackOff(): Long

}

class ConstantBackOffStrategy(private val waitTime: Long) : BackOffStrategy {

    override fun failAndBackOff() = waitTime

}

class ExponentialBackOffStrategy(private val baseWaitTime: Long,
                                 private val maxWaitTime: Long) : BackOffStrategy {

    private var failures: Int = 0

    override fun failAndBackOff(): Long {
        failures++
        return Math.min(maxWaitTime, baseWaitTime * Math.pow(2.0, failures.toDouble()).toLong())
    }

}

class DecorrelatedJitterBackOffStrategy(private val baseWaitTime: Int,
                                        private val maxWaitTime: Int) : BackOffStrategy {

    private var sleep = baseWaitTime

    private val random = Random()

    override fun failAndBackOff(): Long {
        sleep = Math.min(maxWaitTime, baseWaitTime + random.nextInt(sleep * 3 - baseWaitTime))
        return sleep.toLong()
    }

}

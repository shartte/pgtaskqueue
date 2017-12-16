package de.hartte.workqueue

import org.slf4j.LoggerFactory
import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

class BackOffWorkWrapper<T>(private val supplier: Supplier<T>,
                                     private val backOffStrategySupplier: () -> BackOffStrategy) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val cancelLatch = CountDownLatch(1)

    fun get(): T {

        val backOff = backOffStrategySupplier()
        var failures = 0

        while (!cancelLatch.await(0, TimeUnit.MILLISECONDS)) {

            try {
                return supplier.get()
                        .apply {
                            if (failures > 0) {
                                logger.info("Succeeded after {} failures.", failures)
                            }
                        }
            } catch (e: Throwable) {
                failures++
                val waitPeriod = backOff.failAndBackOff()
                logger.info("Failed $failures time(s). Retrying after $waitPeriod ms.", e)
                cancelLatch.await(waitPeriod, TimeUnit.MILLISECONDS)
            }

        }

        throw CancellationException()

    }

    fun abort() {
        cancelLatch.countDown()
    }

}

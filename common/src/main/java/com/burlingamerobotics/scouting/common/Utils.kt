package com.burlingamerobotics.scouting.common

import java.util.concurrent.Executors

object Utils {

    /**
     * An executor for performing simple I/O tasks.
     */
    val ioExecutor = Executors.newScheduledThreadPool(10)

    //val ioExecutor = ThreadPoolExecutor(8, 16, 10L, TimeUnit.SECONDS, LinkedBlockingDeque<Runnable>())

}
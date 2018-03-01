package com.burlingamerobotics.scouting

import java.util.concurrent.*

object Utils {

    /**
     * An executor for performing simple I/O tasks.
     */
    val ioExecutor = Executors.newCachedThreadPool()

    //val ioExecutor = ThreadPoolExecutor(8, 16, 10L, TimeUnit.SECONDS, LinkedBlockingDeque<Runnable>())

}
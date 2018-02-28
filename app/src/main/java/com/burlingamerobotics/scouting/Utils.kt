package com.burlingamerobotics.scouting

import java.util.concurrent.Executors

object Utils {

    /**
     * An executor for performing simple I/O tasks.
     */
    val ioExecutor = Executors.newCachedThreadPool()

}
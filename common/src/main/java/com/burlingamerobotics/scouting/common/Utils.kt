package com.burlingamerobotics.scouting.common

import java.io.*
import java.util.concurrent.Executors

object Utils {

    /**
     * An executor for performing simple I/O tasks.
     */
    val ioExecutor = Executors.newScheduledThreadPool(10)
    //val ioExecutor = ThreadPoolExecutor(8, 16, 10L, TimeUnit.SECONDS, LinkedBlockingDeque<Runnable>())

    internal val bos = ByteArrayOutputStream()
    internal val oos = ObjectOutputStream(Utils.bos)

    fun close() {
        bos.close()
        oos.close()
    }

}

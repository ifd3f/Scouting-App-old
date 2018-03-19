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

/**
 * Converts your (hopefully serializable) object into a bytearray.
 */
fun Any.serializedByteArray(): ByteArray {
    Utils.oos.writeObject(this)
    val out = Utils.bos.toByteArray()
    Utils.bos.reset()
    return out
}

/**
 * Converts your bytearray into a serializable object.
 */
fun ByteArray.deserialized(): Any {
    val bis = ByteArrayInputStream(this)
    val out = ObjectInputStream(bis).use {
        it.readObject()
    }
    bis.close()
    return out
}

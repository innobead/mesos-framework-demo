package example.mesos.actor

import example.mesos.ObjectManageable

interface Actor<T> : ObjectManageable {
    fun onMessage(message: T)

    fun onError(e: Throwable) {
        logger.error("Error!", e)
    }
}
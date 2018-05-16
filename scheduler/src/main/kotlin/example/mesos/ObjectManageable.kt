package example.mesos

import example.mesos.common.Loggable

interface ObjectManageable : Loggable {
    fun onStart() {
        logger.info("Starting ${this::class.qualifiedName}")
    }

    fun onStop() {
        logger.info("Stopping ${this::class.qualifiedName}")
    }
}
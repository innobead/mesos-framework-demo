package example.mesos

import example.mesos.common.Loggable
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlin.reflect.KClass

object ObjectRegistry : Loggable {
    val objects: MutableMap<String, Any> = mutableMapOf()

    fun <T : Any> registry(kClass: KClass<T>, service: () -> Any) {
        val name = kClass.qualifiedName!!

        if (name !in objects) {
            logger.info("Registering ${kClass.qualifiedName}")

            val obj = service()

            if (obj is ObjectManageable) {
                obj.onStart()
            }

            objects[name] = obj
        }
    }

    inline fun <reified T> get(): T? {
        val name = T::class.qualifiedName!!

        return objects[name] as? T
    }

    inline fun <reified A, B> getWithType(): B? {
        val name = A::class.qualifiedName!!

        @Suppress("UNCHECKED_CAST")
        return objects[name] as? B
    }

    fun stop() {
        objects.values.forEach {
            if (it is ObjectManageable) {
                it.onStop()
            } else if (it is SendChannel<*>) {
                it.close()
            }
        }
    }
}
package example.mesos.actor

import example.mesos.ObjectRegistry
import example.mesos.common.Loggable
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineName
import kotlinx.coroutines.experimental.NonCancellable
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.withContext


object ActorRegistry : Loggable {

    fun <T : BaseMessage> register(obj: Actor<T>) {
        ObjectRegistry.registry(obj::class) {
            actor<T>(
                    CommonPool + CoroutineName(obj::class.simpleName!!),
                    capacity = Channel.UNLIMITED
            ) {
                obj.onStart()

                try {
                    for (message in channel) {
                        try {
                            withContext(NonCancellable) {
                                obj.onMessage(message)
                            }
                        } catch (e: Exception) {
                            logger.error("Failed to process message $message", e)
                        }
                    }
                } catch (e: Throwable) {
                    obj.onError(e)
                } finally {
                    obj.onStop()
                }
            }
        }
    }

}
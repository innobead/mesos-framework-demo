package example.mesos

interface ServiceAware

inline fun <reified T> ServiceAware.getService(): T? = ObjectRegistry.get<T>()

inline fun <reified A, B> ServiceAware.getActor(): B? = ObjectRegistry.getWithType<A, B>()

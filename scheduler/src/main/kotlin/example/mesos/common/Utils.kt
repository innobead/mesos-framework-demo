package example.mesos.common

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KLoggable
import mu.KLogger
import org.apache.curator.framework.CuratorFramework

val objectMapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)!!

fun Any.toJson(): String {
    return objectMapper.writeValueAsString(this)
}

interface Loggable : KLoggable {
    override val logger: KLogger
        get() = logger()
}

fun CuratorFramework.createRecursive(path: String) {
    val paths = mutableListOf<String>()

    path.split("/").filter {
        it.isNotEmpty()
    }.fold("") { accu, v ->
        "$accu/$v".also {
            paths.add(it)
        }
    }

    paths.forEach {
        if (checkExists().forPath(it) == null) {
            create().forPath(it)
        }
    }

}
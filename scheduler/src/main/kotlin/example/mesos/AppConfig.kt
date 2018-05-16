package example.mesos

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract

object AppConfig {

    data class App(
            val home: String,
            val port: Int,
            val zkNamespace: String,
            val zkHostPort: String,
            val taskDockerImageType: String,
            val taskDockerImage: String
    )

    data class Mesos(val zkEndpoint: String)

    val config = ConfigFactory.load()!!

    val app: App = config.extract("app")
    val mesos: Mesos = config.extract("mesos")
}
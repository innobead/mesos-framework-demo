package example.mesos


import example.mesos.actor.ActorRegistry
import example.mesos.actor.ClusterConfigActor
import example.mesos.common.Loggable
import example.mesos.common.toJson
import example.mesos.framework.CustomScheduler
import example.mesos.grpc.ClusterConfigGrpcService
import example.mesos.service.ClusterConfigManagement
import example.mesos.service.ClusterService
import example.mesos.service.ClusterTaskManagement
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import kotlinx.coroutines.experimental.cancelAndJoin
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.apache.mesos.MesosSchedulerDriver
import org.apache.mesos.Protos
import kotlin.concurrent.thread
import kotlin.reflect.full.createInstance

object App : Loggable {

    private const val FRAMEWORK_NAME = "custom"

    private const val EXECUTOR_USER = "root"

    val grpcServer: Server by lazy {
        val serviceClasses = listOf(
                ClusterConfigGrpcService::class
        )

        with(ServerBuilder.forPort(AppConfig.app.port)) {
            serviceClasses.forEach {
                addService(it.createInstance())
            }

            addService(ProtoReflectionService.newInstance())

            build()
        }
    }

    val mesosDriver: MesosSchedulerDriver by lazy {
        val frameworkBuilder = Protos.FrameworkInfo.newBuilder()
                .setUser(EXECUTOR_USER)
                .setName(FRAMEWORK_NAME)

        MesosSchedulerDriver(
                CustomScheduler(),
                frameworkBuilder.build(),
                AppConfig.mesos.zkEndpoint,
                false
        )
    }

    init {
        logger.info("Starting application, \n${AppConfig.toJson()}")

        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            logger.info("Stopping application")

            ObjectRegistry.stop()
        })

        logger.info("Registering services")

        mapOf(
                ClusterTaskManagement::class to { ClusterService },
                ClusterConfigManagement::class to { ClusterService }
        ).forEach { kClass, service ->
            ObjectRegistry.registry(kClass, service)
        }

        logger.info("Registering actors")

        listOf(
                ClusterConfigActor
        ).forEach {
            ActorRegistry.register(it)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val grpcJob = launch {
            with(grpcServer) {
                logger.info("Starting ${this::class.qualifiedName}")
                start()

                while (!isTerminated) {
                    delay(1000)
                }

                logger.info("Stopped ${this::class.qualifiedName}")
            }
        }

        logger.info("Starting ${this::class.qualifiedName}")
        mesosDriver.run()

        logger.info("Stopping ${this::class.qualifiedName}, ${mesosDriver.stop()}")
        Thread.sleep(500)

        runBlocking {
            grpcJob.cancelAndJoin()
        }
    }
}
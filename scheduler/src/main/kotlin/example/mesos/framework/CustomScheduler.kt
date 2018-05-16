package example.mesos.framework


import com.kizitonwose.time.seconds
import example.mesos.*
import example.mesos.common.Loggable
import example.mesos.common.toJson
import example.mesos.service.ClusterConfigManagement
import example.mesos.service.ClusterTaskManagement
import org.apache.mesos.Protos
import org.apache.mesos.Scheduler
import org.apache.mesos.SchedulerDriver
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class CustomScheduler : ServiceAware, Loggable, Scheduler {

    companion object {
        private const val EXECUTOR_NAME = "custom"
    }

    private var frameworkId: Protos.FrameworkID? = null

    private var masterInfo: Protos.MasterInfo? = null

    private val clusterTaskManagement = getService<ClusterTaskManagement>()!!

    private val clusterConfigManagement = getService<ClusterConfigManagement>()!!

    private val launchedTaskCount = AtomicInteger()

    init {
        launchedTaskCount.addAndGet(clusterTaskManagement.getTasks().size)
    }

    /**
     * custom executor, no need
     */
    /*private val executorInfo: Protos.ExecutorInfo by lazy {
        Protos.ExecutorInfo.newBuilder()
                .setExecutorId(Protos.ExecutorID.newBuilder().setValue(EXECUTOR_NAME))
                .setName(EXECUTOR_NAME)
                .setType(Protos.ExecutorInfo.Type.CUSTOM)
                .setCommand(Protos.CommandInfo.newBuilder()
                        .setShell(false)
                )
                .setContainer(
                        Protos.ContainerInfo.newBuilder()
                                .setType(Protos.ContainerInfo.Type.MESOS)
                                .setMesos(Protos.ContainerInfo.MesosInfo.newBuilder()
                                        .setImage(Protos.Image.newBuilder()
                                                .setType(Protos.Image.Type.DOCKER)
                                                .setDocker(Protos.Image.Docker.newBuilder().setName(AppConfig.app.taskDockerImage))
                                                .setCached(false)
                                        )
                                )
                ).build()
    }*/

    override fun offerRescinded(driver: SchedulerDriver?, offerId: Protos.OfferID?) {
        logger.info("Rescinded offer ($offerId)")
    }

    override fun registered(driver: SchedulerDriver?, frameworkId: Protos.FrameworkID?, masterInfo: Protos.MasterInfo?) {
        logger.info("Registered to Mesos ($masterInfo), framework ($frameworkId)")

        this.frameworkId = frameworkId
        this.masterInfo = masterInfo
    }

    override fun disconnected(driver: SchedulerDriver?) {
        logger.info("Disconnected from Mesos")

        this.frameworkId = null
        this.masterInfo = null
    }

    override fun reregistered(driver: SchedulerDriver?, masterInfo: Protos.MasterInfo?) {
        logger.info("Reregistered to Mesos ($masterInfo)")
    }

    override fun error(driver: SchedulerDriver?, message: String?) {
        logger.error("Encountered error ($message)")
    }

    override fun slaveLost(driver: SchedulerDriver?, slaveId: Protos.SlaveID?) {
        logger.warn("Lost slave ($slaveId)")
    }

    override fun executorLost(driver: SchedulerDriver?, executorId: Protos.ExecutorID?, slaveId: Protos.SlaveID?, status: Int) {
        logger.warn("Lost executor ($executorId), slave ($slaveId), status ($status)")
    }

    override fun frameworkMessage(driver: SchedulerDriver?, executorId: Protos.ExecutorID?, slaveId: Protos.SlaveID?, data: ByteArray?) {
        logger.info("Received message from executor ($executorId), slave ($slaveId), data: ${data?.toJson()}")
    }

    override fun statusUpdate(driver: SchedulerDriver?, status: Protos.TaskStatus?) {
        logger.info("Receiving task status update from Mesos, ($status)")

        when (status!!.state) {
            in listOf(
                    Protos.TaskState.TASK_STARTING,
                    Protos.TaskState.TASK_RUNNING
            ) -> {
                clusterTaskManagement.updateTaskStatus(status.taskId, status)
            }

            in listOf(
                    Protos.TaskState.TASK_KILLED,
                    Protos.TaskState.TASK_FINISHED,

                    Protos.TaskState.TASK_FAILED,
                    Protos.TaskState.TASK_ERROR,
                    Protos.TaskState.TASK_DROPPED,

                    Protos.TaskState.TASK_GONE,
                    Protos.TaskState.TASK_GONE_BY_OPERATOR,
                    Protos.TaskState.TASK_UNKNOWN

            ) -> {
                launchedTaskCount.decrementAndGet()
                clusterTaskManagement.deleteTaskStatus(status.taskId)

                logger.info("Reviving offers from Mesos")
                driver!!.reviveOffers()
            }
        }

        driver!!.acknowledgeStatusUpdate(status)
    }

    override fun resourceOffers(driver: SchedulerDriver?, offers: MutableList<Protos.Offer>?) {
        val clusterConfig = clusterConfigManagement.getClusterConfig()

        val taskCount = clusterConfig?.numOfNode
        val cpuOfTask = clusterConfig?.generalConfig?.cpuOfNode
        val memoryOfTask = clusterConfig?.generalConfig?.memOfNode

        if (clusterConfig == null || launchedTaskCount.get() >= taskCount!!) {
            logger.info("No cluster config or all tasks launched (${launchedTaskCount.get()}), no need to process resource offers")

            offers!!.forEach {
                driver!!.declineOffer(it.id)
            }

            val status = driver!!.suppressOffers()
            logger.info("Suppressing offers from Mesos, status ($status)")

            return
        }

        for (offer in offers!!) {
            logger.info("Processing offer ($offer), and launched ${launchedTaskCount.get()} tasks already")

            val launch = Protos.Offer.Operation.Launch.newBuilder()

            var offerCpus = 0.0
            var offerMemory = 0.0

            for (resource in offer.resourcesList) {
                if (resource.name == MESOS_RESOURCE_CPU) {
                    offerCpus += resource.scalar.value
                } else if (resource.name == MESOS_RESOURCE_MEMORY) {
                    offerMemory += resource.scalar.value
                }
            }

            while (launchedTaskCount.get() < taskCount &&
                    offerCpus >= cpuOfTask!! &&
                    offerMemory >= memoryOfTask!!) {

                launchedTaskCount.incrementAndGet()

                val taskId = Protos.TaskID.newBuilder()
                        .setValue(UUID.randomUUID().toString())
                        .build()

                val task = Protos.TaskInfo.newBuilder()
                        .setTaskId(taskId)
                        .setName("$EXECUTOR_NAME-${taskId.value}")
                        .setSlaveId(offer.slaveId)
                        .setKillPolicy(
                                Protos.KillPolicy.newBuilder().setGracePeriod(
                                        Protos.DurationInfo.newBuilder()
                                                .setNanoseconds(30.seconds.inNanoseconds.longValue)
                                )
                        )
                        .addResources(Protos.Resource.newBuilder()
                                .setName(MESOS_RESOURCE_CPU)
                                .setType(Protos.Value.Type.SCALAR)
                                .setScalar(Protos.Value.Scalar.newBuilder().setValue(cpuOfTask)))
                        .addResources(Protos.Resource.newBuilder()
                                .setName(MESOS_RESOURCE_MEMORY)
                                .setType(Protos.Value.Type.SCALAR)
                                .setScalar(Protos.Value.Scalar.newBuilder().setValue(memoryOfTask)))
                        .setCommand(Protos.CommandInfo.newBuilder()
                                .setShell(false)
                        ).apply {
                            if (AppConfig.app.taskDockerImageType.trim().toLowerCase() == "docker") {
                                setContainer(
                                        Protos.ContainerInfo.newBuilder()
                                                .setType(Protos.ContainerInfo.Type.DOCKER)
                                                .setDocker(Protos.ContainerInfo.DockerInfo.newBuilder()
                                                        .setImage(AppConfig.app.taskDockerImage)
                                                        .setForcePullImage(true)
                                                )
                                )
                            } else {
                                setContainer(
                                        Protos.ContainerInfo.newBuilder()
                                                .setType(Protos.ContainerInfo.Type.MESOS)
                                                .setMesos(Protos.ContainerInfo.MesosInfo.newBuilder()
                                                        .setImage(Protos.Image.newBuilder()
                                                                .setType(Protos.Image.Type.DOCKER)
                                                                .setDocker(Protos.Image.Docker.newBuilder().setName(AppConfig.app.taskDockerImage))
                                                                .setCached(false)
                                                        )
                                                )
                                )
                            }
                        }
                        .build()

                launch.addTaskInfos(Protos.TaskInfo.newBuilder(task))

                offerCpus -= cpuOfTask
                offerMemory -= memoryOfTask
            }

            if (launch.taskInfosCount > 0) {
                val operations = listOf(
                        Protos.Offer.Operation.newBuilder()
                                .setType(Protos.Offer.Operation.Type.LAUNCH)
                                .setLaunch(launch)
                                .build()
                )

                val offerIds = listOf(offer.id)

                if (driver!!.acceptOffers(offerIds, operations, Protos.Filters.getDefaultInstance()) == Protos.Status.DRIVER_RUNNING) {
                    logger.info("Accepting offers ($offerIds)")

                    launch.taskInfosList.forEach {
                        clusterTaskManagement.updateTaskStatus(it.taskId, null)
                    }
                }
            } else {
                logger.info("Declining offer ($offer)")
                driver!!.declineOffer(offer.id)
            }
        }
    }

}

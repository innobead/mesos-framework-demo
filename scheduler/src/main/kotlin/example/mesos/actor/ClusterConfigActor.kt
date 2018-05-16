package example.mesos.actor

import example.mesos.App
import example.mesos.ServiceAware
import example.mesos.getService
import example.mesos.service.ClusterConfigManagement
import example.mesos.service.ClusterTaskManagement
import org.apache.mesos.MesosSchedulerDriver
import org.apache.mesos.Protos


object ClusterConfigActor : Actor<BaseMessage>, ServiceAware {


    private val driver: MesosSchedulerDriver by lazy {
        App.mesosDriver
    }

    private val clusterConfigManagement by lazy {
        getService<ClusterConfigManagement>()!!
    }

    private val clusterTaskManagement by lazy {
        getService<ClusterTaskManagement>()!!
    }

    override fun onMessage(message: BaseMessage) {
        when (message) {
            is UpdateConfigMessage -> {
                clusterConfigManagement.updateClusterConfig(message.clusterConfig)

                val futureTaskCount = message.clusterConfig.numOfNode
                val tasks = clusterTaskManagement.getTasks()

                if (tasks.size > futureTaskCount) {
                    logger.info("Scaling down the number of cluster, current (${tasks.size}), future (${futureTaskCount})")

                    tasks.takeLast(tasks.size - futureTaskCount).forEach {
                        if (driver.killTask(it) == Protos.Status.DRIVER_RUNNING) {
                            logger.info("Killed task ($it) by scheduler cluster config change")
                        }
                    }
                }

                logger.info("Reviving offers from Mesos")
                App.mesosDriver.reviveOffers()
            }
        }
    }

}
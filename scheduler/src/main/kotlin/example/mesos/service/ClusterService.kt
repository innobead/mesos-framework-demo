package example.mesos.service

import com.google.protobuf.util.JsonFormat
import example.mesos.AppConfig
import example.mesos.ObjectManageable
import example.mesos.ServiceAware
import example.mesos.common.createRecursive
import example.mesos.proto.ClusterConfig
import example.mesos.proto.ClusterRuntimeConfig
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.mesos.Protos


object ClusterService : ObjectManageable, ServiceAware, ClusterTaskManagement, ClusterConfigManagement {

    private const val ZK_TASKS_PATH = "/tasks"
    private const val ZK_CLUSTER_PATH = "/cluster"

    private val zkClient: CuratorFramework by lazy {
        val retryPolicy = ExponentialBackoffRetry(1000, 3)
        CuratorFrameworkFactory.builder()
                .namespace(AppConfig.app.zkNamespace)
                .connectString(AppConfig.app.zkHostPort)
                .retryPolicy(retryPolicy)
                .build().apply {
                    start()
                }
    }

    override fun onStart() {
        super.onStart()

        cleanTasksStatus()
    }

    override fun onStop() {
        super.onStop()

        cleanTasksStatus()
    }

    private fun cleanTasksStatus() {
        try {
            if (zkClient.checkExists().forPath(ZK_TASKS_PATH) != null) {
                zkClient.delete().deletingChildrenIfNeeded().forPath(ZK_TASKS_PATH)
            }
        } catch (e: Exception) {
            logger.error("Failed to delete task status paths, due to $e")
        }
    }

    override val clusterRuntimeConfig: ClusterRuntimeConfig?
        get() = null

    override fun getClusterConfig(): ClusterConfig? {
        return try {
            val data = String(zkClient.data.forPath(ZK_CLUSTER_PATH))

            ClusterConfig.newBuilder().let {
                JsonFormat.parser().merge(data, it)
                it.build()
            }
        } catch (e: Exception) {
            logger.error("Failed to get cluster config, due to $e")
            null
        }
    }

    override fun updateClusterConfig(clusterConfig: ClusterConfig) {
        logger.info("Updating cluster config, $clusterConfig")

        zkClient.createRecursive(ZK_CLUSTER_PATH)
        zkClient.setData().forPath(ZK_CLUSTER_PATH, JsonFormat.printer().print(clusterConfig).toByteArray())
    }

    override fun getTasks(): List<Protos.TaskID> {
        return try {
            if (zkClient.checkExists().forPath(ZK_TASKS_PATH) != null) {
                zkClient.children.forPath(ZK_TASKS_PATH).map {
                    Protos.TaskID.newBuilder().setValue(it).build()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            logger.error("Failed to get tasks, due to $e")
            emptyList()
        }
    }

    override fun getTaskStatus(taskID: Protos.TaskID): Protos.TaskStatus? {
        return try {
            val data = String(zkClient.data.forPath("$ZK_TASKS_PATH/${taskID.value}"))

            Protos.TaskStatus.newBuilder().let {
                JsonFormat.parser().merge(data, it)
                it.build()
            }
        } catch (e: Exception) {
            logger.error("Failed to get task status ($taskID), due to $e")
            null
        }
    }

    override fun updateTaskStatus(taskID: Protos.TaskID, taskStatus: Protos.TaskStatus?) {
        if (taskStatus == null) {
            return
        }

        logger.info("Updating task ($taskID), status (${taskStatus.state})")

        try {
            val path = "$ZK_TASKS_PATH/${taskID.value}"

            zkClient.createRecursive(path)
            zkClient.setData().forPath(path, JsonFormat.printer().print(taskStatus).toByteArray())
        } catch (e: Exception) {
            logger.error("Failed to update task status ($taskID), due to $e")
        }
    }

    override fun deleteTaskStatus(taskID: Protos.TaskID) {
        logger.info("Deleting task ($taskID)")

        try {
            val path = "$ZK_TASKS_PATH/${taskID.value}"

            if (zkClient.checkExists().forPath(path) != null) {
                zkClient.delete().forPath(path)
            }
        } catch (e: Exception) {
            logger.error("Failed to delete task status ($taskID), due to $e")
        }
    }

}
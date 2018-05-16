package example.mesos.service

import example.mesos.proto.ClusterConfig
import example.mesos.proto.ClusterRuntimeConfig

interface ClusterConfigManagement {

    val clusterRuntimeConfig: ClusterRuntimeConfig?

    fun getClusterConfig(): ClusterConfig?

    fun updateClusterConfig(clusterConfig: ClusterConfig)

}
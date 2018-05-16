package example.mesos.actor

import example.mesos.proto.ClusterConfig


sealed class BaseMessage

data class UpdateConfigMessage(val clusterConfig: ClusterConfig) : BaseMessage()

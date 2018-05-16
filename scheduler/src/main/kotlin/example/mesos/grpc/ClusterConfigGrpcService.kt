package example.mesos.grpc

import com.google.protobuf.Empty
import example.mesos.ServiceAware
import example.mesos.actor.BaseMessage
import example.mesos.actor.ClusterConfigActor
import example.mesos.actor.UpdateConfigMessage
import example.mesos.getActor
import example.mesos.proto.ClusterConfig
import example.mesos.proto.ClusterRuntimeConfig
import example.mesos.service.ClusterService
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.runBlocking


class ClusterConfigGrpcService : ClusterConfigServiceGrpc.ClusterConfigServiceImplBase(), ServiceAware {

    private val actor by lazy {
        getActor<ClusterConfigActor, SendChannel<BaseMessage>>()!!
    }

    override fun updateConfig(request: ClusterConfig?, responseObserver: StreamObserver<Empty>?) {
        runBlocking {
            actor.send(UpdateConfigMessage(request!!))
        }

        responseObserver!!.onNext(Empty.getDefaultInstance())
        responseObserver.onCompleted()
    }

    override fun getConfig(request: Empty?, responseObserver: StreamObserver<ClusterConfig>?) {
        with(responseObserver!!) {
            onNext(ClusterService.getClusterConfig())
            onCompleted()
        }
    }

    override fun getRuntimeConfig(request: Empty?, responseObserver: StreamObserver<ClusterRuntimeConfig>?) {
        with(responseObserver!!) {
            onNext(ClusterService.clusterRuntimeConfig)
            onCompleted()
        }
    }
}
package example.mesos

import com.google.protobuf.Empty
import example.mesos.grpc.ClusterConfigServiceGrpc
import example.mesos.proto.ClusterConfig
import io.grpc.ManagedChannelBuilder

object AppClient {

    @JvmStatic
    fun main(array: Array<String>) {
        val channel = ManagedChannelBuilder.forAddress("localhost", AppConfig.app.port)
                .usePlaintext()
                .build()

        val service = ClusterConfigServiceGrpc.newBlockingStub(channel)

        val clusterConfig = ClusterConfig.newBuilder()
                .setGeneralConfig(
                        ClusterConfig.GeneralConfig.newBuilder().setCpuOfNode(0.2)
                                .setMemOfNode(32.0)
                )
                .setNumOfNode(2)
                .build()

        service.updateConfig(clusterConfig)

        println(service.getConfig(Empty.getDefaultInstance()))
    }

}
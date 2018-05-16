package example.mesos.service

import org.apache.mesos.Protos

interface ClusterTaskManagement {

    fun getTasks(): List<Protos.TaskID>

    fun getTaskStatus(taskID: Protos.TaskID): Protos.TaskStatus?

    fun updateTaskStatus(taskID: Protos.TaskID, taskStatus: Protos.TaskStatus?)

    fun deleteTaskStatus(taskID: Protos.TaskID)

}
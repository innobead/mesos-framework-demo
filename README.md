## Goals 
- Setup a Mesos development environment including Mesos master & agent, Zookeeper w/ exhibitor, Marathon      
- Create a Mesos framework/scheduler, use default command executor, then launch scalable tasks running   

## Demo
[![Demo Video](http://img.youtube.com/vi/FYWyFBmAv-Q/0.jpg)](https://www.youtube.com/watch?v=FYWyFBmAv-Q)
      
## Prerequisites 
- 3 CPU, 4GB Memory for Docker
- 2 CPU, 2GB Memory for Vagrant Mesos master
- 2 CPU, 4GB Memory for Vagrant Mesos agent
- docker      
- jq      
- Java, Gradle & python3      
- vagrant
- virtualbox

## Tutorial 
### Docker
1. Build Docker images including framework and task      
``` make buildDocker ``` 

2. Start Mesos docker environment    
``` make startMesos ``` 

3. Push built Docker images to the docker registry in docker-compose env      
``` make pushDocker ``` 

4. Start the custom Mesos scheduler w/o any configuration, so no tasks running      
> Go to Marahton or Mesos UI to see if the scheduler running    
 ``` make startScheduler ``` 

5. Update the config of Mesos scheduler, then the num of tasks will be launched accordingly        
> Check Mesos console to see if all tasks running by the scheduler. If encountering error due to network issue, please retry.    
  For scale up - by changing Makefile 'updateScheulder' target    
  `num_of_node: 5`    
  For scale down - by changing Makefile 'updateScheulder' target    
`num_of_node: 1`   
  
``` make updateScheduler ```   

### Vagrant
1. Launch 3 nodes environment 

```
vagrant up
```

## Notes  
- Unable to run default or custom executor, it would encounter cgroup and some runtime issue.    
- Limited Mesos isolators are able to use in Docker environment    
- *org.apache.mesos.Scheduler* needs thread-safe, because in Mesos runtime, there are concurrent call resourceOffered    
- TaskInfo config is a little complicated due to different executor strategy (default command executor, default executor and custom executor)    
    
## References   
- [Mesos executor explanation](https://allegro.tech/2018/01/mesos_executor.html)    
    
- Four types of executors  
   - Command Executor – Speaks V0 API and is capable of running only a single task.    
   - Docker Executor – Similar to command executor but launches a docker container instead of a command.    
   - Default Executor – Introduced in Mesos 1.0 release. Similar to command executor but speaks V1 API and is capable of running pods (aka task groups).    
   - Custom Executor – Above executors are built into Mesos. A custom executor is written by a user to handle custom workloads. \It can use V0 or V1 API and can run single or multiple tasks depending on implementation. In this article we are focusing on our custom implementation and what we achieved with it.
   

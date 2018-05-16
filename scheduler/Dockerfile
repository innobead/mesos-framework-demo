FROM centos:7

RUN yum install -y epel-release && \
    yum update -y && \
    yum install -y java-1.8.0-openjdk which && \
    curl -O http://repos.mesosphere.com/el/7/x86_64/RPMS/mesos-1.5.0-2.0.1.el7.x86_64.rpm && \
    yum install -y mesos-*.rpm && \
    yum clean all

ENV APP_ZK_HOSTPORT=zookeeper:2181
ENV MESOS_ZK_ENDPOINT=zk://zookeeper:2181/mesos
ENV MESOS_SCHEDULER_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
ENV LD_LIBRARY_PATH=/usr/local/lib
ENV APP_TASK_DOCKER_IMAGE_TYPE=docker
ENV APP_TASK_DOCKER_IMAGE=docker.registry/mesos/task:latest
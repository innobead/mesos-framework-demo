#!/usr/bin/env bash
## /etc/systemd/system/dcos-mesos-slave.service
# [Unit]
# Description=Mesos Agent: distributed systems kernel agent
#
# [Service]
# Restart=always
# StartLimitInterval=0
# RestartSec=5
# KillMode=control-group
# Delegate=true
# LimitNOFILE=infinity
# TasksMax=infinity
# SyslogIdentifier=mesos-agent
# EnvironmentFile=/opt/mesosphere/environment
# EnvironmentFile=/opt/mesosphere/etc/mesos-slave-common
# EnvironmentFile=/opt/mesosphere/etc/mesos-slave
# EnvironmentFile=/opt/mesosphere/etc/proxy.env
# EnvironmentFile=-/opt/mesosphere/etc/mesos-slave-common-extras
# EnvironmentFile=-/var/lib/dcos/mesos-slave-common
# EnvironmentFile=-/var/lib/dcos/mesos-resources
# EnvironmentFile=-/run/dcos/etc/mesos-slave
# ExecStartPre=/bin/ping -c1 ready.spartan
# ExecStartPre=/opt/mesosphere/bin/bootstrap dcos-mesos-slave
# ExecStartPre=/opt/mesosphere/bin/make_disk_resources.py /var/lib/dcos/mesos-resources
# ExecStartPre=/bin/bash -c 'for i in /proc/sys/net/ipv4/conf/*/rp_filter; do echo 2 > $i; echo -n "$i: "; cat $i; done'
# ExecStart=/opt/mesosphere/packages/mesos--21658bb088b96ea08e8e00d62d0b87c52e01f511/bin/start_mesos.sh /opt/mesosphere/packages/mesos--21658bb088b96ea08e8e00d62d0b87c52e01f511/bin/mesos-agent

MESOS_MASTER=zk://zk-1.zk:2181,zk-2.zk:2181,zk-3.zk:2181,zk-4.zk:2181,zk-5.zk:2181/mesos
MESOS_CONTAINERIZERS=docker,mesos
MESOS_EXTERNAL_LOG_FILE=/var/log/mesos/mesos-agent.log
MESOS_MODULES_DIR=/opt/mesosphere/etc/mesos-slave-modules
MESOS_CONTAINER_LOGGER=com_mesosphere_mesos_JournaldLogger
MESOS_ISOLATION=cgroups/cpu,cgroups/mem,cgroups/blkio,disk/du,network/cni,filesystem/linux,docker/runtime,docker/volume,volume/sandbox_path,volume/secret,posix/rlimits,linux/capabilities,com_mesosphere_MetricsIsolatorModule,cgroups/devices,gpu/nvidia,namespaces/pid
MESOS_DOCKER_VOLUME_CHECKPOINT_DIR=/var/lib/mesos/isolators/docker/volume
MESOS_IMAGE_PROVIDERS=docker
MESOS_NETWORK_CNI_CONFIG_DIR=/opt/mesosphere/etc/dcos/network/cni
MESOS_NETWORK_CNI_PLUGINS_DIR=/opt/mesosphere/active/cni/:/opt/mesosphere/active/dcos-cni/:/opt/mesosphere/active/mesos/libexec/mesos
MESOS_WORK_DIR=/var/lib/mesos/slave
MESOS_SLAVE_SUBSYSTEMS=cpu,memory
MESOS_LAUNCHER_DIR=/opt/mesosphere/active/mesos/libexec/mesos
MESOS_EXECUTOR_ENVIRONMENT_VARIABLES=file:///opt/mesosphere/etc/mesos-executor-environment.json
MESOS_EXECUTOR_REGISTRATION_TIMEOUT=10mins
MESOS_RECONFIGURATION_POLICY=additive
MESOS_RECOVERY_TIMEOUT=24hrs
MESOS_CGROUPS_ENABLE_CFS=true
MESOS_CGROUPS_LIMIT_SWAP=false
MESOS_DISALLOW_SHARING_AGENT_PID_NAMESPACE=true
MESOS_DOCKER_REMOVE_DELAY=1hrs
MESOS_DOCKER_STOP_TIMEOUT=20secs
MESOS_DOCKER_STORE_DIR=/var/lib/mesos/slave/store/docker
MESOS_GC_DELAY=2days
MESOS_HOSTNAME_LOOKUP=false
MESOS_DEFAULT_CONTAINER_DNS=file:///opt/mesosphere/etc/mesos-slave-dns.json
MESOS_IMAGE_GC_CONFIG=file:///opt/mesosphere/etc/mesos-slave-image-gc-config.json
GLOG_drop_log_memory=false
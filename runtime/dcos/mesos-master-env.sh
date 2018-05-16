#!/usr/bin/env bash
## /etc/systemd/system/dcos-mesos-master.service
#[Unit]
#Description=Mesos Master: distributed systems kernel
#[Service]
#Restart=always
#StartLimitInterval=0
#RestartSec=15
#LimitNOFILE=infinity
#TasksMax=infinity
#PermissionsStartOnly=True
#SyslogIdentifier=mesos-master
#EnvironmentFile=/opt/mesosphere/environment
#EnvironmentFile=/opt/mesosphere/etc/mesos-master
#EnvironmentFile=/opt/mesosphere/etc/proxy.env
#EnvironmentFile=-/opt/mesosphere/etc/mesos-master-provider
#EnvironmentFile=-/opt/mesosphere/etc/mesos-master-extras
#EnvironmentFile=-/run/dcos/etc/mesos-master
#ExecStartPre=/bin/ping -c1 ready.spartan
#ExecStartPre=/bin/bash -c 'for i in /proc/sys/net/ipv4/conf/*/rp_filter; do echo 2 > $i; echo -n "$i: "; cat $i; done'
#ExecStartPre=/opt/mesosphere/bin/bootstrap dcos-mesos-master
#ExecStart=/opt/mesosphere/packages/mesos--21658bb088b96ea08e8e00d62d0b87c52e01f511/bin/start_mesos.sh /opt/mesosphere/packages/mesos--21658bb088b96ea08e8e00d62d0b87c52e01f511/bin/mesos-master

MESOS_EXTERNAL_LOG_FILE=/var/lib/dcos/mesos/log/mesos-master.log
MESOS_MODULES_DIR=/opt/mesosphere/etc/mesos-master-modules
MESOS_REGISTRY_STORE_TIMEOUT=60secs
MESOS_REGISTRY_FETCH_TIMEOUT=60secs
MESOS_REGISTRY_STRICT=false
MESOS_SLAVE_REMOVAL_RATE_LIMIT=1/20mins
MESOS_OFFER_TIMEOUT=2mins
MESOS_WORK_DIR=/var/lib/dcos/mesos/master
MESOS_ZK=zk://zk-1.zk:2181,zk-2.zk:2181,zk-3.zk:2181,zk-4.zk:2181,zk-5.zk:2181/mesos
MESOS_WEIGHTS=
MESOS_QUORUM=1
MESOS_HOSTNAME_LOOKUP=false
MESOS_MAX_SLAVE_PING_TIMEOUTS=20
MESOS_FAIR_SHARING_EXCLUDED_RESOURCE_NAMES=gpus
MESOS_FILTER_GPU_RESOURCES=true


GLOG_drop_log_memory=false
LIBPROCESS_NUM_WORKER_THREADS=16
SASL_PATH=/opt/mesosphere/lib/sasl2
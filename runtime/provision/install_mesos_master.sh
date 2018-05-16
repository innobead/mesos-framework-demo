#!/usr/bin/env bash

sudo su

#systemctl status mesos-master >/dev/null 2>&1 && echo "Mesos Master already installed" && exit 0

echo "Installing Mesos Master"

artifact=mesos-1.5.0-2.0.1.el7.x86_64.rpm

if ! [ -f "/vagrant/downloads/${artifact}" ]; then
    echo "Downloading Mesos artifact"

    # https://open.mesosphere.com/downloads/mesos/
    curl -sSLkO http://repos.mesosphere.com/el/7/x86_64/RPMS/${artifact}
    mv ${artifact} /vagrant/downloads/${artifact}
fi

yum install -y /vagrant/downloads/${artifact}

set -o errexit
set -o nounset
set -o pipefail

systemctl daemon-reload

systemctl restart mesos-master
systemctl enable mesos-master

## FIXME env variables (most of them, not working, due to /usr/bin/mesos-init-wrapper wrong implementation)
#/etc/default/mesos          # For both slave and master.
#/etc/default/mesos-master   # For the master only.
#/etc/default/mesos-slave    # For the slave only.

## Use options to set w/o issues
# options
echo "zk://mesos.master1:2181/mesos" > /etc/mesos/zk

# master options
echo "/var/lib/mesos-master" > /etc/mesos-master/work_dir
#echo "$(hostname)" > /etc/mesos-master/ip
echo "$(hostname)" > /etc/mesos-master/hostname
echo "mesos" > /etc/mesos-master/cluster

#    Adding attributes and resources to the slaves is slightly more granular.
#    Although you can pass them all at once with files called 'attributes' and
#    'resources', you can also set them by creating files under directories
#    labeled 'attributes' or 'resources':
#
#      echo north-west > /etc/mesos-slave/attributes/rack

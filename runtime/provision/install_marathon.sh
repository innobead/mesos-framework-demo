#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

sudo su

systemctl status marathon >/dev/null 2>&1 && echo "Marathon already installed" && exit 0

artifact=marathon.tgz

if ! [ -f "/vagrant/downloads/${artifact}" ]; then
    echo "Downloading Marathon"

    curl -sSLk -o ${artifact} https://downloads.mesosphere.com/marathon/releases/1.6.322/marathon-1.6.322-2bf46b341.tgz
    mv ${artifact} /vagrant/downloads/${artifact}
fi

echo "Installing Marathon"

mkdir -p /opt/marathon
tar -zxvf /vagrant/downloads/${artifact} -C /opt/marathon --strip-components=1

cat <<EOT > /usr/lib/systemd/system/marathon.service
[Unit]
Description=Mesos Marathon
After=network.target mesos-master.service
Wants=network.target mesos-master.service

[Service]
ExecStart=/opt/marathon/bin/marathon --master zk://mesos.master1:2181/mesos --zk zk://mesos.master1:2181/marathon
Restart=always

[Install]
WantedBy=multi-user.target
EOT

systemctl daemon-reload
systemctl restart marathon
systemctl enable marathon
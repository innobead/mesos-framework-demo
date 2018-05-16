#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

sudo su

systemctl status zookeeper >/dev/null 2>&1 && echo "Zookeeper already installed" && exit 0

artifact=zookeeper-3.4.12.tar.gz

if ! [ -f "/vagrant/downloads/${artifact}" ]; then
    echo "Downloading Zookeeper"

    curl -sSLkO http://apache.stu.edu.tw/zookeeper/current/${artifact}
    mv ${artifact} /vagrant/downloads/${artifact}
fi

echo "Installing Zookeeper"

mkdir -p /opt/zookeeper
tar -zxvf /vagrant/downloads/${artifact} -C /opt/zookeeper --strip-components=1

mkdir -p /var/zookeeper
echo 1 > /var/zookeeper/myid

cat <<EOT > /opt/zookeeper/conf/zoo.cfg
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/var/zookeeper
clientPort=2181
server.1=$(hostname):2888:3888
EOT

cat <<EOT > /usr/lib/systemd/system/zookeeper.service
[Unit]
Description=Zookeeper
After=network.target
Wants=network.target

[Service]
Environment=ZOO_LOG_DIR=/var/log/zookeeper
ExecStart=/opt/zookeeper/bin/zkServer.sh start-foreground
Restart=always

[Install]
WantedBy=multi-user.target
EOT

systemctl daemon-reload
systemctl restart zookeeper
systemctl enable zookeeper

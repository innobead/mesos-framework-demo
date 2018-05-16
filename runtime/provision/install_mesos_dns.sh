#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

sudo su

systemctl status mesosdns >/dev/null 2>&1 && echo "Mesos DNS already installed" && exit 0

echo "Downloading Mesos DNS"

go get github.com/mesosphere/mesos-dns
mkdir -p /opt/mesosdns/bin
export GOPATH=$HOME/go
mv $GOPATH/bin/mesos-dns /opt/mesosdns/bin/

echo "Installing Mesos DNS"

cat <<EOT > /opt/mesosdns/config.json
{
  "zk": "zk://mesos.master1:2181/mesos",
  "masters": ["127.0.0.1:5050"],
  "refreshSeconds": 60,
  "ttl": 60,
  "domain": "mesos",
  "port": 53,
  "resolvers": ["8.8.8.8", "8.8.4.4"],
  "timeout": 5,
  "httpon": true,
  "dsnon": true,
  "httpport": 8123,
  "externalon": true,
  "listener": "0.0.0.0",
  "SOAMname": "root.ns1.mesos",
  "SOARname": "ns1.mesos",
  "SOARefresh": 60,
  "SOARetry":   600,
  "SOAExpire":  86400,
  "SOAMinttl": 60
}
EOT

cat <<EOT > /usr/lib/systemd/system/mesosdns.service
[Unit]
Description=Mesos DNS
After=network.target
Wants=network.target

[Service]
ExecStart=/opt/mesosdns/bin/mesos-dns -config=/opt/mesosdns/config.json
Restart=always

[Install]
WantedBy=multi-user.target
EOT

systemctl daemon-reload
systemctl restart mesosdns
systemctl enable mesosdns

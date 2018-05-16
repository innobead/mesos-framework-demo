#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

sudo su

systemctl status exhibitor >/dev/null 2>&1 && echo "Exhibitor already installed" && exit 0

echo "Installing Exhibitor"

gradle -b /vagrant/exhibitor/build.gradle shadowJar

mkdir -p /opt/exhibitor
cp /vagrant/exhibitor/build/libs/exhibitor-1.6.0-all.jar /opt/exhibitor/exhibitor.jar

cat <<EOT > /usr/lib/systemd/system/exhibitor.service
[Unit]
Description=Exhibitor
After=network.target zookeeper.service
Wants=network.target zookeeper.service

[Service]
ExecStart=/bin/java -jar /opt/exhibitor/exhibitor.jar -c file --port 8081
Restart=always

[Install]
WantedBy=multi-user.target
EOT

systemctl daemon-reload
systemctl restart exhibitor
systemctl enable exhibitor

#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

sudo su

mkdir -p /vagrant/downloads

if ! [ -x "$(command -v java)" ]; then
    echo "Installing java"

    yum install -y java-1.8.0-openjdk yum-utils golang unzip zip git net-tools bind-utils
fi

if ! [ -x "$(command -v gradle)" ]; then
    echo "Installing gradle"

    curl -sSLkO https://downloads.gradle.org/distributions/gradle-4.7-bin.zip
    mkdir -p /opt/gradle
    unzip -o -d /opt/gradle gradle-4.7-bin.zip
    rm -f gradle-4.7-bin.zip

    ln -s /opt/gradle/gradle-4.7/bin/gradle /usr/bin/gradle
fi

if ! [ -x "$(command -v docker)" ]; then
    echo "Installing Docker"

    yum install -y epel-release
    yum update -y
    curl -fsSL https://get.docker.com/ | sh

    systemctl daemon-reload
    systemctl restart docker
    systemctl enable docker

    usermod -aG docker $(whoami)
fi
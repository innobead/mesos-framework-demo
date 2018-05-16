# Vagrant

## Install vagrant-vbguest to support synced folder
```
vagrant plugin install vagrant-vbguest
vagrant plugin install vagrant-hostmanager
```
# Mesos
## Reset
```
rm -rf /var/lib/mesos-master/*
rm -rf /var/lib/mesos-slave/*

systemctrl restart mesos-master
systemctrl restart mesos-slave
```

# Exhibitor

## Build exhibitor shadow jar
```
cd exhibitor
gradle shadowJar
```

# Docker 
## Generate cert and key
```
openssl req -x509 -nodes -newkey rsa:4096 -sha256 -keyout server.key -out server.crt -subj "/CN=docker.registry" -days 3650
```
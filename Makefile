.PHONY: help \
        buildDocker \
        pushDocker \
        startMesos \
        updateMesosRuntime \
        stopMesos \
        getRDocker \
        runGrpcCli \
        pingService \
        startScheduler \
        updateScheduler

.DEFAULT_GOAL := help

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

build: buildDocker

buildDocker: ## Build docker images. If push image, append args=-Ppush
	@gradle clean
	@gradle :scheduler:distdocker -x test $(args)
	@gradle :task:distdocker -x test $(args)

pushDocker: ## Push docker iamges
	@docker push localhost:5000/mesos/scheduler
	@docker push localhost:5000/mesos/task

startMesos:  ## Start Mesos runtime
	@docker-compose -f runtime/docker-compose.yaml build
	@docker-compose -f runtime/docker-compose.yaml up -d
	@docker-compose -f runtime/docker-compose.yaml ps

updateMesosRuntime:  ## Update Mesos runtime
	@docker-compose -f runtime/docker-compose.yaml up -d

stopMesos:  ## Stop Mesos runtime
	@docker-compose -f runtime/docker-compose.yaml down
	@docker-compose -f runtime/docker-compose.yaml kill
	@docker volume prune

getRDocker:  ## Get registered docker images
	@docker run --rm --network runtime_net gempesaw/curl-jq -sk https://docker.registry/v2/_catalog | jq '.'
	
pingService:  ## Ping service, ex: scheduler.marathon.mesos
	@docker-compose -f runtime/docker-compose.yaml exec dnsutils ping $(args)

runGrpcCli:  ## Run gRPC command
	@docker pull innobead/grpc_cli-docker
	@docker run --rm --network runtime_net --dns 172.16.30.100 -e "GRPC_VERBOSITY=DEBUG" innobead/grpc_cli-docker $(args)

startScheduler:  ## Start scheduler
	@curl -sX POST --header "Content-Type: application/json" -d @scheduler/marathon.json http://localhost:8080/v2/apps | jq '.'
	@curl -s http://localhost:8080/v2/apps | jq '.'

updateScheduler: ## Update scheduler
	$(MAKE) runGrpcCli \
		args="call scheduler.marathon.mesos:8888 ClusterConfigService.updateConfig '\
		num_of_node: 3, \
		general_config: { cpu_of_node: 0.2, mem_of_node: 32.0}\
		'"
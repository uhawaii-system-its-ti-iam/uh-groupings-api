# Makefile — UH Groupings API
#
# Provides convenience targets that ensure commands run from the correct
# working directory regardless of where `make` is invoked.

SHELL := /bin/bash
AWS_DIR := aws

# Docker Desktop check — verifies the Docker daemon is reachable.
define check_docker
	@if ! docker info >/dev/null 2>&1; then \
		echo "Error: Docker Desktop is not running. Please start Docker Desktop and try again."; \
		exit 1; \
	fi
endef

# --- AWS Infrastructure ---

.PHONY: aws-vault-setup aws-setup aws-teardown aws-stack-events aws-service-events aws-task-status aws-logs

## Ensure aws-vault is installed and the uh-groupings profile is configured
aws-vault-setup:
	$(AWS_DIR)/setup-vault.sh

## Run setup inside the Docker AWS CLI container
aws-setup:
	$(check_docker)
	cd $(AWS_DIR) && docker-compose -f docker-compose.aws.yml run --rm aws-cli bash setup.sh

## Delete all CloudFormation stacks (prompts for confirmation)
aws-teardown:
	$(check_docker)
	@echo "WARNING: This will delete all AWS resources for the project."
	@read -r -p "Are you sure? (y/n) " confirm && [ "$$confirm" = "y" ] || exit 1
	cd $(AWS_DIR) && docker-compose -f docker-compose.aws.yml run --rm aws-cli bash -c ' \
		source .env && \
		aws cloudformation delete-stack --stack-name "$${AWS_PROJECT_ID}-pipeline-$${AWS_ENV}" --region "$${AWS_REGION}" && \
		aws cloudformation delete-stack --stack-name "$${AWS_PROJECT_ID}-ecs-$${AWS_ENV}" --region "$${AWS_REGION}" && \
		aws cloudformation delete-stack --stack-name "$${AWS_PROJECT_ID}-ecr-$${AWS_ENV}" --region "$${AWS_REGION}"'

## Show CloudFormation events that failed during stack creation
aws-stack-events:
	$(check_docker)
	cd $(AWS_DIR) && docker-compose -f docker-compose.aws.yml run --rm aws-cli bash -c ' \
		source .env && \
		aws cloudformation describe-stack-events \
			--stack-name "$${AWS_PROJECT_ID}-ecs-$${AWS_ENV}" \
			--query "StackEvents[?ResourceStatus==\`CREATE_FAILED\`]" \
			--region "$${AWS_REGION}"'

## Show the most recent ECS service events
aws-service-events:
	$(check_docker)
	cd $(AWS_DIR) && docker-compose -f docker-compose.aws.yml run --rm aws-cli bash -c ' \
		source .env && \
		CLUSTER=$$(aws cloudformation describe-stacks \
			--stack-name "$${AWS_PROJECT_ID}-ecs-$${AWS_ENV}" \
			--query "Stacks[0].Outputs[?OutputKey==\`ClusterName\`].OutputValue" \
			--output text --region "$${AWS_REGION}") && \
		SERVICE=$$(aws cloudformation describe-stacks \
			--stack-name "$${AWS_PROJECT_ID}-ecs-$${AWS_ENV}" \
			--query "Stacks[0].Outputs[?OutputKey==\`ServiceName\`].OutputValue" \
			--output text --region "$${AWS_REGION}") && \
		aws ecs describe-services \
			--cluster "$${CLUSTER}" \
			--services "$${SERVICE}" \
			--query "services[0].events[0:10]" \
			--region "$${AWS_REGION}"'

## Show the stopped reason for the most recent ECS task
aws-task-status:
	$(check_docker)
	cd $(AWS_DIR) && docker-compose -f docker-compose.aws.yml run --rm aws-cli bash -c ' \
		source .env && \
		CLUSTER=$$(aws cloudformation describe-stacks \
			--stack-name "$${AWS_PROJECT_ID}-ecs-$${AWS_ENV}" \
			--query "Stacks[0].Outputs[?OutputKey==\`ClusterName\`].OutputValue" \
			--output text --region "$${AWS_REGION}") && \
		TASK=$$(aws ecs list-tasks \
			--cluster "$${CLUSTER}" \
			--query "taskArns[0]" --output text \
			--region "$${AWS_REGION}") && \
		aws ecs describe-tasks \
			--cluster "$${CLUSTER}" \
			--tasks "$${TASK}" \
			--query "tasks[0].{StoppedReason:stoppedReason,Containers:containers[*].{Name:name,Reason:reason}}" \
			--region "$${AWS_REGION}"'

## Tail CloudWatch logs for the API
aws-logs:
	$(check_docker)
	cd $(AWS_DIR) && docker-compose -f docker-compose.aws.yml run --rm aws-cli bash -c ' \
		source .env && \
		aws logs tail "/ecs/$${AWS_PROJECT_ID}" --follow --region "$${AWS_REGION}"'

# --- Application ---

.PHONY: run test test-unit test-integration test-single build build-run clean

## Remove build artifacts
clean:
	./mvnw clean

## Build WAR artifact
build:
	$(check_docker)
	./mvnw clean package

## Run the Spring Boot application
run:
	$(check_docker)
	./mvnw clean spring-boot:run

## Build then run (reuses the artifact from build, no second clean)
build-run: build
	$(check_docker)
	./mvnw spring-boot:run

## Run all tests (unit + integration)
test:
	$(check_docker)
	./mvnw clean test

## Run unit tests only (classes ending with Test)
test-unit:
	./mvnw clean test -Dtest='*Test'

## Run integration tests only (Test* classes, requires live Grouper API credentials)
test-integration:
	$(check_docker)
	./mvnw clean test -Dtest='Test*' -Dspring.profiles.active=integrationTest

## Run a single test class: make test-single CLASS=GroupPathServiceTest
test-single:
	@[ -n "$(CLASS)" ] || { echo "Usage: make test-single CLASS=ClassName"; exit 1; }
	$(check_docker)
	./mvnw clean test -Dtest=$(CLASS)

# --- Docker desktop ---

.PHONY: docker-up

## Start the full Docker stack (app + dependencies)
docker-up:
	$(check_docker)
	docker-compose up --build

# --- Help ---

.PHONY: help
help:
	@echo "UH Groupings API - Available targets:"
	@echo ""
	@echo "  AWS targets must be wrapped with aws-vault for credentials, e.g.:"
	@echo "    aws-vault exec uh-groupings -- make aws-setup"
	@echo "  See aws/README.md for details."
	@echo ""
	@echo "  AWS Infrastructure:"
	@echo "    aws-vault-setup    Install aws-vault and configure profile (one-time, no wrapper)"
	@echo "    aws-setup          Run interactive AWS setup (Docker)"
	@echo "    aws-teardown       Delete all AWS CloudFormation stacks (Docker)"
	@echo ""
	@echo "  AWS Troubleshooting:"
	@echo "    aws-stack-events   Show CloudFormation CREATE_FAILED events"
	@echo "    aws-service-events Show recent ECS service events"
	@echo "    aws-task-status    Show why the most recent ECS task stopped"
	@echo "    aws-logs           Tail the API CloudWatch logs"
	@echo ""
	@echo "  Build & Run:"
	@echo "    build              Build WAR artifact"
	@echo "    run                Run Spring Boot application"
	@echo "    build-run          Build then run"
	@echo "    clean              Remove build artifacts"
	@echo "    docker-up          Start full Docker stack"
	@echo ""
	@echo "  Testing:"
	@echo "    test               Run all tests (unit + integration)"
	@echo "    test-unit          Run unit tests only (*Test classes)"
	@echo "    test-integration   Run integration tests only (Test* classes)"
	@echo "    test-single        Run one test class (CLASS=ClassName)"
	@echo ""
	@echo "  Other:"
	@echo "    help               Show this help message"

.DEFAULT_GOAL := help

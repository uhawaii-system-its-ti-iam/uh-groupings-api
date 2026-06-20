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

.PHONY: aws-setup aws-teardown

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
	@echo "  AWS Infrastructure:"
	@echo "    aws-setup          Run interactive AWS setup (Docker)"
	@echo "    aws-teardown       Delete all AWS CloudFormation stacks (Docker)"
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

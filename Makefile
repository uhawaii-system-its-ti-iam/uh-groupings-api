# Makefile — UH Groupings API
#
# Convenience targets that run from the correct working directory regardless of
# where `make` is invoked.
#
# ---------------------------------------------------------------------------
# SANDBOX / DEVELOPMENT BRANCH
#
# This branch targets a SINGLE, SIMPLE sandbox environment only:
#   - one ECS Fargate task, one private subnet, one Availability Zone
#   - no load balancer, no public endpoint, no inbound path
#   - a NAT Gateway with a fixed Elastic IP for outbound Grouper access
#   - the API is private; the companion UI reaches it over ECS Service Connect
#   - teardown / re-setup is the normal iteration loop, not a last resort
#
# There are deliberately no promotion, blue/green, autoscaling, or multi-AZ
# targets here. A production environment is a separate future branch.
# See aws/AGENTS.md and aws/docs/AWS_ARCHITECTURE.md.
# ---------------------------------------------------------------------------

SHELL := /bin/bash
AWS_DIR := aws

# Docker Desktop check — only for targets that genuinely need the daemon
# (building/pushing a container image, or running the docker-compose stack).
# Maven build and test targets do NOT require Docker.
define check_docker
	@if ! docker info >/dev/null 2>&1; then \
		echo "Error: Docker Desktop is not running. Please start Docker Desktop and try again."; \
		exit 1; \
	fi
endef

# AWS CLI check — verifies the AWS CLI v2 is installed on the host.
define check_aws
	@if ! command -v aws >/dev/null 2>&1; then \
		echo "Error: AWS CLI v2 is not installed."; \
		echo "  macOS:  brew install awscli"; \
		echo "  Linux:  https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html"; \
		exit 1; \
	fi
endef

# Loads aws/.env and derives every resource name from the project convention
# <owner>-<project>-<env>[-suffix]. Defined once so the naming convention is not
# duplicated (and cannot drift) across targets. Use as:
#     cd $(AWS_DIR) && $(aws_names) && <command using $${CLUSTER} etc>
define aws_names
set -a && source .env && set +a && \
STACK_VPC="$${AWS_PROJECT_ID}-vpc-$${AWS_ENV}" && \
STACK_ECR="$${AWS_PROJECT_ID}-ecr-$${AWS_ENV}" && \
STACK_ECS="$${AWS_PROJECT_ID}-ecs-$${AWS_ENV}" && \
STACK_PIPELINE="$${AWS_PROJECT_ID}-pipeline-$${AWS_ENV}" && \
BASE="$${AWS_OWNER}-$${AWS_PROJECT_ID}-$${AWS_ENV}" && \
CLUSTER="$${BASE}-cluster" && \
SERVICE="$${BASE}-service" && \
LOG_GROUP="/ecs/$${BASE}"
endef

# --- AWS Infrastructure (sandbox) ---

.PHONY: aws-sso-setup aws-sso-login aws-list-vpcs aws-check-vpc aws-setup \
        aws-teardown aws-status aws-redeploy aws-stack-events \
        aws-service-events aws-task-status aws-logs aws-github-connect

## Configure the IAM Identity Center (SSO) profile and sign in. One time per
## developer (also run any time you want to authenticate explicitly). Reads the
## SSO values from aws/.env, writes the profile to ~/.aws/config if needed, and
## logs in via the browser only if there is no valid session.
aws-sso-setup:
	$(check_aws)
	cd $(AWS_DIR) && bash auth.sh

## Force a fresh SSO login (proactive refresh, even if the session is still valid)
aws-sso-login:
	$(check_aws)
	cd $(AWS_DIR) && bash auth.sh force

## Create or locate a GitHub CodeConnections connection and display its ARN.
## Run this before deploying the CodePipeline stack. Opens a browser to complete
## the OAuth handshake if needed, then polls until the connection is AVAILABLE.
aws-github-connect:
	$(check_aws)
	cd $(AWS_DIR) && bash github-connect.sh

## List VPCs in the configured account/region (from aws/.env)
aws-list-vpcs:
	$(check_aws)
	cd $(AWS_DIR) && \
		set -a && source .env && set +a && \
		echo "" && \
		echo "VPC_ID in aws/.env: $${VPC_ID}" && \
		echo "" && \
		aws ec2 describe-vpcs \
			--query "Vpcs[].{Id:VpcId,CIDR:CidrBlock,Default:IsDefault}" \
			--output table \
			--region "$${AWS_REGION}"

## Validate that the VPC in aws/.env meets project requirements
aws-check-vpc:
	$(check_aws)
	cd $(AWS_DIR) && bash check-vpc.sh

## Provision the sandbox (needs the AWS CLI plus Docker to build/push the image).
## Idempotent and re-runnable: use it to resume after a failure or to pick up
## template changes.
aws-setup:
	$(check_aws)
	$(check_docker)
	cd $(AWS_DIR) && bash setup.sh

## Show sandbox provisioning status: stack states, whether the API task is
## running, the NAT Gateway's Elastic IP (the address the UH firewall must
## allow-list), and the connection values (security group id, Service Connect
## namespace and DNS name) that the companion UI project needs in order to
## reach the API. This is the whole verification story for this branch — the API
## is not functionally exercised until the UI is deployed (see aws/AGENTS.md,
## "Verification scope").
aws-status:
	$(check_aws)
	@cd $(AWS_DIR) && $(aws_names) && \
		echo "" && \
		echo "Sandbox: $${BASE}   (region $${AWS_REGION}, tier $${APP_TIER})" && \
		echo "" && \
		echo "Grouper egress (UH firewall must allow-list this address):" && \
		aws cloudformation describe-stacks --stack-name "$${STACK_VPC}" \
			--query 'Stacks[0].Outputs[?OutputKey==`NatEipAddress` || OutputKey==`NatEipAllocationIdOut`].{Key:OutputKey,Value:OutputValue}' \
			--output table --region "$${AWS_REGION}" 2>/dev/null \
			|| echo "  (vpc stack not deployed)" && \
		echo "" && \
		echo "CloudFormation stacks:" && \
		for s in "$${STACK_VPC}" "$${STACK_ECR}" "$${STACK_ECS}" "$${STACK_PIPELINE}"; do \
			st=$$(aws cloudformation describe-stacks --stack-name "$$s" \
				--query 'Stacks[0].StackStatus' --output text \
				--region "$${AWS_REGION}" 2>/dev/null || echo "NOT_DEPLOYED"); \
			printf '  %-40s %s\n' "$$s" "$$st"; \
		done && \
		echo "" && \
		echo "ECS service:" && \
		aws ecs describe-services --cluster "$${CLUSTER}" --services "$${SERVICE}" \
			--query 'services[0].{Status:status,Running:runningCount,Desired:desiredCount,TaskDef:taskDefinition}' \
			--output table --region "$${AWS_REGION}" 2>/dev/null \
			|| echo "  (ECS service not deployed)" && \
		echo "" && \
		echo "Connection values the UI project needs (CloudFormation exports):" && \
		aws cloudformation describe-stacks --stack-name "$${STACK_ECS}" \
			--query 'Stacks[0].Outputs[].{Key:OutputKey,Value:OutputValue}' \
			--output table --region "$${AWS_REGION}" 2>/dev/null \
			|| echo "  (ECS stack not deployed)" && \
		echo "" && \
		echo "Reminder: a running task proves the image pulled and secrets resolved" && \
		echo "through the VPC endpoints. Grouper errors in the logs are expected until" && \
		echo "the UH firewall allow-lists the NAT EIP. There is no endpoint to curl." && \
		echo ""

## Force a new ECS deployment without a code change (picks up a re-pushed
## :latest image). Handy for sandbox iteration.
aws-redeploy:
	$(check_aws)
	cd $(AWS_DIR) && $(aws_names) && \
		aws ecs update-service \
			--cluster "$${CLUSTER}" --service "$${SERVICE}" \
			--force-new-deployment \
			--query 'service.{Service:serviceName,Status:status}' \
			--output table --region "$${AWS_REGION}"

## Delete all sandbox CloudFormation stacks (prompts for confirmation).
## Deletion is ordered and waited on: pipeline → ecs → ecr → vpc. Stacks that
## do not exist are skipped. Secrets Manager entries are PRESERVED so the JWT
## key survives and UI tokens are not invalidated. The NAT Gateway's Elastic IP
## survives only if NAT_EIP_ALLOCATION_ID is set in aws/.env.
aws-teardown:
	$(check_aws)
	@echo ""
	@echo "WARNING: this deletes the sandbox CloudFormation stacks (pipeline, ecs, ecr, vpc)."
	@echo "The pre-existing VPC and the Secrets Manager entries are NOT deleted."
	@echo "The NAT Gateway IS deleted; its Elastic IP survives only if"
	@echo "NAT_EIP_ALLOCATION_ID is set in aws/.env."
	@echo ""
	@read -r -p "Are you sure? (y/n) " confirm && [ "$$confirm" = "y" ] || exit 1
	@cd $(AWS_DIR) && $(aws_names) && \
		for s in "$${STACK_PIPELINE}" "$${STACK_ECS}" "$${STACK_ECR}" "$${STACK_VPC}"; do \
			if aws cloudformation describe-stacks --stack-name "$$s" \
				--region "$${AWS_REGION}" >/dev/null 2>&1; then \
				echo "Deleting $$s ..."; \
				aws cloudformation delete-stack --stack-name "$$s" --region "$${AWS_REGION}"; \
				aws cloudformation wait stack-delete-complete \
					--stack-name "$$s" --region "$${AWS_REGION}" \
					&& echo "  deleted." \
					|| { echo "  FAILED to delete $$s — see 'make aws-stack-events'"; exit 1; }; \
			else \
				echo "Skipping $$s (not deployed)."; \
			fi; \
		done && \
		echo "" && \
		echo "Teardown complete. Secrets preserved:" && \
		echo "  groupings/api/grouper-password" && \
		echo "  groupings/api/jwt-secret" && \
		echo "" && \
		if [ -n "$${NAT_EIP_ALLOCATION_ID}" ]; then \
			echo "NAT Elastic IP $${NAT_EIP_ALLOCATION_ID} preserved (set in aws/.env)."; \
			echo "The UH firewall allow-list stays valid."; \
		else \
			echo "WARNING: NAT_EIP_ALLOCATION_ID is not set in aws/.env, so the NAT"; \
			echo "Elastic IP was CloudFormation-owned and has been RELEASED. The next"; \
			echo "'make aws-setup' will allocate a different address and the UH firewall"; \
			echo "allow-list will need to be re-requested."; \
		fi && \
		echo "" && \
		echo "Re-provision with 'make aws-setup'." && \
		echo ""

## Show failed CloudFormation events across all sandbox stacks (create, update,
## and delete failures — not just CREATE_FAILED, since setup is re-runnable).
aws-stack-events:
	$(check_aws)
	@cd $(AWS_DIR) && $(aws_names) && \
		for s in "$${STACK_VPC}" "$${STACK_ECR}" "$${STACK_ECS}" "$${STACK_PIPELINE}"; do \
			if aws cloudformation describe-stacks --stack-name "$$s" \
				--region "$${AWS_REGION}" >/dev/null 2>&1; then \
				echo ""; \
				echo "=== $$s ==="; \
				aws cloudformation describe-stack-events --stack-name "$$s" \
					--query "reverse(StackEvents[?contains(ResourceStatus, 'FAILED')].[LogicalResourceId,ResourceStatus,ResourceStatusReason])" \
					--output text --region "$${AWS_REGION}" \
					| sed 's/^/  /' \
					| grep -v '^  *$$' || echo "  (no failures)"; \
			fi; \
		done; \
		echo ""

## Show the most recent ECS service events
aws-service-events:
	$(check_aws)
	cd $(AWS_DIR) && $(aws_names) && \
		aws ecs describe-services \
			--cluster "$${CLUSTER}" \
			--services "$${SERVICE}" \
			--query "services[0].events[0:10]" \
			--region "$${AWS_REGION}"

## Show the stopped reason for the most recent ECS task. The usual sandbox
## failure is ResourceInitializationError, meaning the outbound path is broken —
## image pulls and secret fetches both go out through the NAT Gateway.
aws-task-status:
	$(check_aws)
	cd $(AWS_DIR) && $(aws_names) && \
		TASK=$$(aws ecs list-tasks \
			--cluster "$${CLUSTER}" \
			--desired-status STOPPED \
			--query "taskArns[0]" --output text \
			--region "$${AWS_REGION}"); \
		if [ "$${TASK}" = "None" ] || [ -z "$${TASK}" ]; then \
			TASK=$$(aws ecs list-tasks --cluster "$${CLUSTER}" \
				--query "taskArns[0]" --output text --region "$${AWS_REGION}"); \
		fi; \
		if [ "$${TASK}" = "None" ] || [ -z "$${TASK}" ]; then \
			echo "No tasks found in $${CLUSTER}."; \
		else \
			aws ecs describe-tasks \
				--cluster "$${CLUSTER}" \
				--tasks "$${TASK}" \
				--query "tasks[0].{LastStatus:lastStatus,StoppedReason:stoppedReason,Containers:containers[*].{Name:name,Reason:reason}}" \
				--region "$${AWS_REGION}"; \
		fi

## Tail CloudWatch logs for the API
aws-logs:
	$(check_aws)
	cd $(AWS_DIR) && $(aws_names) && \
		echo "Tailing $${LOG_GROUP} (Ctrl+C to stop)" && \
		aws logs tail "$${LOG_GROUP}" --follow --region "$${AWS_REGION}"

# --- Application ---

.PHONY: run test test-unit test-integration test-single build build-run clean

## Remove build artifacts
clean:
	./mvnw clean

## Build WAR artifact
build:
	./mvnw clean package

## Run the Spring Boot application locally (localhost profile, port 8081)
run:
	./mvnw clean spring-boot:run

## Build then run (reuses the artifact from build, no second clean)
build-run: build
	./mvnw spring-boot:run

## Run unit tests. Surefire excludes **/Test*.java, so this is unit tests only
## and needs no Grouper access. Same set as test-unit.
test:
	./mvnw clean test

## Run unit tests only (*Test classes) — explicit form of the default `test`
test-unit:
	./mvnw clean test -Dtest='*Test'

## Run integration tests only (Test* classes, requires live Grouper credentials)
test-integration:
	./mvnw clean test -Dtest='Test*' -Dspring.profiles.active=integrationTest

## Run a single test class: make test-single CLASS=GroupPathServiceTest
test-single:
	@[ -n "$(CLASS)" ] || { echo "Usage: make test-single CLASS=ClassName"; exit 1; }
	./mvnw clean test -Dtest=$(CLASS)

# --- Docker desktop ---

.PHONY: docker-up docker-down

## Start the full Docker stack (app + dependencies) on port 8081
docker-up:
	$(check_docker)
	docker-compose up --build

## Stop the Docker stack
docker-down:
	$(check_docker)
	docker-compose down

# --- Help ---

.PHONY: help
help:
	@echo "UH Groupings API — SANDBOX / DEVELOPMENT branch"
	@echo ""
	@echo "  This branch provisions ONE simple sandbox environment: a single"
	@echo "  Fargate task in a single private subnet and AZ, with no load"
	@echo "  balancer and no public endpoint. Teardown and re-setup is the"
	@echo "  normal iteration loop. Production is a separate future branch."
	@echo ""
	@echo "  AWS targets authenticate via IAM Identity Center (SSO) and require"
	@echo "  the AWS CLI v2 (macOS: brew install awscli). Any aws-* target signs"
	@echo "  you in automatically when needed; set the SSO values in aws/.env"
	@echo "  first. AWS_PROFILE comes from .env, so no manual export is needed."
	@echo "  Docker is needed only for aws-setup, docker-up, and docker-down."
	@echo ""
	@echo "  See aws/README.md and aws/AGENTS.md for details."
	@echo ""
	@echo "  AWS Setup:"
	@echo "    aws-sso-setup      Configure SSO profile + sign in (also auto-runs on demand)"
	@echo "    aws-sso-login      Force a fresh SSO login (proactive refresh)"
	@echo "    aws-list-vpcs      List VPCs in the configured account/region"
	@echo "    aws-check-vpc      Validate the VPC meets project requirements"
	@echo "    aws-github-connect Create/locate a GitHub connection + display ARN for aws/.env"
	@echo "    aws-setup          Provision the sandbox (idempotent, re-runnable)"
	@echo "    aws-teardown       Delete the sandbox stacks (secrets preserved)"
	@echo ""
	@echo "  AWS Operations:"
	@echo "    aws-status         Stack status, task state, and the values the UI needs"
	@echo "    aws-redeploy       Force a new ECS deployment (re-pushed :latest)"
	@echo "    aws-logs           Tail the API CloudWatch logs"
	@echo ""
	@echo "  AWS Troubleshooting:"
	@echo "    aws-stack-events   Failed CloudFormation events, all stacks"
	@echo "    aws-service-events Recent ECS service events"
	@echo "    aws-task-status    Why the most recent ECS task stopped"
	@echo ""
	@echo "  Localhost Build & Run:"
	@echo "    build              Build WAR artifact"
	@echo "    run                Run Spring Boot application (port 8081)"
	@echo "    build-run          Build then run"
	@echo "    clean              Remove build artifacts"
	@echo "    docker-up          Start the Docker stack (port 8081)"
	@echo "    docker-down        Stop the Docker stack"
	@echo ""
	@echo "  Localhost Testing:"
	@echo "    test               Run unit tests (Test*.java excluded by surefire)"
	@echo "    test-unit          Run unit tests only (*Test classes)"
	@echo "    test-integration   Run integration tests (Test* classes, needs Grouper)"
	@echo "    test-single        Run one test class (CLASS=ClassName)"
	@echo ""
	@echo "  Other:"
	@echo "    help               Show this help message"

.DEFAULT_GOAL := help

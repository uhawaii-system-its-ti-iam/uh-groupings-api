# FOR LOCAL USE ONLY.

.PHONY: start start-mailhog start-uh-groupings-api stop-mailhog

# Stop the MailHog server if it is running
stop-mailhog:
	@echo "Stopping MailHog server..."
	@if docker ps --format '{{.Names}}' | grep -qx mailhog; then \
		docker stop mailhog; \
	else \
		echo "MailHog is not running."; \
	fi

# Start the UH Groupings API and MailHog server
start: start-uh-groupings-api

ifeq ($(CLEAN),1)

start-uh-groupings-api: start-mailhog
	@echo "Starting UH Groupings API..."
	./mvnw clean spring-boot:run

else

start-uh-groupings-api: start-mailhog
	@echo "Starting UH Groupings API..."
	./mvnw spring-boot:run

endif

start-mailhog:
	@echo "Starting MailHog server..."
	@if docker ps --format '{{.Names}}' | grep -qx mailhog; then \
		echo "MailHog is already running."; \
	elif docker ps -a --format '{{.Names}}' | grep -qx mailhog; then \
		docker start mailhog; \
	else \
		docker run -d --name mailhog \
			-p 1025:1025 \
			-p 8025:8025 \
			mailhog/mailhog; \
	fi
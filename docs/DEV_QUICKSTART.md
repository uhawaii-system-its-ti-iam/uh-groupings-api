# Local Development Quick Start

## Run Locally in 10 Minutes

**Purpose:** Get the application running on your machine for development.

**Need AWS?** See [AWS_QUICKSTART.md](./AWS_QUICKSTART.md) (initial setup) or [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md) (ongoing operations)

---

## Quick Start (3 Steps)

### 1. Create Configuration File (2 min)

```bash
# Create properties file
mkdir -p ~/.$(whoami)-conf
nano ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
```

Add your configuration:
```properties
grouper.api.url=https://grouper-dev.example.com/grouper-ws
grouper.username=your-dev-username
grouper.password=your-dev-password
jwt.secret.key=local-dev-secret
```

**Need the full template?** See [docker/README.md](../docker/README.md)

### 2. Generate Docker Environment (1 min)

```bash
./docker/dev-overrides-properties.sh
```

### 3. Start Application (1 min)

```bash
docker-compose up
```

**Expected output:**
```
uh-groupings-api | Started SpringBootWebApplication in 45.123 seconds
```

---

## Verify It Works

```bash
# Test health endpoint
curl http://localhost:8080/actuator/health

# Should return: {"status":"UP"}
```

**Open in browser:**
- Health: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html

---

## Common Commands

```bash
# Stop
docker-compose down

# Restart after code changes
docker-compose down && docker-compose up --build

# View logs
docker-compose logs -f

# Update configuration
nano ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
./docker/dev-overrides-properties.sh
docker-compose down && docker-compose up
```

---

## Troubleshooting

**Docker not running?**
```bash
open -a Docker  # macOS
# Wait 30 seconds
```

**Port 8080 in use?**
```bash
# Find what's using it
lsof -i :8080
# Kill it or change port in docker-compose.yml
```

**Properties file not found?**
```bash
# Verify file exists
ls -la ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
```

**More help:** See [docker/README.md](../docker/README.md) or [SECRETS.md](./SECRETS.md)

---

## Next Steps

✅ **You're running!** Now you can:
- Edit code and rebuild: `docker-compose up --build`
- Run tests: `./mvnw test`
- View detailed local dev guide: [docker/README.md](../docker/README.md)
- Deploy to AWS: [AWS_QUICKSTART.md](./AWS_QUICKSTART.md)

---

**Time:** ~10 minutes | **Last Updated:** 2026-06-09

---

## Prerequisites (5 minutes)

### Required Software

- **Docker Desktop** - [Download](https://www.docker.com/products/docker-desktop)
- **Git** - Should already be installed
- **Text editor** - nano, vim, VSCode, or your preference

### Verify Installation

```bash
# Check Docker
docker --version
docker ps  # Should not error

# Check Git
git --version
```

If Docker isn't running:
```bash
# macOS
open -a Docker

# Wait 30 seconds for Docker to start
```

---

## Step 1: Clone the Repository (1 minute)

```bash
# Clone the repository (if you haven't already)
cd ~/git-workspace  # or your preferred location
git clone https://github.example.com/uhawaii-system-its-ti-iam/uh-groupings-api.git
cd uh-groupings-api
```

---

## Step 2: Create Properties File (3 minutes)

### Create the Configuration Directory

```bash
# Create directory for your configuration
mkdir -p ~/.$(whoami)-conf

# Create the properties file
nano ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
```

### Add Your Configuration

Copy and paste this template, then update with your values:

```properties
# ============================================================================
# Grouper Configuration
# ============================================================================
grouper.api.url=https://grouper-dev.example.com/grouper-ws/servicesRest/json/v2_5_000
grouper.username=your-dev-username
grouper.password=your-dev-password

# ============================================================================
# Database Configuration (if needed)
# ============================================================================
# If you have a local database:
# spring.datasource.url=jdbc:postgresql://localhost:5432/groupings
# spring.datasource.username=groupings_user
# spring.datasource.password=your-local-db-password

# ============================================================================
# JWT Configuration
# ============================================================================
jwt.secret.key=local-dev-secret-change-this-to-something-unique
jwt.expiration.ms=86400000

# ============================================================================
# Email Configuration (optional for local dev)
# ============================================================================
# spring.mail.host=smtp.example.com
# spring.mail.port=587
# spring.mail.username=your-email@example.com
# spring.mail.password=your-email-password

# ============================================================================
# Vault Configuration (disable for local dev)
# ============================================================================
spring.cloud.vault.enabled=false

# ============================================================================
# Logging
# ============================================================================
logging.level.root=INFO
logging.level.edu.hawaii.its=DEBUG

# ============================================================================
# Actuator (health checks and metrics)
# ============================================================================
management.endpoints.web.exposure.include=health,info,metrics
```

**Save and exit:**
- In nano: Press `Ctrl+X`, then `Y`, then `Enter`
- In vim: Press `Esc`, type `:wq`, press `Enter`

### Secure Your Properties File

```bash
# Set restrictive permissions (owner read/write only)
chmod 600 ~/.$(whoami)-conf/uh-groupings-api-overrides.properties

# Verify
ls -la ~/.$(whoami)-conf/
```

---

## Step 3: Generate Docker Environment File (1 minute)

```bash
# Run the conversion script
./docker/dev-overrides-properties.sh

# Expected output:
# Wrote docker/.env
```

### Verify the Generated File

```bash
# Check that the file was created
ls -la docker/.env

# View the contents (optional)
cat docker/.env
```

You should see your properties converted to environment variables:
```bash
# Generated from: /Users/yourname/.yourname-conf/uh-groupings-api-overrides.properties
# Do not commit this file.

GROUPER_API_URL=https://grouper-dev.example.com/grouper-ws/servicesRest/json/v2_5_000
GROUPER_USERNAME=your-dev-username
GROUPER_PASSWORD=your-dev-password
JWT_SECRET_KEY=local-dev-secret-change-this-to-something-unique
# ... etc
```

---

## Step 4: Start the Application (3 minutes)

```bash
# Start Docker Compose
docker-compose up

# First time will take 2-3 minutes to:
# - Download base images
# - Build the application
# - Start the container
```

### Watch for Successful Startup

You should see logs similar to:
```
uh-groupings-api    | Started SpringBootWebApplication in 45.123 seconds
uh-groupings-api    | Tomcat started on port(s): 8080 (http)
```

---

## Step 5: Test the Application (1 minute)

### Open a New Terminal Window

```bash
# Test the health endpoint
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

### Test in Browser

Open your browser to:
- **Health Check:** http://localhost:8080/actuator/health
- **API Documentation:** http://localhost:8080/swagger-ui.html
- **Application Info:** http://localhost:8080/actuator/info

---

## 🎉 Success!

Your application is now running locally! You should see:
- ✅ Docker container running
- ✅ Application logs streaming
- ✅ Health endpoint returning `{"status":"UP"}`
- ✅ Swagger UI accessible

---

## Common Development Tasks

### Stop the Application

```bash
# In the terminal where docker-compose is running:
# Press Ctrl+C

# Or from another terminal:
docker-compose down
```

### Restart After Code Changes

```bash
# Stop and rebuild
docker-compose down
docker-compose up --build

# Or force a complete rebuild
docker-compose down
docker-compose build --no-cache
docker-compose up
```

### View Logs

```bash
# Follow logs in real-time
docker-compose logs -f

# View last 100 lines
docker-compose logs --tail=100

# View logs for specific service
docker-compose logs -f uh-groupings-api
```

### Update Your Configuration

```bash
# 1. Edit your properties file
nano ~/.$(whoami)-conf/uh-groupings-api-overrides.properties

# 2. Regenerate the .env file
./docker/dev-overrides-properties.sh

# 3. Restart Docker Compose
docker-compose down
docker-compose up
```

### Run Tests

```bash
# Run tests with Maven
./mvnw test

# Run specific test
./mvnw test -Dtest=YourTestClass

# Run tests with coverage
./mvnw clean test jacoco:report
```

### Access the Container Shell

```bash
# Get a shell inside the running container
docker-compose exec uh-groupings-api bash

# Check Java version
docker-compose exec uh-groupings-api java -version

# View application logs inside container
docker-compose exec uh-groupings-api ls -la /var/log/
```

### Clean Up Docker Resources

```bash
# Remove stopped containers
docker-compose down

# Remove containers and volumes
docker-compose down -v

# Remove all Docker resources (careful!)
docker system prune -a
```

---

## Troubleshooting

### Issue: "Docker daemon not running"

**Solution:**
```bash
# macOS: Start Docker Desktop
open -a Docker

# Wait 30 seconds, then retry
docker ps
```

### Issue: "Port 8080 already in use"

**Error:**
```
Error starting userland proxy: listen tcp 0.0.0.0:8080: bind: address already in use
```

**Solution:**
```bash
# Find what's using port 8080
lsof -i :8080

# Kill the process (replace PID with actual process ID)
kill -9 <PID>

# Or change the port in docker-compose.yml:
# ports:
#   - "8081:8080"  # Use 8081 on your machine
```

### Issue: "Properties file not found"

**Error:**
```
ERROR: source properties file not found: ~/.yourname-conf/uh-groupings-api-overrides.properties
```

**Solution:**
```bash
# Check if file exists
ls -la ~/.$(whoami)-conf/

# If not, create it
mkdir -p ~/.$(whoami)-conf
nano ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
# Add your configuration (see Step 2)
```

### Issue: "docker/.env not found"

**Solution:**
```bash
# Generate the .env file
./docker/dev-overrides-properties.sh

# Verify it was created
ls -la docker/.env
```

### Issue: "Application won't start"

**Check the logs:**
```bash
docker-compose logs -f uh-groupings-api
```

**Common causes:**
- Invalid Grouper credentials → Check your properties file
- Database connection failed → Verify database is running
- Port conflict → Change port in docker-compose.yml
- Missing configuration → Review properties file

### Issue: "Permission denied running script"

**Error:**
```
-bash: ./docker/dev-overrides-properties.sh: Permission denied
```

**Solution:**
```bash
chmod +x docker/dev-overrides-properties.sh
```

### Issue: "Health check always failing"

**Check:**
```bash
# Test manually
curl http://localhost:8080/actuator/health

# Check if actuator is enabled
curl http://localhost:8080/actuator

# View application logs
docker-compose logs -f uh-groupings-api | grep -i actuator
```

---

## Development Workflow

### Typical Day-to-Day Development

```bash
# Morning: Start the application
docker-compose up

# Code, code, code... 💻

# Test your changes
curl http://localhost:8080/api/v2.1/your-endpoint

# View logs
docker-compose logs -f

# Restart after significant changes
docker-compose down && docker-compose up --build

# Evening: Stop the application
docker-compose down
```

### Working with Multiple Branches

```bash
# Switch branches
git checkout feature/new-feature

# Rebuild if dependencies changed
docker-compose down
docker-compose up --build

# Switch back to main
git checkout main
docker-compose down
docker-compose up
```

### Testing API Endpoints

```bash
# Using curl
curl http://localhost:8080/api/v2.1/groupings

# Using curl with authentication
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v2.1/groupings

# POST request
curl -X POST http://localhost:8080/api/v2.1/endpoint \
  -H "Content-Type: application/json" \
  -d '{"key":"value"}'
```

---

## Configuration Reference

### Environment Variables in docker-compose.yml

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=localhost  # Spring profile
  - JAVA_OPTS=-Xmx1g -Xms512m        # JVM memory
```

### Spring Profiles

The application uses the `localhost` profile for local development:
- Located in: `src/main/resources/application-localhost.properties`
- Customize as needed for local development

### Properties File Location

Default: `~/.$(whoami)-conf/uh-groupings-api-overrides.properties`

Custom location:
```bash
./docker/dev-overrides-properties.sh /path/to/custom.properties docker/.env
```

---

## Next Steps

### You're Ready to Develop! 🚀

Now that your local environment is running:

1. **Explore the codebase:**
   - `src/main/java/` - Application code
   - `src/test/java/` - Tests
   - `src/main/resources/` - Configuration

2. **Make changes and test:**
   - Edit code in your IDE
   - Rebuild: `docker-compose up --build`
   - Test endpoints with curl or Swagger UI

3. **Read more documentation:**
   - [SECRETS.md](./SECRETS.md) - Secrets management
   - [ARCHITECTURE.md](./ARCHITECTURE.md) - System architecture
   - [docker/README.md](../docker/README.md) - Docker details

4. **Deploy to AWS when ready:**
   - [AWS_QUICKSTART.md](./AWS_QUICKSTART.md) - AWS deployment

---

## Additional Resources

### Documentation
- **[SECRETS.md](./SECRETS.md)** - Local vs AWS secrets
- **[docker/README.md](../docker/README.md)** - Detailed Docker guide
- **[ARCHITECTURE.md](./ARCHITECTURE.md)** - System design
- **[docs/README.md](./README.md)** - Documentation index

### External Resources
- [Docker Documentation](https://docs.docker.com/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

### Support
- Internal: #groupings-dev Slack channel
- Team wiki: [Link to internal wiki]

---

## Summary

You now have:
- ✅ Local development environment running
- ✅ Properties file for configuration
- ✅ Docker Compose for easy startup
- ✅ Health checks working
- ✅ API documentation available

**Happy coding!** 💻

---

**Created:** 2026-06-09  
**Last Updated:** 2026-06-09  
**Maintained By:** UH ITS DevOps Team

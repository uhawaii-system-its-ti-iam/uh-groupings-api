# Local Development Guide

**Need to get started quickly?** See [DEV_QUICKSTART.md](./DEV_QUICKSTART.md) instead.

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

# 2. Restart Docker Compose
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

### Issue: "Port 8081 already in use"

**Error:**
```
Error starting userland proxy: listen tcp 0.0.0.0:8081: bind: address already in use
```

**Solution:**
```bash
# Find what's using port 8081
lsof -i :8081

# Kill the process (replace PID with actual process ID)
kill -9 <PID>

# Or change the port in docker-compose.yml:
# ports:
#   - "8082:8081"  # Use 8082 on your machine
```

### Issue: "Properties file not found"

**Error:**
```
ERROR: source properties file not found: ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
```

**Solution:**
```bash
# Check if file exists
ls -la ~/.$(whoami)-conf/

# If not, create it
mkdir -p ~/.$(whoami)-conf
nano ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
# Add your configuration (see DEV_QUICKSTART.md)
```

### Issue: "Mounted overrides file not found"

**Solution:**
```bash
mkdir -p ~/.$(whoami)-conf
touch ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
chmod 600 ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
```

### Issue: "Properties not loading"

If your properties are not being picked up by the application:

```bash
# Stop and remove containers
docker-compose down

# Verify your file exists with correct permissions
ls -la ~/.$(whoami)-conf/uh-groupings-api-overrides.properties

# Check file permissions (should be 600)
chmod 600 ~/.$(whoami)-conf/uh-groupings-api-overrides.properties

# Restart Docker Compose
docker-compose up

# Check container logs for SPRING_CONFIG_IMPORT and properties import errors
docker-compose logs -f uh-groupings-api | grep -i config
```

The file is mounted to `/app/config/uh-groupings-api-overrides.properties` **read-only** and loaded via `SPRING_CONFIG_IMPORT` environment variable.

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

### Issue: "Health check always failing"

**Check:**
```bash
# Test manually
curl http://localhost:8081/uhgroupingsapi/actuator/health

# Check if actuator is enabled
curl http://localhost:8081/uhgroupingsapi/actuator

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
curl http://localhost:8081/uhgroupingsapi/api/v2.1/your-endpoint

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
curl http://localhost:8081/uhgroupingsapi/api/v2.1/groupings

# Using curl with authentication
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8081/uhgroupingsapi/api/v2.1/groupings

# POST request
curl -X POST http://localhost:8081/uhgroupingsapi/api/v2.1/endpoint \
  -H "Content-Type: application/json" \
  -d '{"key":"value"}'
```

---

## Configuration Reference

### Environment Variables in docker-compose.yml

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=localhost  # Spring profile
  - JAVA_OPTS=-Xmx1g -Xms512m         # JVM memory
```

### Spring Profiles

The application uses the `localhost` profile for local development:
- Located in: `src/main/resources/application-localhost.properties`
- Customize as needed for local development

### Properties File Location

Default: `~/.$(whoami)-conf/uh-groupings-api-overrides.properties`

---

## Additional Resources

### Documentation
- **[DEV_QUICKSTART.md](./DEV_QUICKSTART.md)** - Quick start guide (10 minutes)
- **[SECRETS.md](./SECRETS.md)** - Local vs AWS secrets management
- **[ARCHITECTURE.md](./ARCHITECTURE.md)** - System design and architecture
- **[AWS_QUICKSTART.md](./AWS_QUICKSTART.md)** - AWS deployment guide
- **[docs/README.md](./README.md)** - Documentation index

### External Resources
- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Maven Documentation](https://maven.apache.org/)

### Support
- Internal: #groupings-dev Slack channel
- Team wiki: [Link to internal wiki]

---

## Code Exploration

Now that your local environment is running:

1. **Explore the codebase:**
   - `src/main/java/` - Application code
   - `src/test/java/` - Tests
   - `src/main/resources/` - Configuration
   - `docs/ARCHITECTURE.md` - See AGENTS.md for detailed architecture

2. **Make changes and test:**
   - Edit code in your IDE
   - Rebuild: `docker-compose up --build`
   - Test endpoints with curl or Swagger UI

3. **Read detailed docs:**
   - [ARCHITECTURE.md](./ARCHITECTURE.md) - System design
   - [SECRETS.md](./SECRETS.md) - Secrets management
   - [AGENTS.md](../AGENTS.md) - Development agent guidelines

4. **Deploy to AWS when ready:**
   - [AWS_QUICKSTART.md](./AWS_QUICKSTART.md) - AWS deployment
   - [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md) - Ongoing AWS operations

---

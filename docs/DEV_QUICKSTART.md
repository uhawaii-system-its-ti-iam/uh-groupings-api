# Local Development Quick Start

Get the application running in **~10 minutes**.

**Looking for more details?** See [DEV_README.md](./DEV_README.md) for common tasks, troubleshooting, and workflow.

---

## Prerequisites

- **Docker Desktop** ([Download](https://www.docker.com/products/docker-desktop))

Verify: `docker --version && git --version`

---

## 1. Clone Repository

```bash
git clone https://github.com/uhawaii-system-its-ti-iam/uh-groupings-api.git
cd uh-groupings-api
```

---

## 2. Create Your Project Overrides File

This is where it will be located (outside the project by design).
```bash
mkdir -p ~/.$(whoami)-conf
```

**Create your project overrides file, then update with your values:**

The project file `uh-groupings-api-overrides.skeleton.properties` should be copied to `~/.$(whoami)-conf` and secured.

```bash
chmod 600 ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
```
---

## 3. Start the Application

```bash
docker-compose up
```

Expected output:
```
uh-groupings-api | Started SpringBootWebApplication in 45.123 seconds
uh-groupings-api | Tomcat started on port(s): 8081 (http)
```

---

## 4. Test It

**In another terminal:**
```bash
curl http://localhost:8081/uhgroupingsapi/actuator/health
# Expected: {"status":"UP"}
```

**Or in browser:**
- Health: http://localhost:8081/uhgroupingsapi/actuator/health
- Swagger UI: http://localhost:8081/uhgroupingsapi/swagger-ui.html

---

## Success!

Your application is now running locally! You should see:
- Docker container running
- Application logs streaming
- Health endpoint returning `{"status":"UP"}`
- Swagger UI accessible

---

## Basic Troubleshooting

**Docker not running?**
```bash
open -a Docker  # macOS
# Wait 30 seconds
```

**Port 8081 already in use?**
```bash
# Find what's using it
lsof -i :8081
# Kill it or change port in docker-compose.yml
```

**Properties file not found?**
```bash
# Verify file exists and create if needed
ls -al ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
```

**For more troubleshooting help,** see [DEV_README.md](./DEV_README.md#troubleshooting)

---

## Next Steps

**You're running!** Now you can:
- **Edit code and restart:** `docker-compose down && docker-compose up --build`
- **View logs:** `docker-compose logs -f`
- **Run tests:** `./mvnw test`
- **Learn more:** [DEV_README.md](./DEV_README.md) - common tasks, advanced troubleshooting, development workflow
- **Deploy to AWS:** [AWS_QUICKSTART.md](./AWS_QUICKSTART.md)

---


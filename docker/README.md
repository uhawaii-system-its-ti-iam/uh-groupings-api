# Docker Configuration - Local Development

This directory contains Docker configuration files for **local development only**. Production deployments use AWS Secrets Manager instead.

## Files

- **`dev-overrides-properties.sh`** - Script to convert Spring properties to Docker environment variables
- **`.env`** - Generated environment file (not committed to Git)
- **`.env.example`** - Example format for the generated .env file

## Local Development Setup

### Step 1: Create the overrides properties file in your home directory:

The overrides properties file is placed outside of the project root so that it never gets committed to the repository. This is a security measure.

```bash
# Create the directory
mkdir -p ~/.$(whoami)-conf

# Create the overrides properties file
nano ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
```

### Step 2: Add your Spring Boot properties to the overrides file:

```properties
groupings.api.localhost.user=your_username
groupings.api.test.admin_user=your_usernme

# Grouper client settings
grouperClient.webService.url=https://grouper-test.its.hawaii.edu/grouper-ws/servicesRest/
grouperClient.webService.login = _groupings_api_2
grouperClient.webService.password = redacted

email.is.enabled=false
email.send.recipient=your_ysername@hawaii.edu

jwt.secret.key=redacted

# Flag indicates a successful loading of the personal overrides file.
properties.override.result=OVERRIDDEN
```

### Step 3: Generate Docker Environment File

Run the script to convert your overrides properties to Docker environment variables:

```bash
# From the project root directory
./docker/dev-overrides-properties.sh

# This reads:  ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
# And creates: docker/.env
```

**Output:**
```
Wrote docker/.env
```

### Step 4: Verify the Generated File

```bash
cat docker/.env
```

### Step 5: Start Docker Compose

```bash
docker-compose up
```

Docker Compose will automatically load the environment variables from `docker/.env`.

## Script Usage

### Default Usage

```bash
# Uses default paths
./docker/dev-overrides-properties.sh
```

**Reads from:** `~/.$(whoami)-conf/uh-groupings-api-overrides.properties`  
**Writes to:** `docker/.env`

### Custom Paths

```bash
# Specify custom source and output files
./docker/dev-overrides-properties.sh /path/to/source.properties /path/to/output.env
```

### Example with Custom Paths

```bash
# Use a different source file
./docker/dev-overrides-properties.sh ~/my-config/app.properties docker/.env

# Use a completely different output location
./docker/dev-overrides-properties.sh ~/.myuser-conf/uh-groupings-api-overrides.properties /tmp/test.env
```

## How It Works

The script performs these conversions:

1. **Reads** Spring Boot properties file (Java properties format)
2. **Skips** blank lines and comments
3. **Converts** property names to environment variable names:
   - `grouper.api.url` → `GROUPER_API_URL`
   - `spring.datasource.password` → `SPRING_DATASOURCE_PASSWORD`
4. **Writes** to `docker/.env` in environment variable format
5. **Sets** file permissions to `600` (owner read/write only)

### Conversion Rules

- Lowercase → UPPERCASE
- Dots (`.`) → Underscores (`_`)
- Hyphens (`-`) → Underscores (`_`)
- All non-alphanumeric characters → Underscores (`_`)

**Examples:**
```
grouper.api.url           → GROUPER_API_URL
spring.datasource.url     → SPRING_DATASOURCE_URL
jwt.secret.key            → JWT_SECRET_KEY
my-custom-property        → MY_CUSTOM_PROPERTY
```

## Troubleshooting

### Error: "source properties file not found"

```bash
ERROR: source properties file not found: ~/.yourname-conf/uh-groupings-api-overrides.properties
```

**Solution:** Create the properties file first:
```bash
mkdir -p ~/.$(whoami)-conf
touch ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
nano ~/.$(whoami)-conf/uh-groupings-api-overrides.properties
```

### Docker Compose Can't Find .env File

```bash
ERROR: Couldn't find env file: docker/.env
```

**Solution:** Run the conversion script:
```bash
./docker/dev-overrides-properties.sh
```

### Environment Variables Not Loading

**Check:**
1. `docker/.env` exists and is readable
2. File contains your variables: `cat docker/.env`
3. Restart Docker Compose: `docker-compose down && docker-compose up`

### Permission Denied

```bash
-bash: ./docker/dev-overrides-properties.sh: Permission denied
```

**Solution:**
```bash
chmod +x docker/dev-overrides-properties.sh
```

## Security Best Practices

### ✅ DO

- ✅ Keep properties file in your home directory (`~/.yourname-conf/`)
- ✅ Set restrictive permissions: `chmod 600 ~/.yourname-conf/uh-groupings-api-overrides.properties`
- ✅ Use different credentials for local development vs production
- ✅ Regenerate `docker/.env` when properties change
- ✅ Use `.env.example` as a template for required variables

### ❌ DON'T

- ❌ Never commit `docker/.env` to Git (it's in `.gitignore`)
- ❌ Never commit your `~/.yourname-conf/` properties file
- ❌ Never use production credentials locally
- ❌ Never share your properties file with others
- ❌ Never store credentials in the project directory

## Production vs Local Development

| Aspect               | Local Development             | AWS Production         |
|----------------------|-------------------------------|------------------------|
| **Secrets Storage**  | Properties file in `~/`       | AWS Secrets Manager    |
| **Injection Method** | Docker .env file              | ECS Task Definition    |
| **Configuration**    | `dev-overrides-properties.sh` | CloudFormation/Console |
| **Rotation**         | Manual                        | Can be automated       |
| **Access Control**   | File permissions              | IAM policies           |
| **Encryption**       | None (local only)             | Encrypted at rest      |

**For production:** See [docs/SECRETS.md](../docs/SECRETS.md)

## Workflow Summary

```
┌─────────────────────────────────────────────────────┐
│  ~/.yourname-conf/                                  │
│    uh-groupings-api-overrides.properties            │
│  (Spring Boot properties format)                    │
└───────────────┬─────────────────────────────────────┘
                │
                │ ./docker/dev-overrides-properties.sh
                ▼
┌─────────────────────────────────────────────────────┐
│  docker/.env                                        │
│  (Docker environment variables format)              │
└───────────────┬─────────────────────────────────────┘
                │
                │ docker-compose up
                ▼
┌─────────────────────────────────────────────────────┐
│  Docker Container                                   │
│  (Environment variables injected)                   │
└─────────────────────────────────────────────────────┘
```

## Related Documentation

- **[../docs/SECRETS.md](../docs/SECRETS.md)** - AWS Secrets Manager guide
- **[../docs/DEV_QUICKSTART.md](../docs/DEV_QUICKSTART.md)** - Quick start guide
- **[../docker-compose.yml](../docker-compose.yml)** - Docker Compose configuration

---

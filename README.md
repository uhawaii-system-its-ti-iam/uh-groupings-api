## OOTB (Out-of-the-box) UH Groupings API

OOTB is a local, self-contained version of the UH Groupings API. It uses mock data in memory instead of Grouper, CAS, or LDAP, so you can run and develop Groupings without UH infrastructure or live services.

Production UH Groupings lives on `main`. This `ootb` branch is only for that local environment. If you meant to run the production API, use the [main README](https://github.com/uhawaii-system-its-ti-iam/uh-groupings-api/blob/main/README.md).

https://github.com/uhawaii-system-its-ti-iam/uh-groupings-api/tree/ootb

### Requirements
You need Java 17, a checkout of both this API and [uh-groupings-ui](https://github.com/uhawaii-system-its-ti-iam/uh-groupings-ui/tree/ootb) on the `ootb` branch, and a local overrides file that sets `jwt.secret.key` (see Getting started).

### Getting started
1. Check out the `ootb` branch in **both** `uh-groupings-api` and `uh-groupings-ui`.
2. Create a JWT secret used by both apps. `JwtService` injects `jwt.secret.key` on startup, and that property is not set in the `ootb` profile. Without it, the API fails during Spring context creation and never listens on `8081`.
   - The `ootb` profile loads overrides from exactly `${user.home}/.${user.name}-conf/uh-groupings-api-overrides.properties` (`spring.config.import` in `application-ootb.properties`). The folder must be named `.{username}-conf` under your home directory (a leading-dot name is required, including on Windows).
   - Examples: `C:\Users\jdoe\.jdoe-conf\uh-groupings-api-overrides.properties` or `~/.jdoe-conf/uh-groupings-api-overrides.properties`.
   - Copy [`uh-groupings-api-overrides.skeleton.properties`](uh-groupings-api-overrides.skeleton.properties) to that folder as `uh-groupings-api-overrides.properties`.
   - Set `jwt.secret.key` to a BASE64-encoded 32-byte value (HS256). Grouper fields in the skeleton can stay blank for OOTB. Generate a key with:

     ```
     $ openssl rand -base64 32
     ```

     ```
     PS> [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
     ```
   - Copy [`uh-groupings-ui-overrides.skeleton.properties`](https://github.com/uhawaii-system-its-ti-iam/uh-groupings-ui/blob/ootb/uh-groupings-ui-overrides.skeleton.properties) to the same `.{username}-conf` folder as `uh-groupings-ui-overrides.properties` and use the **same** `jwt.secret.key`.
3. In each project's IDE run configuration, set:

```
Active Profiles: ootb
```

4. Start the API first, then the UI. From the command line, pass the OOTB profile in each project (Maven otherwise starts the production Grouper integration):

```
$ ./mvnw clean spring-boot:run -Dspring-boot.run.profiles=ootb
```

The API listens on `http://localhost:8081/uhgroupingsapi`. The UI listens on `http://localhost:8080/uhgroupings` and talks to that local API.

Run OOTB with `spring-boot:run` from the source checkout. The data harness is loaded from `src/main/resources`, so a packaged WAR is not a supported OOTB deployment.

### How it works
On the `ootb` profile, Spring injects `OotbGrouperApiService` instead of the production Grouper client. Startup loads a static JSON data harness into memory. Common membership, group, subject, and attribute calls use the same response shapes as production, but some operations are unimplemented or return empty results.

```
UI → API → OotbGrouperApiService → In-Memory Data
```

Restarting the API resets the in-memory dataset. Nothing is written to external systems.

### Limitations
- No real Grouper, CAS, or LDAP integration, so authentication and live API behavior cannot be tested here.
- Data is mock JSON, not a live directory. Changes exist only in memory until the API is restarted.
- Some production Grouper features are not implemented in OOTB and may return null or empty results.
- Not suitable for performance or security testing.

Use `main` when you need real Grouper, CAS, or LDAP.

# 🤖 RoboCupMS

![Version](https://img.shields.io/badge/version-v2.0.0-blue)
![Status](https://img.shields.io/badge/status-in%20development-yellow)
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green?logo=spring)

A comprehensive server application for managing robotics competitions. This system provides complete tournament organization capabilities, from team registration to match scheduling and score evaluation.

> **Note:** This repository contains the **Backend**. The Frontend client application can be found here: **[RoboGames Frontend](https://github.com/TMusilova/robogames)**

---

## 📋 Table of Contents

- [About The Project](#about-the-project)
- [Tech Stack](#tech-stack-)
- [Getting Started](#getting-started-)
- [Configuration](#configuration-)
- [Architecture Overview](#architecture-overview-)
- [Testing & Development](#testing--development-)
- [Deployment](#deployment-)
- [Additional Resources](#additional-resources-)

---

## About The Project

**RoboCupMS** is a robust backend system designed for organizing and managing robotics tournaments. It provides comprehensive functionalities for user management, team creation, and the complete setup of competition elements including disciplines, robots, matches, and evaluation.

### ✨ Key Features

* 🔐 **Secure Authentication:** Enterprise-grade OAuth2/OIDC authentication via Keycloak with JWT token validation
* 🏆 **Competition Management:** Complete lifecycle management for teams, disciplines, robots, and competition seasons
* 📊 **Advanced Scoring:** Flexible score evaluation system with configurable aggregation methods
* 🎯 **Match Scheduling:** Automated match scheduling with playground allocation
* 👥 **Team Management:** Team invitations, registration, and member management with configurable limits
* 🤖 **Robot Registration:** Multi-robot support per team with discipline-specific constraints
* 📱 **RESTful API:** Well-documented OpenAPI 3.0 specification for easy integration

---

## Tech Stack 🛠️

| Component | Technology |
|-----------|-----------|
| **Backend Framework** | Spring Boot 3.x |
| **Language** | Java 17+ |
| **ORM** | Hibernate (JPA) |
| **Database** | MariaDB 10.6 |
| **Authentication** | Keycloak 26.4 (OAuth2/OIDC) |
| **Build Tool** | Gradle |
| **API Documentation** | OpenAPI 3.0 |
| **Containerization** | Docker & Docker Compose |
| **Security** | SSL/TLS (HTTPS) |

---

## Getting Started 🚀

The entire application stack is containerized and can be deployed with a single command using Docker Compose.

### Prerequisites

Ensure you have the following installed:
- **Docker** (version 20.10 or higher)
- **Docker Compose** (version 2.0 or higher)

### Installation & Launch

#### 1️⃣ Clone the Repository

```bash
git clone https://github.com/0xMartin/RoboCupMS.git
cd RoboCupMS
```

#### 2️⃣ Configure Environment Variables

Create a `.env` file in the project root directory with the following configuration (you can copy from `.env.example`):

```env
# ===========================================
# DATABASE CONFIGURATION
# ===========================================
# Main Application Database
MYSQL_ROOT_PASSWORD=yourSecureRootPassword123!
DB_DATABASE=robocup
DB_USER=robocup_user
DB_PASSWORD=yourSecureDbPassword456!

# Keycloak Database
KEYCLOAK_DB_DATABASE=keycloak
KEYCLOAK_DB_USER=keycloak
KEYCLOAK_DB_PASSWORD=keycloakSecurePassword789!
KEYCLOAK_DB_ROOT_PASSWORD=keycloakRootPassword012!

# ===========================================
# APPLICATION CONFIGURATION
# ===========================================
APP_PORT=8080
APP_FRONTEND_URL=https://is.robogames.utb.cz

# ===========================================
# SSL/TLS CONFIGURATION
# ===========================================
# These passwords protect the SSL keystore
KEY_STORE_PASSWORD=yourKeystorePassword345!
KEY_PASSWORD=yourKeyPassword678!

# ===========================================
# KEYCLOAK CONFIGURATION
# ===========================================
# Admin Console Access
KEYCLOAK_PORT=8180
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PASSWORD=adminSecurePassword901!

# Realm & Client Configuration
KEYCLOAK_REALM=RoboCupRealm
KEYCLOAK_AUTH_SERVER_URL=http://keycloak:8080
KEYCLOAK_CLIENT_ID=robocup-backend
KEYCLOAK_CLIENT_SECRET=your_generated_client_secret_from_keycloak
```

> [!WARNING]
> **Security Notice:** Never commit the `.env` file to version control! Replace all example passwords with strong, unique passwords in production.

> [!NOTE]
> The `KEYCLOAK_CLIENT_SECRET` must be generated in Keycloak admin console after initial setup. See [KEYCLOAK_SETUP.md](KEYCLOAK_SETUP.md) for details.

#### 3️⃣ Build and Launch

```bash
docker compose up --build
```

This command will:
- Build the Spring Boot application
- Start MariaDB databases (application + Keycloak)
- Launch Keycloak authentication server
- Deploy the RoboCupMS application

**Service Availability:**
- 🌐 **Application API:** `https://localhost:8080`
- 🔐 **Keycloak Admin Console:** `http://localhost:8180`

> [!TIP]
> Use `docker compose up --build -d` to run services in detached mode (background).

#### 4️⃣ Configure Keycloak

> [!IMPORTANT]
> Keycloak configuration is **required** before the application can authenticate users.

Follow the comprehensive setup guide in [KEYCLOAK_SETUP.md](KEYCLOAK_SETUP.md) to:
- ✅ Create the RoboCupRealm
- ✅ Configure the `robocup-backend` client with proper settings
- ✅ Generate and configure the client secret
- ✅ Create test users and assign roles
- ✅ Set up authentication flows and token settings

---

## Configuration ⚙️

### Application Configuration (`application.properties`)

The application uses Spring Boot's externalized configuration. Key settings include:

#### Database Configuration

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mariadb://localhost:3306/robocup}
spring.datasource.username=${DB_USER:robocup_root}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

> [!NOTE]
> The `ddl-auto=update` setting automatically updates the database schema. Use `validate` in production environments.

#### SSL/HTTPS Configuration

The application uses a PKCS12 keystore for SSL/TLS encryption:

```properties
server.ssl.key-store=robocupms.p12
server.ssl.key-store-type=pkcs12
server.ssl.key-alias=springboot
```

> [!TIP]
> Generate your SSL certificate using the provided script: `./Setup/ssl_certificate_setup.sh`

#### OAuth2 Resource Server Configuration

```properties
# Token issuer (public URL - must match token claims)
spring.security.oauth2.resourceserver.jwt.issuer-uri=${APP_FRONTEND_URL}/auth/realms/${KEYCLOAK_REALM}

# JWK Set URI (internal Docker network - faster and more reliable)
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${KEYCLOAK_AUTH_SERVER_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/certs
```

> [!IMPORTANT]
> **Dual URL Configuration:** The `issuer-uri` must be the **public URL** (matches token issuer claim), while `jwk-set-uri` uses the **internal Docker network URL** for performance and reliability.

### Business Logic Configuration (`config.json`)

This file contains application-specific business rules and constraints:

```json
{
  "HEADER_FIELD_TOKEN": "Authorization",
  "TOKEN_VALIDITY_DURATION": 30,
  "USER_MIN_AGE": 6,
  "USER_MAX_AGE": 99,
  "LOW_AGE_CATEGORY_MAX_AGE": 15,
  "MAX_ROBOTS_IN_DISCIPLINE": 1,
  "MAX_TEAM_MEMBERS": 4
}
```

**Configuration Parameters:**

| Parameter | Description | Default |
|-----------|-------------|---------|
| `HEADER_FIELD_TOKEN` | HTTP header name for authentication token | `Authorization` |
| `TOKEN_VALIDITY_DURATION` | Token validity in minutes | `30` |
| `USER_MIN_AGE` | Minimum participant age | `6` |
| `USER_MAX_AGE` | Maximum participant age | `99` |
| `LOW_AGE_CATEGORY_MAX_AGE` | Age threshold for junior category | `15` |
| `MAX_ROBOTS_IN_DISCIPLINE` | Maximum robots per team in a discipline | `1` |
| `MAX_TEAM_MEMBERS` | Maximum members per team | `4` |

> [!TIP]
> Adjust these values based on your competition rules without code changes.

### Docker Compose Services

The application stack consists of four services:

#### 🗄️ Application Database (`db`)
- **Image:** MariaDB 10.6
- **Purpose:** Stores competition data (users, teams, matches, scores)
- **Port:** Internal only (3306)
- **Health Check:** Ensures database is ready before starting dependent services

#### 🗄️ Keycloak Database (`keycloak-db`)
- **Image:** MariaDB 10.6
- **Purpose:** Stores Keycloak authentication data
- **Port:** Internal only (3306)
- **Isolation:** Separate database for security and performance

#### 🔐 Keycloak (`keycloak`)
- **Image:** Keycloak 26.4
- **Purpose:** OAuth2/OIDC authentication provider
- **Port:** `127.0.0.1:8180:8080` (localhost only)
- **Mode:** Development mode with health checks
- **Proxy:** Configured for reverse proxy deployment

> [!WARNING]
> Keycloak runs in **development mode** (`start-dev`). For production, switch to production mode and configure proper database settings.

#### 🚀 Application (`app`)
- **Build:** Local Dockerfile
- **Purpose:** Spring Boot application
- **Port:** `127.0.0.1:8080:8080` (localhost only, HTTPS)
- **Dependencies:** Waits for both databases and Keycloak to be healthy

> [!NOTE]
> Services bind to `127.0.0.1` for security. Use Nginx reverse proxy for external access.

---

## Architecture Overview 🏗️

```
┌─────────────────┐  Port 8180      ┌──────────────────┐      ┌──────────────────┐
│   Keycloak      │◄───────────────►│    Keycloak      │─────►│   Keycloak DB    │
│ Admin Console   │                 │     Server       │      │    (MariaDB)     │
└─────────────────┘                 └──────────────────┘      └──────────────────┘
                                            │
                                            │ JWT Token
                                            │ Validation
                                            ▼
                                    ┌──────────────────┐      ┌──────────────────┐
                                    │    RoboCupMS     │─────►│  Application DB  │
                                    │  Spring Boot App │      │    (MariaDB)     │
                                    └──────────────────┘      └──────────────────┘
                                     Port 8080 (HTTPS)
                                            ▲
                                            │
                                    ┌──────────────────┐
                                    │   Nginx Reverse  │
                                    │      Proxy       │
                                    │  (Let's Encrypt) │
                                    └──────────────────┘
                                       Port 443 (HTTPS)
```

### Authentication Flow

1. **Client** requests access to protected resource
2. **Nginx** forwards request to RoboCupMS backend
3. **RoboCupMS** validates JWT token using Keycloak's public keys
4. If token is invalid/missing, returns 401 Unauthorized
5. If token is valid, processes request and returns response

> [!NOTE]
> The application uses **stateless authentication** - no session storage required.

---

## Testing & Development 🧪

### API Testing

#### Quick Health Check

Verify the API is running with this public endpoint:

```bash
# The -k flag bypasses self-signed SSL certificate verification
curl -k https://localhost:8080/api/discipline/all
```

> [!TIP]
> For development with self-signed certificates, use `-k` flag. In production with valid SSL, remove this flag.

**Available API specifications:**
- `auth_api.yaml` - Authentication endpoints
- `competition_api.yaml` - Competition management
- `discipline_api.yaml` - Discipline configuration
- `match_api.yaml` - Match operations
- `robot_api.yaml` - Robot registration
- `team_api.yaml` - Team management
- And more...

### Database Access

#### Connect to Application Database

```bash
# Interactive MariaDB shell
docker exec -it robocupms-db-1 mariadb -u root -p
```

Enter the `MYSQL_ROOT_PASSWORD` from your `.env` file when prompted.

#### Connect to Keycloak Database

```bash
# Interactive MariaDB shell for Keycloak
docker exec -it robocupms-keycloak-db-1 mariadb -u root -p
```

Enter the `KEYCLOAK_DB_ROOT_PASSWORD` from your `.env` file.

> [!TIP]
> Use a database GUI client like DBeaver or MySQL Workbench for easier database management.

#### Useful SQL Commands

```sql
-- Show all databases
SHOW DATABASES;

-- Select database
USE robocup;

-- List all tables
SHOW TABLES;

-- View table structure
DESCRIBE team;

-- Query example
SELECT * FROM competition WHERE active = TRUE;
```

### Viewing Logs

```bash
# View all service logs
docker compose logs

# Follow logs in real-time
docker compose logs -f

# View specific service logs
docker compose logs app
docker compose logs keycloak

# View last 100 lines
docker compose logs --tail=100 app
```

### Rebuilding Services

```bash
# Rebuild and restart all services
docker compose up --build

# Rebuild specific service
docker compose up --build app

# Clean rebuild (removes volumes)
docker compose down -v
docker compose up --build
```

> [!WARNING]
> Using `-v` flag will **delete all data** in databases! Use only for fresh starts.

---

## Deployment 🚀

### Production Deployment with Nginx

For production deployment, use Nginx as a reverse proxy with Let's Encrypt SSL certificates.

#### Architecture

This project uses Nginx as a gateway and reverse proxy. It handles:
- ✅ SSL/TLS termination via Let's Encrypt
- ✅ Request routing based on URL paths
- ✅ Load balancing (if needed)
- ✅ Static file serving (frontend)

**Routing Rules:**

| Path | Target | Description |
|------|--------|-------------|
| `/` | Frontend (React) | Static React application |
| `/api/` | Backend (Spring Boot) | RESTful API endpoints |
| `/auth` | Keycloak | Authentication service |

#### Nginx Configuration

Create `/etc/nginx/sites-available/robogames.conf`:

```nginx
server {
    server_name is.robogames.utb.cz;

    listen 443 ssl; # managed by Certbot
    ssl_certificate /etc/letsencrypt/live/is.robogames.utb.cz/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/is.robogames.utb.cz/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot

    # Frontend (React Static Build)
    location / {
        root /var/www/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # Backend API (Spring Boot via HTTPS)
    location /api/ {
        proxy_pass https://127.0.0.1:8080;
        proxy_ssl_verify off;  # Self-signed cert between Nginx and app

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket support (if needed)
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # Keycloak (Identity Provider via HTTP)
    location /auth {
        proxy_pass http://127.0.0.1:8180;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

        # Critical for Keycloak behind reverse proxy
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port 443;

        # Increase buffer sizes for Keycloak
        proxy_buffer_size 128k;
        proxy_buffers 4 256k;
        proxy_busy_buffers_size 256k;
    }
}

# HTTP to HTTPS Redirect
server {
    if ($host = is.robogames.utb.cz) {
        return 301 https://$host$request_uri;
    } # managed by Certbot

    listen 80;
    server_name is.robogames.utb.cz;
    return 404; # managed by Certbot
}
```

#### Enabling the Configuration

```bash
# Create symbolic link to enable the site
sudo ln -s /etc/nginx/sites-available/robogames.conf /etc/nginx/sites-enabled/

# Test configuration
sudo nginx -t

# Reload Nginx
sudo systemctl reload nginx
```

#### SSL Certificate Setup

Use Certbot to obtain and configure Let's Encrypt certificates:

```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx

# Obtain certificate and configure Nginx automatically
sudo certbot --nginx -d is.robogames.utb.cz

# Test automatic renewal
sudo certbot renew --dry-run
```

> [!IMPORTANT]
> Ensure your domain points to your server's IP address before running Certbot.

#### Production Checklist

- [ ] Update `.env` with production values
- [ ] Change all default passwords
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` (not `update`)
- [ ] Configure Keycloak for production mode
- [ ] Set up automated database backups
- [ ] Configure monitoring and logging
- [ ] Set up firewall rules (only ports 80, 443, 22)
- [ ] Enable automatic security updates
- [ ] Review and test all Keycloak realm settings
- [ ] Set up SSL certificate auto-renewal

> [!WARNING]
> **Production Security:** Never use default credentials! Generate strong, unique passwords for all services and store them securely.

---

## Additional Resources 📚

### Documentation

- 📖 **[Keycloak Setup Guide](KEYCLOAK_SETUP.md)** - Detailed Keycloak configuration steps
- 🔐 **[OAuth2 Integration Guide](OAUTH.md)** - OAuth2 implementation details
- 📁 **[OpenAPI Specifications](OpenAPI/)** - Complete API documentation

### Database

- 🗄️ **[Database Setup Script](Setup/db_setup.sql)** - Initial database schema
- 📊 **Database Diagram** - Entity relationships (see project documentation)

### Useful Commands

```bash
# Check service status
docker compose ps

# Stop all services
docker compose down

# View resource usage
docker stats

# Clean up unused resources
docker system prune -a

# Export database backup
docker exec robocupms-db-1 mysqldump -u root -p robocup > backup.sql

# Import database backup
docker exec -i robocupms-db-1 mariadb -u root -p robocup < backup.sql
```

### Troubleshooting

**Common Issues:**

1. **Port already in use**
   ```bash
   # Find process using port
   lsof -i :8080
   # Kill process or change port in .env
   ```

2. **SSL certificate issues**
   ```bash
   # Regenerate self-signed certificate
   ./Setup/ssl_certificate_setup.sh
   ```

3. **Keycloak connection refused**
   - Check if Keycloak service is healthy: `docker compose ps`
   - Verify `KEYCLOAK_AUTH_SERVER_URL` in `.env`
   - Check Keycloak logs: `docker compose logs keycloak`

4. **Database connection failed**
   - Verify database credentials in `.env`
   - Check database health: `docker compose ps db`
   - View database logs: `docker compose logs db`

> [!TIP]
> For more help, check the [Issues](https://github.com/0xMartin/RoboCupMS/issues) page or create a new issue.

---

## Contributing 🤝

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request


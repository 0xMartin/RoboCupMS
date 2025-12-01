# 🤖 RoboCupMS

![Version](https://img.shields.io/badge/version-v2.0.0-blue)
![Status](https://img.shields.io/badge/status-in%20development-yellow)
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)

A server application for managing robotics competitions. This project is currently in development.

---


## About The Project

**RoboCupMS** is a backend system for handling the organization of robotics tournaments. It provides functionalities for user management, team creation, and the setup of competition elements like disciplines, robots, and matches.

The system supports score evaluation and scheduling of matches.

### Key Features

* 🔑 **Authentication:** Keycloak-based OAuth2/OIDC authentication with JWT tokens
* 🏆 **Competition Management:** Tools to create and manage teams, disciplines, robots, and competition seasons.
* 📊 **Scoring & Scheduling:** Functionality for score evaluation and match scheduling.

---

## Tech Stack 🛠️

* **Backend:** Java, Spring Boot, Hibernate (JPA)
* **Database:** MariaDB
* **Authentication:** Keycloak (OAuth2/OIDC)
* **Build Tool:** Gradle
* **Containerization:** Docker

---

## Getting Started 🚀

The project is containerized and can be run using Docker Compose.

### Prerequisites

* Docker and Docker Compose

### Installation & Launch

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/0xMartin/RoboCupMS.git
    cd RoboCupMS
    ```

2.  **Create an environment file:**
    Create a `.env` file in the root directory. This file holds all necessary configurations and secrets.
    ```env
    # --- Database Credentials ---
    MYSQL_ROOT_PASSWORD=yourSecretRootPassword
    DB_DATABASE=robocup
    DB_USER=robocup_user
    DB_PASSWORD=a63W9bXZYhcwAT9B

    # --- Application Config ---
    APP_PORT=8080

    # --- SSL Secrets ---
    KEY_STORE_PASSWORD=f4R03eRRG3
    KEY_PASSWORD=f4R03eRRG3

    # Keycloak Configuration
    KEYCLOAK_PORT=8180
    KEYCLOAK_ADMIN_USER=admin
    KEYCLOAK_ADMIN_PASSWORD=admin
    KEYCLOAK_REALM=RoboCupRealm
    KEYCLOAK_AUTH_SERVER_URL=http://keycloak:8080
    KEYCLOAK_CLIENT_ID=robocup-backend
    KEYCLOAK_CLIENT_SECRET=your_client_secret

    # Keycloak Database Configuration
    KEYCLOAK_DB_DATABASE=keycloak
    KEYCLOAK_DB_USER=keycloak
    KEYCLOAK_DB_PASSWORD=keycloak_db_pass_123oehrtč 
    ```

3.  **Build and run the application:**
    ```bash
    docker compose up --build
    ```
    The application will be running at `https://localhost:8080`.
    Keycloak admin console will be available at `http://localhost:8180`.

4.  **Configure Keycloak:**
    Follow the detailed setup guide in [KEYCLOAK_SETUP.md](KEYCLOAK_SETUP.md) to:
    - Create the RoboCupRealm
    - Configure the robocup-backend client
    - Create test users
    - Set up authentication flow

---


## Architecture Overview ⚙️
```
┌─────────────┐  Port 8180    ┌──────────────┐      ┌─────────────────┐
│  Keycloak   │◄─────────────►│   Keycloak   │─────►│  Keycloak DB    │
│   Admin     │               │   Server     │      │   (MariaDB)     │
└─────────────┘               └──────────────┘      └─────────────────┘
                                      │
                                      │ JWT Validation
                                      ▼
                              ┌──────────────┐      ┌─────────────────┐
                              │   RoboCupMS  │─────►│  Application DB │
                              │   Backend    │      │   (MariaDB)     │
                              └──────────────┘      └─────────────────┘
                               Port 8080 (HTTPS)
```

## Quick Test ✅

To verify the API is running, call a public endpoint using `curl`:

```bash
# The -k flag bypasses the self-signed SSL certificate check
curl -k https://localhost:8080/api/discipline/all
```

## Connect to backend database

To connect to the backend database, you can use the following command:
```bash
docker exec -it robocupms-db-1 mariadb -u root -p
```
Then enter the root password specified in the `.env` file when prompted.

## 🌐 Nginx Configuration (Reverse Proxy)

This project uses Nginx as a gateway and reverse proxy. It handles SSL termination via Let's Encrypt and routes traffic to the appropriate Docker containers based on the URL path:

* __/__ → Frontend (React Static Build)
* __/api/__ → Backend (Spring Boot)
* __/auth__ → Identity Provider (Keycloak)

###  📄 Configuration File

Below is the recommended configuration for /etc/nginx/sites-available/robogames.conf.

```nginx
server {
    server_name is.robogames.utb.cz;

    listen 443 ssl; # managed by Certbot
    ssl_certificate /etc/letsencrypt/live/is.robogames.utb.cz/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/is.robogames.utb.cz/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot

    # Frontend (React)
    location / {
        root /var/www/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # Backend API (Spring Boot)
    location /api/ {
        proxy_pass https://127.0.0.1:8080;
        proxy_ssl_verify off;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Keycloak (Identity Provider)
    location /auth {
        proxy_pass http://127.0.0.1:8180;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port 443;

        proxy_buffer_size 128k;
        proxy_buffers 4 256k;
        proxy_busy_buffers_size 256k;
    }
}

# HTTP -> HTTPS presmerovani
server {
    if ($host = is.robogames.utb.cz) {
        return 301 https://$host$request_uri;
    } # managed by Certbot

    listen 80;
    server_name is.robogames.utb.cz;
    return 404; # managed by Certbot
}
``` 

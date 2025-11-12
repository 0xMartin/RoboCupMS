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

* 🔑 **Authentication:** Standard registration and login with email/password and Google OAuth2.
* 🏆 **Competition Management:** Tools to create and manage teams, disciplines, robots, and competition seasons.
* 📊 **Scoring & Scheduling:** Functionality for score evaluation and match scheduling.

---

## Tech Stack 🛠️

* **Backend:** Java, Spring Boot, Hibernate (JPA)
* **Database:** MariaDB
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
    # Database Credentials
    MYSQL_ROOT_PASSWORD=yourSecretRootPassword
    DB_DATABASE=robocup
    DB_USER=robocup_root
    DB_PASSWORD=a63W9bXZYhcwAT9B

    # Application Port
    APP_PORT=8080

    # SSL Secrets
    KEY_STORE_PASSWORD=f4R03eRRG3
    KEY_PASSWORD=f4R03eRRG3

    # Google OAuth2 Secrets
    GOOGLE_CLIENT_ID=your_google_client_id
    GOOGLE_CLIENT_SECRET=your_google_client_secret
    ```

3.  **Build and run the application:**
    ```bash
    docker compose up --build
    ```
    The application will be running at `https://localhost:8080`.

---

## Quick Test ✅

To verify the API is running, call a public endpoint using `curl`:

```bash
# The -k flag bypasses the self-signed SSL certificate check
curl -k https://localhost:8080/api/discipline/all
```
 

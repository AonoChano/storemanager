@echo off
cd /d %~dp0

echo ============================================
echo   StoreManager One-Click Start
echo   (Docker MySQL+Redis ^-^> Spring Boot :8080)
echo ============================================

echo [1/4] Checking Docker...
docker info >nul 2>&1
if errorlevel 1 (
    echo   Docker Desktop is not running, starting it...
    start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    echo   Waiting for Docker daemon (first start may take a while)...
    :wait_docker
    timeout /t 3 /nobreak >nul
    docker info >nul 2>&1
    if errorlevel 1 goto wait_docker
)
echo   Docker OK

echo [2/4] Starting MySQL + Redis containers (first run downloads images)...
docker compose up -d
if errorlevel 1 (
    echo   Failed to start containers. Check: docker compose logs
    pause
    exit /b 1
)

echo [3/4] Waiting for MySQL ready...
:wait_mysql
docker compose exec -T mysql mysqladmin ping -h localhost -uroot -p123456 --silent >nul 2>&1
if errorlevel 1 (
    timeout /t 3 /nobreak >nul
    goto wait_mysql
)
echo   MySQL ready

echo [4/4] Starting Spring Boot app on http://localhost:8080 ...
call mvnw spring-boot:run

echo.
echo App stopped. Press any key to exit.
pause >nul

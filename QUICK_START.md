# 🚀 Quick Start Guide - CyberGuard Spring Boot Edition

## Installation Prerequisites

Choose ONE of the following options:

### Option A: Using Maven (Recommended)

**Step 1: Install Java 17+**
- Download: https://www.oracle.com/java/technologies/javase-jdk17-downloads.html
- Or use: `brew install openjdk@17` (macOS) / `choco install openjdk17` (Windows)
- Verify: `java -version`

**Step 2: Install Maven**
- Download: https://maven.apache.org/download.cgi
- Extract to a folder
- Add to PATH
- Verify: `mvn --version`

**Step 3: Build**
```bash
cd DataAccessLogAnalyzer
mvn clean package
```

**Step 4: Run**
```bash
java -jar target/app.jar
```

Access: http://localhost:8080

---

### Option B: Using Docker (Easier)

**Prerequisites:**
- Docker Desktop installed: https://www.docker.com/products/docker-desktop

**Build & Run:**
```bash
cd DataAccessLogAnalyzer

# Build Docker image
docker build -t cyberguard:latest .

# Run container
docker run -p 8080:8080 \
  -e DB_PATH=/data/data_access_logs.db \
  -v cyberguard-data:/data \
  cyberguard:latest
```

Access: http://localhost:8080

---

### Option C: Using Maven Wrapper (NO MAVEN INSTALLATION NEEDED)

**Unix/Linux/macOS:**
```bash
cd DataAccessLogAnalyzer
chmod +x mvn-wrapper.sh
./mvn-wrapper.sh clean package
java -jar target/app.jar
```

**Windows:**
```cmd
cd DataAccessLogAnalyzer
mvn-wrapper.bat clean package
java -jar target/app.jar
```

---

## Render Deployment (5 Minutes)

1. **Commit code to GitHub**
   ```bash
   git add .
   git commit -m "Spring Boot conversion"
   git push origin main
   ```

2. **Go to render.com**
   - Sign in (or create free account)
   - Click "New" → "Web Service"
   - Connect your GitHub repository
   - Click "Create Web Service"

3. **Done!** 🎉
   - Render automatically builds and deploys
   - Gets a live URL like: `https://cyberguard-xxx.onrender.com`
   - View logs in Render dashboard

---

## Testing

### Web UI
Open http://localhost:8080 and submit test data

### cURL Test
```bash
curl -X POST http://localhost:8080/DataAccessServlet \
  -d "userId=USR-123&resource=admin&accessType=DELETE&timestamp=2025-05-14T14:30:00"
```

### Expected Response
- ✅ Safe access → Green page with checkmark
- ⚠️ Suspicious access → Red page with warning

---

## Troubleshooting

| Error | Fix |
|-------|-----|
| `Command 'mvn' not found` | Install Maven or use Docker/Maven Wrapper |
| `Port 8080 already in use` | Run `PORT=8081 java -jar target/app.jar` |
| `Cannot connect to database` | Ensure `/data` directory is writable |
| `Docker build fails` | Ensure Docker daemon is running |

---

## What's Inside

✅ Spring Boot 3.2.5  
✅ Embedded Tomcat  
✅ SQLite Database  
✅ REST API Endpoint  
✅ Modern Dashboard UI  
✅ 3D Cybersecurity Design  
✅ Production-Ready Docker Setup  
✅ Render Deployment Config  

---

## File Structure

```
├── pom.xml                          (Maven configuration)
├── Dockerfile                       (Docker build instructions)
├── render.yaml                      (Render deployment config)
├── mvn-wrapper.sh/bat              (Easy build without Maven installed)
├── README.md                        (Full documentation)
├── CONVERSION_SUMMARY.md           (What was converted)
├── QUICK_START.md                  (This file)
├── src/main/
│   ├── java/com/cyberguard/
│   │   ├── DataAccessLogAnalyzerApplication.java
│   │   ├── controller/DataAccessController.java
│   │   ├── service/AccessLogService.java
│   │   ├── model/AccessLogRequest.java
│   │   └── config/AppInitializer.java
│   └── resources/
│       ├── application.properties
│       └── static/
│           ├── index.html
│           ├── style.css
│           └── script.js
└── target/
    └── app.jar                     (After building)
```

---

## Next Steps

1. **Choose installation method** (Maven, Docker, or Wrapper)
2. **Build:** `mvn clean package` (or Docker equivalent)
3. **Run:** `java -jar target/app.jar`
4. **Test:** Visit http://localhost:8080
5. **Deploy:** Push to GitHub → Connect to Render

---

## Support

- 📖 See `README.md` for comprehensive docs
- 🔍 See `CONVERSION_SUMMARY.md` for technical details
- 🐳 See `Dockerfile` for build process
- ⚙️ See `render.yaml` for deployment config

---

**Ready? Let's go! 🚀**

Choose your path above and follow the steps. You'll have CyberGuard running in minutes!

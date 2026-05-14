# 🛡️ CyberGuard – Data Access Log Analyzer

A professional enterprise-grade **Spring Boot** web application for real-time cybersecurity monitoring and suspicious data access detection. Features a modern 3D futuristic cybersecurity dashboard with SQLite backend, deployed directly on Render.

## ✨ Features

- **Real-time Access Logging**: Submit and analyze data access events instantly
- **Threat Detection Engine**: Automatically identifies suspicious access patterns (DELETE operations, admin resource access)
- **SQLite Database**: Persistent data storage with JDBC connectivity
- **3D Futuristic UI**: Animated cybersecurity dashboard with glassmorphism design
- **Responsive Design**: Fully optimized for desktop and mobile devices
- **Professional Styling**: Neon effects, particle animations, and smooth transitions
- **Alert System**: Real-time threat notifications with detailed analysis
- **Production-Ready**: Spring Boot with embedded Tomcat, optimized for cloud deployment

## 📋 Tech Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Framework** | Spring Boot | 3.2.5 |
| **Runtime** | Java | 17+ |
| **Build Tool** | Maven | 3.9.2+ |
| **Database** | SQLite | 3.45.3.0 |
| **Frontend** | HTML5, CSS3, JavaScript (Vanilla) |  |
| **Server** | Embedded Tomcat | 10.1+ |
| **Container** | Docker | Latest |
| **Deployment** | Render | Free Tier+ |

## 🚀 Quick Start (Local Development)

### Prerequisites
- Java 17 or higher
- Maven 3.9.2 or higher
- Git

### Installation

1. **Clone the repository**
```bash
git clone <your-repository-url>
cd DataAccessLogAnalyzer
```

2. **Build the project**
```bash
mvn clean package
```

3. **Run the application**
```bash
java -jar target/app.jar
```

4. **Access the application**
Open your browser and navigate to:
```
http://localhost:8080
```

## 🌐 Render Deployment

### Automatic Deployment (Recommended)

1. **Connect GitHub Repository to Render**
   - Go to [render.com](https://render.com)
   - Click "New" → "Web Service"
   - Connect your GitHub repository
   - Render will auto-detect the `render.yaml` configuration

2. **Configure Environment Variables** (if needed)
   - `PORT` - Application port (auto-set by Render to 8080)
   - `DB_PATH` - SQLite database location (default: `/data/data_access_logs.db`)
   - `JAVA_OPTS` - JVM options (default: `-Xmx512m -Xms256m`)

3. **Deploy**
   - Click "Create Web Service"
   - Render will automatically build and deploy your application

### Manual Deployment

```bash
# 1. Push code to GitHub
git push origin main

# 2. Render webhook will trigger automatically
# OR manually trigger build in Render dashboard

# 3. Application will be available at:
# https://<service-name>.onrender.com
```

## 📦 Docker Deployment

### Build Docker Image
```bash
docker build -t cyberguard:latest .
```

### Run Docker Container
```bash
docker run -p 8080:8080 \
  -e PORT=8080 \
  -e DB_PATH=/data/data_access_logs.db \
  -v cyberguard-data:/data \
  cyberguard:latest
```

### Run with Docker Compose
```bash
docker-compose up -d
```

## 🔧 Project Structure

```
DataAccessLogAnalyzer/
├── src/main/
│   ├── java/com/cyberguard/
│   │   ├── DataAccessLogAnalyzerApplication.java  (Main entry point)
│   │   ├── controller/
│   │   │   └── DataAccessController.java          (Spring MVC controller)
│   │   ├── service/
│   │   │   └── AccessLogService.java              (Business logic & DB operations)
│   │   ├── model/
│   │   │   └── AccessLogRequest.java              (Data model)
│   │   └── config/
│   │       └── AppInitializer.java                (Startup initialization)
│   └── resources/
│       ├── application.properties                 (Spring Boot config)
│       └── static/
│           ├── index.html                         (UI Dashboard)
│           ├── style.css                          (Styling)
│           └── script.js                          (Frontend logic)
├── pom.xml                                        (Maven dependencies)
├── Dockerfile                                     (Docker build config)
├── render.yaml                                    (Render deployment config)
└── README.md                                      (This file)
```

## 📝 API Endpoints

### POST `/DataAccessServlet`
Submit a data access event for analysis.

**Request Parameters:**
```
userId: string          (e.g., "USR-00421")
resource: string        (e.g., "/api/users" or "admin")
accessType: string      (READ, WRITE, UPDATE, DELETE, EXECUTE)
timestamp: datetime     (e.g., "2025-05-14T14:30:00")
```

**Response:**
Returns HTML page with:
- Access event analysis
- Threat detection result (safe or suspicious)
- Database save status
- Database record confirmation

**Example cURL:**
```bash
curl -X POST http://localhost:8080/DataAccessServlet \
  -d "userId=USR-00421&resource=admin&accessType=DELETE&timestamp=2025-05-14T14:30:00"
```

## 🛡️ Security Features

- **Input Sanitization**: XSS protection with HTML entity encoding
- **SQLite Security**: Parameterized queries to prevent SQL injection
- **Threat Detection Logic**:
  - DELETE operations flagged as suspicious
  - Admin resource access flagged as suspicious
  - Automatic database logging with timestamp
- **HTTPS**: Render provides automatic SSL/TLS certificates

## 📊 Database Schema

### `access_logs` Table
```sql
CREATE TABLE access_logs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  userId TEXT NOT NULL,
  resource TEXT NOT NULL,
  accessType TEXT NOT NULL,
  timestamp TEXT NOT NULL,
  isSuspicious INTEGER DEFAULT 0,
  createdAt DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

## 🔍 Troubleshooting

### Issue: "Port already in use"
**Solution:** Change port via environment variable
```bash
PORT=8081 java -jar target/app.jar
```

### Issue: "Database not found"
**Solution:** Ensure `/data` directory exists and is writable
```bash
mkdir -p /data
chmod 755 /data
```

### Issue: "Main method not found"
**Solution:** This has been fixed! The application now includes:
```java
public static void main(String[] args) {
    SpringApplication.run(DataAccessLogAnalyzerApplication.class, args);
}
```

### Issue: "SQLite JDBC driver not found"
**Solution:** Maven dependency is included in `pom.xml`:
```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.45.3.0</version>
</dependency>
```

## 🔄 Building and Packaging

### Generate WAR/JAR
```bash
# Build JAR (recommended for Spring Boot)
mvn clean package

# Output: target/app.jar
```

### Skip Tests (faster build)
```bash
mvn clean package -DskipTests
```

### Rebuild for Production
```bash
mvn clean package -P production
```

## 📈 Performance Optimization

- **Embedded Tomcat**: No external server needed
- **Lazy Loading**: Spring components loaded on-demand
- **Static Resource Caching**: CSS/JS cached by browser
- **JVM Tuning**: Memory settings in `render.yaml`

## 🌟 Dashboard Features

### Real-time Monitoring
- Form for submitting access events
- Instant threat detection analysis
- Color-coded status indicators (green for safe, red for suspicious)
- Live metrics showing total logs, suspicious count, safe count

### Interactive UI
- Animated particle background
- Scanline effects for cybersecurity aesthetics
- Modal alerts for suspicious activities
- Recent activity feed
- Live log stream

### Responsive Design
- Desktop: Full sidebar layout
- Tablet: Optimized grid layout
- Mobile: Single column with stacked components

## 📞 Support & Contact

For issues, feature requests, or questions:
- Open a GitHub issue
- Check the troubleshooting section
- Review Render deployment logs

## 📄 License

This project is provided as-is for educational and commercial use.

## 🎯 Future Enhancements

- [ ] PostgreSQL/MySQL support
- [ ] User authentication & roles
- [ ] Advanced analytics dashboard
- [ ] CSV/PDF report generation
- [ ] API authentication (OAuth2)
- [ ] Real-time WebSocket notifications
- [ ] Email alerts for suspicious activities

## ✅ Deployment Checklist

- [x] Main method present in `DataAccessLogAnalyzerApplication.java`
- [x] Spring Boot starter-web dependency configured
- [x] SQLite JDBC driver included
- [x] Dockerfile optimized for multi-stage build
- [x] render.yaml configured with all necessary settings
- [x] application.properties with PORT environment variable support
- [x] Static resources in `/static` folder
- [x] Maven build succeeds with `mvn clean package`
- [x] Database initialization on application startup
- [x] Health check endpoint configured
- [x] Docker image builds successfully
- [x] Ready for Render deployment!

---

**Version:** 1.0.0 (Spring Boot Edition)  
**Last Updated:** May 2025  
**Status:** Production Ready ✅

# IllySurveyAdmin

Enterprise-level survey administration system with Genesys Cloud integration.

## 🎯 Features

### Core Functionality
- **Survey Management**: View, filter, and export survey data
- **Genesys Integration**: OAuth authentication with Genesys Cloud
- **User Management**: Display user info from Genesys API
- **Session Management**: Automatic timeout and lifecycle monitoring
- **CSV Export**: Export filtered survey data

### Security
- ✅ OAuth 2.0 Authentication
- ✅ Session-based authorization
- ✅ Token revocation on logout
- ✅ Cache control (no back button after logout)
- ✅ 30-minute auto-timeout
- ✅ Complete audit logging

### Performance
- ✅ User info caching (90%+ reduction in API calls)
- ✅ Connection pool monitoring
- ✅ Slow query detection
- ✅ Database query optimization

### Logging & Monitoring
- ✅ 95+ log statements across all components
- ✅ Request tracking with IP addresses
- ✅ Performance metrics (timing)
- ✅ Error details (SQL State, Error Code)
- ✅ User activity tracking

## 🏗️ Architecture

```
├── src/main/java/com/comapp/illy/
│   ├── AuthFilter.java           # Authentication filter
│   ├── CallbackServlet.java      # OAuth callback handler
│   ├── ConfigServlet.java        # Configuration loader
│   ├── DBConnection.java         # Database connection pool
│   ├── GenesysConfig.java        # Genesys configuration
│   ├── GetUserServlet.java       # User info API
│   ├── LoginServlet.java         # Login handler
│   ├── LogoutServlet.java        # Logout with token revocation
│   ├── MainServlet.java          # Main admin servlet
│   ├── SessionListener.java      # Session lifecycle monitor
│   ├── SurveyBean.java           # Survey data model
│   └── SurveyDAO.java            # Database access layer
├── src/main/webapp/
│   ├── index.jsp                 # Main admin page
│   ├── login.jsp                 # Login page
│   └── WEB-INF/
│       ├── web.xml               # Web configuration
│       └── lib/                  # Dependencies
└── src/main/resources/
    └── log4j2.xml                # Logging configuration
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Apache Tomcat 11+
- PostgreSQL Database
- Genesys Cloud Account

### Configuration

1. **Database Setup** (`META-INF/context.xml`):
```xml
<Resource name="jdbc/IllyDB"
          auth="Container"
          type="javax.sql.DataSource"
          driverClassName="org.postgresql.Driver"
          url="jdbc:postgresql://localhost:5432/genesysdb"
          username="your_username"
          password="your_password" />
```

2. **Genesys OAuth** (`config.properties`):
```properties
genesys.client.id=your_client_id
genesys.client.secret=your_client_secret
genesys.redirect.uri=http://localhost:8080/IllySurveyAdmin/oauth/callback
genesys.region=mypurecloud.ie
```

### Deployment

1. **Build Project**:
```bash
mvn clean install
```

2. **Deploy to Tomcat**:
```bash
cp target/IllySurveyAdmin.war $TOMCAT_HOME/webapps/
```

3. **Start Tomcat**:
```bash
$TOMCAT_HOME/bin/startup.sh
```

4. **Access Application**:
```
http://localhost:8080/IllySurveyAdmin/
```

## 📊 Usage

### Login
1. Click "Accedi con Genesys"
2. Enter Genesys credentials
3. Authorize application
4. Redirected to admin panel

### View Surveys
- Default: Last 30 days
- Filter by: Date range, Type, Score, Audio
- Sort by: Date (descending)

### Export Data
1. Apply desired filters
2. Click "Esporta CSV"
3. Download starts automatically

### Logout
- Click logout button (top-right)
- Session invalidated
- Token revoked from Genesys
- Redirected to login page

## 🔍 Logging

### Log Levels

**Production**:
```xml
<Logger name="com.comapp.illy" level="INFO" />
```

**Development**:
```xml
<Logger name="com.comapp.illy" level="DEBUG" />
```

### Log Examples

**Login**:
```
[INFO ] Login request received from IP: 192.168.1.100
[INFO ] User John Doe successfully authenticated via Genesys OAuth
```

**Admin Access**:
```
[INFO ] Admin page request - User: John Doe, SessionID: ABC123, IP: 192.168.1.100
[INFO ] Query executed successfully - Records: 1500, Time: 234ms
```

**Logout**:
```
[INFO ] Logout initiated - User: John Doe, SessionID: ABC123
[INFO ] Successfully revoked Genesys access token
```

## 📝 License

This project is proprietary software.

## 👥 Authors

- **Development Team** - Initial work

## 🙏 Acknowledgments

- Genesys Cloud API
- UIKit Framework
- Apache Tomcat
- PostgreSQL
- Log4j2

## 🔗 Links

- [Genesys Cloud Documentation](https://developer.genesys.cloud/)
- [Apache Tomcat](https://tomcat.apache.org/)
- [UIKit Framework](https://getuikit.com/)

---

**Version**: 2.0.0  
**Last Updated**: 2025-11-27  
**Status**: ✅ Production Ready

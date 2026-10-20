MOBILEMART - SIMPLE MOBILE SELLING PROJECT
==========================================
Features:
- Admin login page
- 30 mobile models from different brands
- Search and brand filter
- Add/remove mobiles and edit quantity
- Java calculates the final bill using prices from MySQL and saves the order
- Customer feedback page
- Plain Java HttpServer + JDBC; no Spring Boot, no Maven, no XML

REQUIREMENTS
1. JDK 17 or newer
2. MySQL Server / MySQL Workbench
3. MySQL Connector/J JAR

SETUP
1. Run database.sql in MySQL Workbench.
2. Put the Connector/J JAR at lib/mysql-connector-j.jar.
   Download from https://dev.mysql.com/downloads/connector/j/
3. Open src/MobileMartServer.java and check DB_USER and DB_PASSWORD.
   Defaults: root / 123456789. Change to your actual MySQL password if needed.
4. Open Command Prompt in this MobileMart folder and compile:
   javac -cp "lib/mysql-connector-j.jar" -d out src/MobileMartServer.java
5. Run on Windows:
   java -cp "out;lib/mysql-connector-j.jar" MobileMartServer
6. Open http://localhost:8080

DEMO LOGIN: admin / admin123
Mobile prices are example project prices, not live market prices.
The project source is provided; test it with your local JDK/MySQL setup.

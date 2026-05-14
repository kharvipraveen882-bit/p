# Stage 1: Build the Java code
FROM openjdk:11-jdk as builder

WORKDIR /app

# Copy the source code and libraries
COPY src ./src
COPY WebContent/WEB-INF/lib ./lib

# Create output directory for classes
RUN mkdir -p out/classes

# Compile the servlet
# Assuming servlet-api.jar and sqlite-jdbc.jar are in the lib folder
RUN javac -cp "lib/servlet-api.jar:lib/sqlite-jdbc-3.53.0.0.jar" -d out/classes src/com/cyberguard/servlet/DataAccessServlet.java

# Stage 2: Setup Tomcat
FROM tomcat:9-jre11

# Remove default Tomcat webapps to clean up
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WebContent as the ROOT application
COPY WebContent /usr/local/tomcat/webapps/ROOT/

# Copy the compiled classes into the WEB-INF/classes directory
COPY --from=builder /app/out/classes /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/

# Expose Tomcat port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]

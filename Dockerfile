FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

#### **B) Create system.properties for Heroku**

1. **Right-click** on project root
2. **New → File**
3. Name: `system.properties`
4. Paste:
```
java.runtime.version=17
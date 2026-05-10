FROM maven:3.9-eclipse-temurin-21-alpine
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn package -DskipTests -q -Xmx512m
EXPOSE 8080
CMD ["java", "-Xmx256m", "-jar", "target/*.jar"]

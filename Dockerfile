FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

COPY pom.xml ./
COPY src ./src

RUN mvn -DskipTests clean package \
 && cp $(find target -maxdepth 1 -name "*.jar" ! -name "original-*.jar" | head -n 1) app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /src/app.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]

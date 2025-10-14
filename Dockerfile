# build stage
FROM maven:3.9-eclipse-temurin-21 as build

# zajistíme locale/UTF-8 pro Linux uvnitř kontejneru
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV MAVEN_OPTS="-Dfile.encoding=UTF-8"

WORKDIR /app
COPY pom.xml .
# stáhneme dependency offline (urychlí budoucí buildy)
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src

# při buildu explicitně předáme kódování
RUN mvn -Dproject.build.sourceEncoding=UTF-8 -DskipTests -q package

# runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

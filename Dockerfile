# Dockerfile
FROM openjdk:21
WORKDIR /app

# Copiamos el wrapper y el pom para cachear dependencias
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copiamos el código y construimos
COPY src src
RUN ./mvnw package -DskipTests -B

EXPOSE 8080

# Aquí el cambio: apuntamos al JAR generado por tu proyecto
ENTRYPOINT ["java", "-jar", "target/parcial-final-n-capas-0.0.1-SNAPSHOT.jar"]

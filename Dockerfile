# Dockerfile
FROM openjdk:21
WORKDIR /app
# Copiamos sólo el wrapper y el pom para cachear dependencias
COPY mvnw pom.xml ./
COPY .mvn .mvn
# Le damos permiso de ejecución al wrapper
RUN chmod +x mvnw
# Bajamos dependencias sin compilar
RUN ./mvnw dependency:go-offline -B
# Copiamos el código y construimos
COPY src src
RUN ./mvnw package -DskipTests -B
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/canchitas-0.0.1-SNAPSHOT.jar"]

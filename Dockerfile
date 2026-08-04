FROM maven:3.9.9-eclipse-temurin-23-alpine AS build
WORKDIR /app

# Copy IG microservice JARs into build stage lib/ directory and install them to local container repository
COPY lib ./lib
RUN mvn install:install-file -Dfile=lib/vehicledetails_IG-1.0.0.jar -DgroupId=com.guidewire.ig -DartifactId=vehicledetails_IG -Dversion=1.0.0 -Dpackaging=jar
RUN mvn install:install-file -Dfile=lib/addressstandardization_IG-1.0.0.jar -DgroupId=com.guidewire.ig -DartifactId=addressstandardization_IG -Dversion=1.0.0 -Dpackaging=jar
RUN mvn install:install-file -Dfile=lib/creditfraud_IG-1.0.0.jar -DgroupId=com.guidewire.ig -DartifactId=creditfraud_IG -Dversion=1.0.0 -Dpackaging=jar
RUN mvn install:install-file -Dfile=lib/telematics_IG-1.0.0.jar -DgroupId=com.guidewire.ig -DartifactId=telematics_IG -Dversion=1.0.0 -Dpackaging=jar

RUN apk add --no-cache nodejs npm

COPY pom.xml .
COPY tsconfig.json ./
COPY package*.json ./
RUN npm install
COPY src ./src
RUN mvn clean compile -DskipTests

EXPOSE 8085 8082
ENTRYPOINT ["mvn", "exec:java", "-Dexec.mainClass=com.guidewire.pc.App"]

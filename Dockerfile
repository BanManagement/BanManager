FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy Gradle wrapper and configuration first for better caching.
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties ./

RUN chmod +x gradlew

# Copy project sources.
COPY . .

# Generate aggregated Javadocs.
RUN ./gradlew --no-daemon clean aggregateJavadocs

# Normalize output path to /out.
RUN set -eux; \
    mkdir -p /out; \
    if [ -d build/docs/javadoc ]; then \
      cp -a build/docs/javadoc/. /out/; \
    elif [ -d build/docs/aggregateJavadoc ]; then \
      cp -a build/docs/aggregateJavadoc/. /out/; \
    else \
      echo "Javadocs output directory not found" >&2; \
      exit 1; \
    fi

FROM nginx:alpine

COPY --from=build /out/ /usr/share/nginx/html/
EXPOSE 80

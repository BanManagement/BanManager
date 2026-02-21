FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy Gradle wrapper and configuration first for better caching.
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties ./

RUN chmod +x gradlew

# Copy project sources.
COPY . .

# Generate API Javadocs using a minimal settings file
# to avoid loading Fabric/Stonecutter projects.
RUN cp settings-javadocs.gradle.kts settings.gradle.kts && \
    ./gradlew --no-daemon :BanManagerCommon:javadoc

# Normalize output path to /out.
RUN set -eux; \
    mkdir -p /out; \
    if [ -d common/build/docs/javadoc ]; then \
      cp -a common/build/docs/javadoc/. /out/; \
    elif [ -d build/docs/javadoc ]; then \
      cp -a build/docs/javadoc/. /out/; \
    elif [ -d build/docs/aggregateJavadoc ]; then \
      cp -a build/docs/aggregateJavadoc/. /out/; \
    elif [ -d build/docs/aggregateJavadocs ]; then \
      cp -a build/docs/aggregateJavadocs/. /out/; \
    else \
      echo "Javadocs output directory not found" >&2; \
      exit 1; \
    fi

FROM nginx:alpine

COPY --from=build /out/ /usr/share/nginx/html/
EXPOSE 80

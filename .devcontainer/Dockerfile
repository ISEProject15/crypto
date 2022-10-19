ARG VARIANT="17-bullseye"
FROM mcr.microsoft.com/vscode/devcontainers/java:0-${VARIANT}

RUN apt update && apt install -y \
    openjdk-17-jdk \
    && java -version
WORKDIR /src
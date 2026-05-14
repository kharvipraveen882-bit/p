#!/bin/bash
# Maven Wrapper - Use this to build without having Maven installed globally
# Run: ./mvn-wrapper.sh clean package

# Detect OS
OS=$(uname -s)
MAVEN_VERSION="3.9.2"

# Download Maven if not exists
if [ ! -d "maven-$MAVEN_VERSION" ]; then
    echo "🔧 Downloading Maven $MAVEN_VERSION..."
    if [[ "$OS" == "Darwin" ]]; then
        curl -s https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xz
        mv apache-maven-$MAVEN_VERSION maven-$MAVEN_VERSION
    else
        wget -q https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz -O - | tar xz
        mv apache-maven-$MAVEN_VERSION maven-$MAVEN_VERSION
    fi
fi

# Run Maven with passed arguments
./maven-$MAVEN_VERSION/bin/mvn "$@"

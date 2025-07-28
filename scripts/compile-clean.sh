#!/bin/bash

# Script to compile FraudLens without warnings
echo "🔧 Compiling FraudLens without warnings..."

# Set environment variables to suppress warnings
export MAVEN_OPTS="-Djava.security.manager=allow -Dmaven.compiler.showWarnings=false -Dmaven.compiler.showDeprecation=false"

# Clean and compile with quiet mode
mvn clean compile \
    --batch-mode \
    --quiet \
    -Dmaven.compiler.showWarnings=false \
    -Dmaven.compiler.showDeprecation=false \
    -Djava.security.manager=allow

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful without warnings!"
else
    echo "❌ Compilation failed!"
    exit 1
fi 
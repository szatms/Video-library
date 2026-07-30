#!/bin/bash

# Cleanup script for microservice project
# This script removes virtual environments and temporary files

echo "Starting cleanup..."

# Remove virtual environment directories
echo "Removing virtual environments..."
find . -type d -name "venv" -exec rm -rf {} + 2>/dev/null || true
find . -type d -name ".venv" -exec rm -rf {} + 2>/dev/null || true
find . -type d -name "env" -exec rm -rf {} + 2>/dev/null || true

# Remove __pycache__ directories
echo "Removing Python cache files..."
find . -type d -name "__pycache__" -exec rm -rf {} + 2>/dev/null || true

# Remove .pyc files
echo "Removing Python compiled files..."
find . -name "*.pyc" -delete 2>/dev/null || true

# Remove log files
echo "Removing log files..."
rm -f *.log 2>/dev/null || true

# Remove temporary files
echo "Removing temporary files..."
rm -f .DS_Store 2>/dev/null || true

# Remove any .git directories (if they exist)
echo "Removing .git directories..."
find . -type d -name ".git" -exec rm -rf {} + 2>/dev/null || true

echo "Cleanup completed!"

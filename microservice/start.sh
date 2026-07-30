#!/bin/bash

# YouTube Data Harvester - Start Script

echo "Starting YouTube Data Harvester service..."

# Change to the script directory
cd "$(dirname "$0")"

# Check if we're already in a virtual environment
if [ -z "$VIRTUAL_ENV" ]; then
    # Use existing virtual environment if it exists
    VENV_DIR="./venv"
    if [ -d "$VENV_DIR" ]; then
        echo "Using existing virtual environment..."
    else
        echo "Creating virtual environment..."
        python3 -m venv "$VENV_DIR"
        if [ $? -ne 0 ]; then
            echo "Error: Failed to create virtual environment"
            exit 1
        fi
    fi

    # Activate virtual environment
    source "$VENV_DIR/bin/activate"
    if [ $? -ne 0 ]; then
        echo "Error: Failed to activate virtual environment"
        exit 1
    fi
fi

# Install dependencies if not already installed
echo "Installing dependencies..."
pip install -r requirements.txt
if [ $? -ne 0 ]; then
    echo "Error: Failed to install dependencies"
    exit 1
fi

# Start the FastAPI application
echo "Starting service on port 8000 (localhost only)..."
python main.py
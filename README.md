# Stocks Processor

A Spring Batch application that processes stock data from Compressed CSV files. The application listens for notifications from Google Pub/Sub, downloads compressed CSV files from Google Cloud Storage, processes the data, and stores it in a PostgreSQL database. Once the processing is completed, the files are removed.

## Features

- **Google Cloud Integration**: Uses Pub/Sub to listen for notifications and Cloud Storage to download CSV files.
- **Spring Batch**: Efficient batch processing of stock data.
- **PostgreSQL**: Stores processed stock data in a relational database.
- **Docker**: The project is containerized using Docker for consistent development and deployment.

## Requirements

- Java 21+
- Spring Boot
- PostgreSQL
- Docker
- Google Cloud SDK
- Gradle (build tool)

## Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/devcalm/stocks-processor.git
   cd stocks-processor
   ```
2. **Setup environment variables**:  Create a <code>.env</code> file based on <code>.env.example</code>:
   ```bash
   cp .env.example .env
   ```

   Modify the following parameters in <code>.env</code>:

- <code>POSTGRES_HOST</code>, <code>POSTGRES_PORT</code>, <code>POSTGRES_USER</code>, <code>POSTGRES_PASSWORD</code>, <code>POSTGRES_DB</code> – PostgreSQL database details.
- <code>GCP_CREDENTIAL_FILE_PATH</code> – Path to Google Cloud credentials.
- <code>GCP_PROJECT_ID</code>, <code>GCP_SUBSCRIPTION_NAME</code>, <code>GCP_BUCKET</code> – Google Cloud Pub/Sub and Cloud Storage details.

3. **Build the application**:
   ```bash
   ./gradlew build
   ```
4. **Run the application using Docker**: Make sure Docker is running and execute the following:
   ```bash
   docker-compose up
   ```

**Project Structure**   

- <b>src/:</b> Contains the Java source code, including batch configuration and job processing logic.
- <b>k8s/:</b> Kubernetes configurations for deploying the application.
- <b>storage/:</b> Stores downloaded compressed CSV files temporarily before processing.

**Tech Stack**   

- <b>Spring Boot</b> for application configuration.
- <b>Spring Batch</b> for batch processing.
- <b>Google Cloud</b> Pub/Sub for notifications.
- <b>Google Cloud</b> Storage for file storage.
- <b>PostgreSQL</b> for database management.
- <b>Docker</b> for containerization.
- <b>Kubernetes</b> for deployment and scaling.

**License**   

This project is licensed under the MIT License.

---
**Kubernetes Deployment**

The <code>k8s</code> folder contains Kubernetes manifests to deploy the application in a cloud-native environment. The files define the resources required to run application on a Kubernetes cluster.

**Steps to Deploy on Kubernetes**
1. **Configure Secrets and ConfigMaps**: Set up Kubernetes secrets and ConfigMaps to store sensitive information like PostgreSQL credentials and Google Cloud credentials. These can be created using the following commands:
   ```bash
   kubectl create namespace stocks-processor
   kubectl create secret generic gcp-credentials --from-file=path-to-credentials.json
   kubectl create configmap app-config --from-env-file=.env
   ```
2. **Deploy PostgreSQL**: Deploy the PostgreSQL instance using the manifest in the <code>k8s</code> folder:
   ```bash
   kubectl apply -f k8s/postgres-deployment.yaml
   ```
3. **Deploy the Stocks Processor Application**: Once PostgreSQL is up and running, deploy Spring Batch application using:
   ```bash
   kubectl apply -f k8s/stocks-processor-deployment.yaml
   ```
4. **Monitor the Deployment**: Pods monitoring:
   ```bash
   kubectl get pods
   kubectl logs <pod-name>
   ```
5. **Scaling**: Scale the application by modifying the <code>replicas</code> field in the <code>stocks-processor-deployment.yaml</code> file or by using the following command:
   ```bash
   kubectl scale deployment stocks-processor --replicas=3
   ```

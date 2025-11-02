# Pet Clinic Deployment Playbook

This guide walks through deploying the Spring Boot Pet Clinic application to an AWS EC2 instance using Docker and an automated Jenkins pipeline. The focus is on infrastructure and automation—no application code changes are required, and the pipeline will containerize and run the existing code as-is.

> **Heads-up on Java versions**
>
> The Maven build targets Java 17 (see `pom.xml`). The assignment requests JDK 8 and JDK 11 on the EC2 host—install those for completeness, but make Java 17 the default for Jenkins and runtime tasks so the build succeeds.

---

## 1. Prerequisites checklist

- AWS account with permissions to create EC2 instances and security groups.
- GitHub (or other Git) repository containing this project.
- SSH key pair for EC2 access.
- Local workstation with Git to push the project to your remote repository.
- (Optional) Container registry account if you later decide to publish images externally.

---

## 2. Prepare the Git repository

1. Initialize a new Git repository (if you have not already) and push it to GitHub:

   ```bash
   git init
   git remote add origin https://github.com/<your-username>/petclinic.git
   git add .
   git commit -m "Initial pet clinic deployment setup"
   git push -u origin main
   ```

2. Confirm the Jenkinsfile defaults before pushing (or override them with Jenkins parameters later):
   - `APP_NAME`: Container name used during deployment (defaults to `pet-clinic`).
   - `APP_PORT`: Host/container port exposed for the app (defaults to `8081`).

---

## 3. Launch the AWS EC2 instance

1. **AMI**: Amazon Linux 2023 (64-bit x86) or Ubuntu Server 22.04 LTS (64-bit x86) both work well.
2. **Instance type**: t3.medium (2 vCPU, 4 GiB) gives Jenkins, Docker, and the app enough headroom.
3. **Storage**: 30 GiB gp3 root volume.
4. **Security group** – open the following inbound ports:
   - 22: SSH administrative access (lock down to your IP).
   - 8080: Jenkins web UI.
   - 8081: Pet Clinic application once the container is running.
   - 50000 (optional): Jenkins inbound agent port if you plan to connect agents.
5. Launch the instance with your SSH key pair and note the public DNS name/IP.

---

## 4. Bootstrap the EC2 host

SSH into the instance as `ec2-user` (Amazon Linux) or `ubuntu` (Ubuntu) and execute the following commands. Substitute `dnf` with `apt` on Ubuntu where noted.

### 4.1 System update and tooling

```bash
sudo dnf update -y
sudo dnf install -y git unzip
echo "System packages updated"
```

> **Ubuntu tip:** run `sudo apt update && sudo apt install -y git unzip` instead.

### 4.2 Install JDK 8, JDK 11, and JDK 17

```bash
sudo dnf install -y java-1.8.0-amazon-corretto java-11-amazon-corretto java-17-amazon-corretto
sudo alternatives --config java    # choose the Java 17 option
java -version
```

> **Ubuntu tip:** install OpenJDK packages via `sudo apt install -y openjdk-8-jdk openjdk-11-jdk openjdk-17-jdk`.

Set `JAVA_HOME` globally (Java 17):

```bash
sudo tee /etc/profile.d/java.sh > /dev/null <<'EOF'
export JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto
export PATH="$JAVA_HOME/bin:$PATH"
EOF
source /etc/profile.d/java.sh
```

### 4.3 Install and configure Docker

```bash
sudo dnf install -y docker
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user
newgrp docker
docker version
```

> **Ubuntu tip:** install Docker Engine by following the official convenience script or run `sudo apt install -y docker.io` for a quick start.

### 4.4 Install Jenkins

```bash
sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key
sudo dnf install -y jenkins
sudo systemctl enable --now jenkins
sudo systemctl status jenkins --no-pager

# Allow Jenkins to control Docker
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

> **Ubuntu tip:** add the Jenkins Debian repository (per the [official docs](https://www.jenkins.io/doc/book/installing/linux/)) and install via `sudo apt install jenkins`, then apply the same Docker group steps.

Retrieve the initial admin password:

```bash
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

### 4.5 Optional: Install Docker Compose v2

```bash
sudo dnf install -y docker-compose-plugin
```

---

## 5. First-time Jenkins configuration

1. Open `http://<ec2-public-dns>:8080` and complete the setup wizard using the password retrieved above.
2. Install the **suggested plugins** plus:
   - Docker Pipeline
   - Pipeline Utility Steps
3. Create the Jenkins admin user requested by the assignment.
4. Configure global tools:
   - **JDK**: Add a `JDK 17` entry pointing to `/usr/lib/jvm/java-17-amazon-corretto` and uncheck automatic installation.
5. Add credentials as needed:
   - **Git** (if the repository is private): add a personal access token or SSH key.

---

## 6. Working folder on EC2

Clone the repository so Docker and Jenkins can reference it locally:

```bash
cd /opt
sudo git clone https://github.com/<your-username>/petclinic.git
sudo chown -R jenkins:jenkins petclinic
```

The Jenkins pipeline will reuse this workspace when it checks out code.

---

## 7. Jenkins pipeline job

1. In Jenkins select **New Item → Pipeline** and name it `pet-clinic-pipeline`.
2. Under **Pipeline definition**, choose **Pipeline script from SCM**.
3. Select **Git** as SCM and provide the repository URL and branch. If the repo is private, attach the credential added earlier.
4. Leave the script path as `Jenkinsfile` (present at the repo root).
5. Save the job and click **Build Now** to run the pipeline.

### 7.1 Pipeline stages explained

| Stage              | What happens                                                                                  |
| ------------------ | --------------------------------------------------------------------------------------------- |
| Checkout           | Pulls the latest commit from your Git repository.                                             |
| Build & Test       | Executes `./mvnw clean verify` ensuring the code compiles and tests (currently none) run.     |
| Build Docker Image | Builds `pet-clinic:build-<build-number>` locally on the Jenkins/EC2 host using the Dockerfile. |
| Deploy Container   | Removes any running container named `pet-clinic` and starts a new one mapping port 8081.      |

The pipeline uses environment variables defined at the top of `Jenkinsfile`. Adjust them if you want to rename the container or expose a different port.

---

## 8. Docker image behaviour

- The multi-stage `Dockerfile` compiles the project with Maven on Java 17 and delivers a lightweight runtime image based on Eclipse Temurin 17 JRE.
- Port `8081` is exposed—the same port configured in `application.properties`.

Manual build/test commands (outside Jenkins) for reference:

```bash
./mvnw clean verify
docker build -t pet-clinic:local .
docker run -d --name pet-clinic -p 8081:8081 pet-clinic:local
```

Stop and clean:

```bash
docker rm -f pet-clinic || true
docker image prune --force
```

---

## 9. Accessing the live application

After the Jenkins pipeline finishes, browse to:

```
http://<ec2-public-dns>:8081
```

Because no external database is provisioned, the CRUD screens will render but persistence features will require a database to be fully functional. You can later point the container to a managed database by supplying the appropriate `SPRING_DATASOURCE_*` environment variables.

---

## 10. Troubleshooting tips

| Issue                                                           | Resolution                                                                                                                                                                                                                               |
| --------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Jenkins build fails with `Unsupported class file major version` | Ensure the system default Java is 17 (`java -version`). Re-run `sudo alternatives --config java`.                                                                                                                                        |
| Docker build fails due to network timeouts                      | Retry once your EC2 instance has outbound internet access. Consider increasing the root volume size.                                                                                                                                     |
| Container exits on startup complaining about MySQL              | Either provide a MySQL endpoint or temporarily comment out the datasource properties when running locally. Jenkins deployment assumes no external database; the UI will still render but persistence calls will fail without a database. |
| `docker: permission denied` inside pipeline                     | Confirm the Jenkins user belongs to the `docker` group (`sudo usermod -aG docker jenkins` then restart Jenkins).                                                                                                                         |

---

## 11. Clean-up

When you are finished testing:

```bash
# On the EC2 host
sudo systemctl stop jenkins
docker rm -f pet-clinic || true
sudo systemctl stop docker
```

Terminate the EC2 instance from the AWS console to avoid ongoing costs.

---

## 12. What was delivered in this repository

- Updated multi-stage `Dockerfile` targeting Java 17 for an optimized runtime container.
- Production-ready `Jenkinsfile` implementing checkout, build, and local deployment stages on the EC2 host.
- This deployment playbook consolidating all AWS, Docker, and Jenkins steps into a single reference.

You can now trigger the Jenkins pipeline at any time to build, containerize, and deploy the Pet Clinic application on your EC2 host without touching the application source code.

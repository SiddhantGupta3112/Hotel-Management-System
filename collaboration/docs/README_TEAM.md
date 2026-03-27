# Hotel Management System — Team Setup

## Prerequisites
- JDK 21 (Eclipse Temurin recommended)
- Maven 3.8+
- IntelliJ IDEA Community Edition
- Oracle Database XE

## First Time Setup

### 1. Clone the repo
git clone <your-repo-url>

### 2. Set environment variables in IntelliJ
- Run > Edit Configurations > your config > Environment Variables
- Add:
  DB_URL=jdbc:oracle:thin:@localhost:1521/xe
  DB_USER=your_username
  DB_PASS=your_password

### 3. Set up your database
Run the SQL scripts in order in SQL Developer:
src/main/resources/db/01_schema.sql
src/main/resources/db/02_seed.sql
src/main/resources/db/03_price_history_seed.sql

### 4. Load dependencies
    mvn clean install

### 5. Run
    mvn javafx:run

## Branch Strategy
main     → stable only, no direct commits
dev      → merge features here first
feature/ → your work branches
```

---

## 4. Verify `.gitignore` is correct

Open `.gitignore` and confirm `.env` is in there. If the file looks sparse, replace it with:
```
target/
*.class
.idea/
*.iml
.vscode/
.settings/
.classpath
.project
.DS_Store
Thumbs.db
.env
db.properties
#!/bin/bash

# ============================================================
#  Hotel Management System — Project Structure Init
#  Run once from your project root: bash init.sh
#  This creates empty files only. You write the code.
# ============================================================

PACKAGE="src/main/java/com/hotel/app"
RESOURCES="src/main/resources"

echo ""
echo "========================================"
echo "  Creating project structure...         "
echo "========================================"
echo ""

# ── Directories ──────────────────────────────────────────────

mkdir -p $PACKAGE/util
mkdir -p $PACKAGE/entity
mkdir -p $PACKAGE/repository
mkdir -p $PACKAGE/service
mkdir -p $PACKAGE/controller
mkdir -p $RESOURCES/fxml
mkdir -p $RESOURCES/css
mkdir -p $RESOURCES/db
mkdir -p collaboration/templates
mkdir -p collaboration/docs

# ── pom.xml ──────────────────────────────────────────────────

touch pom.xml

# ── Utility ──────────────────────────────────────────────────

touch $PACKAGE/Main.java
touch $PACKAGE/util/DBConnection.java
touch $PACKAGE/util/SessionManager.java

# ── Entities (plain POJOs) ───────────────────────────────────

touch $PACKAGE/entity/User.java
touch $PACKAGE/entity/Customer.java
touch $PACKAGE/entity/Employee.java
touch $PACKAGE/entity/Department.java
touch $PACKAGE/entity/Room.java
touch $PACKAGE/entity/RoomType.java
touch $PACKAGE/entity/Booking.java
touch $PACKAGE/entity/Payment.java
touch $PACKAGE/entity/Invoice.java
touch $PACKAGE/entity/Service.java
touch $PACKAGE/entity/ServiceUsage.java
touch $PACKAGE/entity/RoomMaintenance.java

# ── Repositories (all SQL lives here) ────────────────────────

touch $PACKAGE/repository/UserRepository.java
touch $PACKAGE/repository/CustomerRepository.java
touch $PACKAGE/repository/EmployeeRepository.java
touch $PACKAGE/repository/RoomRepository.java
touch $PACKAGE/repository/BookingRepository.java
touch $PACKAGE/repository/PaymentRepository.java
touch $PACKAGE/repository/InvoiceRepository.java
touch $PACKAGE/repository/ServiceRepository.java
touch $PACKAGE/repository/MaintenanceRepository.java
touch $PACKAGE/repository/PriceHistoryRepository.java

# ── Services (business logic, zero JavaFX imports) ───────────

touch $PACKAGE/service/AuthService.java
touch $PACKAGE/service/BookingService.java
touch $PACKAGE/service/BillingService.java
touch $PACKAGE/service/RoomService.java
touch $PACKAGE/service/HoltWintersService.java

# ── Controllers (JavaFX only) ────────────────────────────────

touch $PACKAGE/controller/LoginController.java
touch $PACKAGE/controller/DashboardController.java
touch $PACKAGE/controller/RoomController.java
touch $PACKAGE/controller/BookingController.java
touch $PACKAGE/controller/CustomerController.java
touch $PACKAGE/controller/BillingController.java
touch $PACKAGE/controller/PredictionController.java
touch $PACKAGE/controller/MaintenanceController.java

# ── FXML Screens ─────────────────────────────────────────────

touch $RESOURCES/fxml/Login.fxml
touch $RESOURCES/fxml/Dashboard.fxml
touch $RESOURCES/fxml/Rooms.fxml
touch $RESOURCES/fxml/Booking.fxml
touch $RESOURCES/fxml/Customers.fxml
touch $RESOURCES/fxml/Billing.fxml
touch $RESOURCES/fxml/Prediction.fxml
touch $RESOURCES/fxml/Maintenance.fxml

# ── CSS ──────────────────────────────────────────────────────

touch $RESOURCES/css/main.css

# ── SQL Scripts ──────────────────────────────────────────────

touch $RESOURCES/db/01_schema.sql
touch $RESOURCES/db/02_seed.sql
touch $RESOURCES/db/03_price_history_seed.sql

# ── Collaboration ────────────────────────────────────────────

touch collaboration/templates/.env.example
touch collaboration/docs/README_TEAM.md

# ── .gitignore ───────────────────────────────────────────────

cat <<'EOF' > .gitignore
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
EOF

echo "Structure created. Start with pom.xml then work file by file."
echo ""

# ── Print tree ───────────────────────────────────────────────

find src collaboration -type f | sort
echo ""
echo "Done."
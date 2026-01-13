# NaraTrad - Backend API 

NaraTrad is a stock portfolio tracking application designed to provide "Wise Trading for Everyone". It solves the problem of investors lacking a clear and simple way to monitor their portfolios by providing a simple and intuitive alternative to complex trading platforms.
## 📌 Project Description and Purpose
This project is part of the LIT Full Stack Development Track 2025 Final Project. The purpose is to build a backend that enables beginner investors to move away from manual tracking tools to a secure, automated, and real-time dashboard.

## ✨ Features List
* **User Authentication**: Secure registration and login using JWT (JSON Web Tokens).
* **Portfolio Management**: Comprehensive CRUD operations for stock holdings with automatic weighted average price calculations.
* **Real-time Stock Data**: Integration with the Finnhub API for live market pricing.
* **Watchlist System**: Ability to track specific stock symbols with targeted price.
* **Admin Dashboard**: Statistical overview of user growth and system performance.
* **Security**: Password encryption using BCrypt and protected API endpoints.
* **Email Services**: Password reset and notification system via Mailtrap.

## 🛠 Technology Stack
* **Framework**: Spring Boot 3.2.4
* **Language**: Java 21
* **Database**: PostgreSQL hosted on Supabase
* **Security**: Spring Security & JWT
* **Documentation**: Swagger UI 
* **Testing**: JUnit 5
* **Deployment**: Railway

## ⚙️ Installation Instructions (Local Setup)
1.  **Clone the repository**:
    ```bash
    git clone https://github.com/NaraStack/NaraTrad-BE.git
    cd naratrad
    ```
2.  **Configure Environment Variables**:
    Set up Environment Variables using the example provided in env.example file
3.  **Build the Project**:
    ```bash
    ./mvnw clean install
    ```

## 🔐 Environment Variables Needed
To run this application, you will need to set the following variables:
* `SPRING_DATASOURCE_URL`: Your Supabase PostgreSQL JDBC URL.
* `SPRING_DATASOURCE_USERNAME`: Your database username.
* `SPRING_DATASOURCE_PASSWORD`: Your database password.
* `JWT_SECRET`: Secret key for signing JWT tokens.
* `FINNHUB_API_KEY`: API key from Finnhub.io.
* `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`: SMTP credentials for Mailtrap.

## 🚀 How to Run the Application
Run the Spring Boot application using Maven:
```bash
./mvnw spring-boot:run
```

## ☁️ Deployment Instructions
This project is configured for continuous deployment on **Railway**:
1.  **Repository Connection**: Connect GitHub repository to the Railway dashboard.
2.  **Variable Setup**: Add all required variables in the **Variables** tab (DB_URL, JWT_SECRET, etc.).
3.  **Deployment**: The application will automatically build and deploy using the `main` branch.
4.  **Verification**: Once the status is **Active/Online**, the API can be accessed via the provided Railway public URL.

## 📸 Documentation
* **Unit Testing Success**: Testing passed for all backend modules.
  <img width="1600" height="850" alt="image" src="https://github.com/user-attachments/assets/dd840f6c-7505-441b-91af-ee09507ebbeb" />
* **API Documentation**: Live documentation of all NaraTrad endpoints.
  <img width="100%" alt="Swagger Documentation NaraTrad" src="https://github.com/user-attachments/assets/0483bb0d-c027-4a02-b87c-5440ddcc074c" />
* **Deployment link**: https://naratrad-be-production.up.railway.app/swagger-ui/index.html

## 👥 Team Member Contributions
* **Ammara Azwa (Backend Developer)**:
    * **Core System Architecture**: Developed the entire backend infrastructure using Spring Boot.
    * **Security Implementation**: Collaborated on the JWT authentication and role-based access control (RBAC) framework.
    * **Cloud & API Integration**: Successfully integrated Supabase for PostgreSQL hosting and Finnhub for real-time market data.
    * **Quality Assurance**: Executed 14 comprehensive unit tests achieving 100% logic reliability.
* **Lysandra Velyca (Frontend Developer)**:
    * **UI/UX Implementation**: Built the core user interface using Angular/React based on the project design.
    * **API Integration**: Responsible for connecting and integrating the Frontend components with the Backend API endpoints.
    * **Responsive Design**: Ensured a seamless user experience across both desktop and mobile devices.
* **Mathilda Dellanova (Project Manager)**:
    * **Project Management**: Managed the overall project timeline, team coordination, and final presentation preparation.
    * **Security & Auth Development**: Contributed to the implementation of the JWT security layer and authentication flow.
    * **Feature Development**: Built the User Dashboard and Watchlist features alongside the team.

# 🌾 KisanMitra – Agricultural Marketplace Platform

**Connecting Farmers, Buyers, and Agricultural Service Providers Through Technology.**

KisanMitra is a full-stack agricultural marketplace designed to bridge the gap between agricultural producers, buyers, and service providers. It provides a digital platform where users can discover agricultural products, publish listings, negotiate prices, manage orders, communicate in real time, and build trust through ratings and reviews.

The platform aims to simplify agricultural trading by making it more accessible, transparent, and efficient.

---

## ✨ Key Features

### 👤 Authentication & User Management
- Secure user registration and login.
- JWT-based authentication and authorization.
- User profile management.
- Protected routes and role-based access.

### 🛒 Agricultural Marketplace
- Browse and publish agricultural listings.
- Support for agricultural produce, fertilizers, equipment, and other farming essentials.
- Upload and display product images.
- Search, category-based browsing, and filters.

### 🤝 Offers & Order Management
- Buyers can submit offers on listed products.
- Sellers can accept, reject, or counter offers.
- Track orders and their current status.
- Manage the buying and selling process.

### 💬 Real-Time Messaging
- Direct communication between buyers and sellers.
- Real-time messaging powered by Socket.IO.
- Instant message updates for smoother negotiations.

### ⭐ Reviews & Ratings
- Review completed transactions.
- Rate sellers based on trading experiences.
- Build trust and transparency within the marketplace.

### 📊 Dashboard & Analytics
- Personalized user dashboard.
- Listing and order summaries.
- Overview of marketplace activities.

### 🌐 Multilingual Support
- English
- Hindi
- Marathi

Designed to make the platform accessible to users from different linguistic backgrounds.

### 🛡️ Admin Management
- Dedicated admin pages.
- Manage marketplace listings and users.
- Support marketplace monitoring and administration.

### 📈 Market Price Reference
- Display agricultural market price reference data.
- Help users understand indicative market prices while trading.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| Frontend | React.js, Vite |
| Styling | Tailwind CSS |
| Backend | Java 21, Spring Boot 3 |
| Database | Supabase PostgreSQL |
| Authentication | Supabase Auth, KisanMitra API JWT |
| Real-Time Communication | Socket.IO |
| Internationalization | i18next |
| Development Tools | npm, Git, GitHub |

---

## 📁 Project Structure

```text
KisanMitra/
│
├── src/                         # React frontend
├── backend/                     # Spring Boot API and Supabase config
├── server/                      # Legacy Express backend
│
├── package.json                # Root scripts and dependencies
└── README.md
```

---

## ⚙️ Installation & Setup

Follow these steps to run KisanMitra locally.

### 1. Prerequisites

Make sure you have installed:

- [Node.js](https://nodejs.org/) (v20.19 or newer)
- npm
- Java 21 and Maven
- A Supabase project with Email and Password authentication enabled
- Git

### 2. Clone the Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd KisanMitra
```

### 3. Install Dependencies

Install the frontend dependencies:

```bash
npm install
```

### 4. Configure Environment Variables

Copy the backend template and set the Supabase Postgres connection, Supabase project URL, Auth API key, and a private JWT secret. The Spring backend loads `backend/.env` automatically when started from `backend/`.

```powershell
Copy-Item backend/.env.example backend/.env
```

Keep the service-role key and database password on the backend only. Never commit `backend/.env` or expose a service-role key in a `VITE_*` variable.

### 5. Configure Frontend Environment (Optional)

Create a root `.env` only if you need a different backend host:

```env
VITE_API_TARGET=http://localhost:5000
VITE_API_BASE_URL=/api
```

### 6. Run the Application

Start the backend in one terminal:

```bash
cd backend
mvn spring-boot:run
```

Start the frontend in another terminal from the project root:

```bash
npm run dev
```

The application will be available at:

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:5000 |
| Backend Health Check | http://localhost:5000/api/health |

The health endpoint reports the active database connection and returns HTTP 503 if it is unavailable.

---

## 🚀 Production Deployment

The API in `backend/` is a Spring Boot 3 service backed by PostgreSQL and Supabase Auth. Set its environment variables through the deployment platform. The React client continues to use the `/api` base path.

KisanMitra supports separate frontend and backend deployments.

### Backend Deployment – Render

1. Create a new Web Service on [Render](https://render.com/).
2. Connect your GitHub repository.
3. Set the root directory to `backend`.
4. Configure:

| Setting | Value |
|---|---|
| Runtime | Java 21 |
| Build Command | `mvn -q -DskipTests package` |
| Start Command | `java -jar target/kisanmitra-api-1.0.0.jar` |
| Health Check Path | `/api/health` |

Add these environment variables:

```env
CLIENT_URL=https://your-frontend-domain
DATABASE_URL=jdbc:postgresql://your-supabase-host:5432/postgres?sslmode=require
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_database_password
DATABASE_DRIVER=org.postgresql.Driver
JWT_SECRET=your_private_secret_at_least_32_characters
SUPABASE_URL=https://your-project-ref.supabase.co
SUPABASE_ANON_KEY=your_supabase_anon_key
SUPABASE_SERVICE_ROLE_KEY=your_server_only_service_role_key
```

Keep the service-role key and database credentials in the backend environment only.

### Frontend Deployment – Vercel

1. Import your GitHub repository into [Vercel](https://vercel.com/).
2. Set the root directory to the repository root.
3. Configure the build environment variable:

```env
VITE_API_BASE_URL=https://your-backend-domain/api
```

4. Deploy the frontend.

The project includes a `vercel.json` configuration for React frontend route handling.

After deployment, test registration, login, marketplace browsing, and backend connectivity.

---

## 🔐 Security & Production Considerations

- JWT secrets and database credentials must remain private.
- Environment variables should be configured through deployment dashboards.
- Production requires working Supabase Postgres and Email/Password Auth configuration.
- Uploaded listing images use Supabase Storage; keep its service-role key on the backend only.
- Market price records currently contain sample data. A maintained and validated market-price source is required before presenting prices as live market data.
- Free hosting services may sleep after inactivity, resulting in slower initial requests.

---

## 📋 Available Commands

Run commands from the repository root unless specified otherwise.

| Command | Description |
|---|---|
| `npm install` | Install frontend dependencies |
| `cd backend && mvn spring-boot:run` | Start the backend |
| `npm run dev` | Start the frontend |
| `npm run build` | Build frontend for production |
| `npm run lint` | Run frontend ESLint |

---

## 🔮 Future Enhancements

Potential improvements for future versions:

- Integration with live agricultural market price APIs.
- Online payment gateway integration.
- Cloud-based image storage.
- Advanced location-based product discovery.
- Push notifications for offers and orders.
- Advanced analytics for farmers and sellers.
- Improved mobile responsiveness and accessibility.

---

## 🎯 Project Objective

KisanMitra aims to create a unified digital ecosystem for agricultural commerce by enabling farmers, buyers, and service providers to interact directly.

By combining marketplace functionality, real-time communication, transparent negotiations, and multilingual accessibility, the platform aims to make agricultural trading simpler and more efficient.

---

## 👨‍💻 Contributors

Developed as a full-stack web development project.

**Project:** KisanMitra  
**Domain:** Agriculture | E-Commerce | Full-Stack Web Development

---

⭐ **If you find KisanMitra interesting, consider giving this repository a star!**


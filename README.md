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
| Backend | Node.js, Express.js |
| Database | MongoDB |
| Authentication | JSON Web Token (JWT) |
| Real-Time Communication | Socket.IO |
| Internationalization | i18next |
| Development Tools | npm, Git, GitHub |

---

## 📁 Project Structure

```text
KisanMitra/
│
├── client/                     # React frontend
│   ├── public/
│   │   └── locales/            # English, Hindi, Marathi translations
│   └── src/
│       ├── components/
│       ├── pages/
│       ├── services/
│       └── ...
│
├── server/                     # Express backend
│   ├── config/                 # Database and socket configuration
│   ├── controllers/            # Request handling logic
│   ├── middleware/             # Authentication and authorization
│   ├── models/                 # MongoDB models
│   ├── routes/                  # API routes
│   └── ...
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
- [MongoDB Atlas](https://www.mongodb.com/products/platform/atlas) account or another accessible MongoDB deployment
- Git

### 2. Clone the Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd KisanMitra
```

### 3. Install Dependencies

Install the root, backend, and frontend dependencies.

```bash
npm install

cd server
npm install

cd ../client
npm install

cd ..
```

### 4. Configure Environment Variables

Create the backend environment file.

**Windows PowerShell:**

```powershell
Copy-Item server/.env.example server/.env
```

Open `server/.env` and configure:

```env
NODE_ENV=development
PORT=5000

CLIENT_URL=http://localhost:5173

JWT_SECRET=your_long_random_secret
JWT_EXPIRE=30d

MONGODB_URI=your_mongodb_connection_string
```

**Important:**
- Replace the MongoDB URI with your actual database connection string.
- Use a strong, private JWT secret.
- Never commit `.env` files to GitHub.
- URL-encode special characters in MongoDB passwords.

During development, the backend can fall back to a temporary in-memory MongoDB instance if Atlas is unavailable. Data stored in this fallback is not persistent.

### 5. Configure Frontend Environment (Optional)

Create `client/.env`:

```env
VITE_API_TARGET=http://localhost:5000
VITE_API_BASE_URL=/api
```

### 6. Run the Application

From the project root:

```bash
npm run dev
```

The application will be available at:

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:5000 |
| Backend Health Check | http://localhost:5000/api/health |

A successful health check should indicate that the database is connected.

**Run separately if required:**

```bash
npm run server
```

```bash
npm run client
```

---

## 🚀 Production Deployment

The replacement backend is being built in `backend/` as a Spring Boot 3 service backed by PostgreSQL. For local development, start PostgreSQL with `docker compose -f backend/docker-compose.yml up -d`, then run the API from `backend/` with `mvn spring-boot:run`. Configure `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `JWT_SECRET` in the environment. The React client continues to use the `/api` base path.

KisanMitra supports separate frontend and backend deployments.

### Backend Deployment – Render

1. Create a new Web Service on [Render](https://render.com/).
2. Connect your GitHub repository.
3. Set the root directory to `server`.
4. Configure:

| Setting | Value |
|---|---|
| Build Command | `npm ci` |
| Start Command | `npm start` |
| Health Check Path | `/api/health` |

Add these environment variables:

```env
NODE_ENV=production
CLIENT_URL=https://your-frontend-domain
JWT_SECRET=your_production_secret
JWT_EXPIRE=30d
MONGODB_URI=your_mongodb_connection_string
```

Ensure MongoDB Atlas allows connections from your backend hosting environment.

### Frontend Deployment – Vercel

1. Import your GitHub repository into [Vercel](https://vercel.com/).
2. Set the root directory to `client`.
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
- Production requires a working MongoDB connection.
- Uploaded listing images currently use local server storage. Persistent object storage should be configured before relying on production image uploads.
- Market price records currently contain sample data. A maintained and validated market-price source is required before presenting prices as live market data.
- Free hosting services may sleep after inactivity, resulting in slower initial requests.

---

## 📋 Available Commands

Run commands from the repository root unless specified otherwise.

| Command | Description |
|---|---|
| `npm install` | Install root dependencies |
| `npm run dev` | Start frontend and backend |
| `npm run server` | Start backend in development mode |
| `npm run client` | Start frontend development server |
| `npm run build` | Build frontend for production |
| `npm start` | Start backend in production mode (inside server) |
| `npm run lint` | Run frontend ESLint (inside client) |

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


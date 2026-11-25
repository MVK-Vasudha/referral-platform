# Referral Platform

A full-stack referral and professional networking platform with a Spring Boot backend and a React frontend.

## Project Structure

- `backend/referral-backend`: Spring Boot API
- `frontend/referral-frontend`: React + Vite client
- `code/`: mirrored source snapshots

## Backend

Tech stack:

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL
- JWT authentication

Backend location:

- [backend/referral-backend](D:/Computer%20Science/projects/referral-platform/backend/referral-backend)

Default backend port:

- `8081`

Database configuration is currently defined in:

- [application.properties](D:/Computer%20Science/projects/referral-platform/backend/referral-backend/src/main/resources/application.properties)

Run the backend:

```powershell
cd backend\referral-backend
.\mvnw spring-boot:run
```

## Frontend

Tech stack:

- React 19
- Vite
- React Router
- Axios
- Tailwind CSS

Frontend location:

- [frontend/referral-frontend](D:/Computer%20Science/projects/referral-platform/frontend/referral-frontend)

Run the frontend:

```powershell
cd frontend\referral-frontend
cmd /c npm install
cmd /c npm run dev
```

Lint the frontend:

```powershell
cd frontend\referral-frontend
cmd /c npm run lint
```

## Current Frontend Features

- Authentication: login and registration
- Profile: view and edit current user profile
- Search: filter users by name, headline, company, and skills
- Connections: view accepted connections and manage pending requests
- Referrals: view sent and received referrals, accept or reject received referrals
- Search actions: send connection requests and submit referral requests directly from search results

## UI Notes

The frontend has been refined to use shared UI patterns for:

- buttons
- inputs
- pagination
- page headers
- page layout
- status messages for loading, error, success, and empty states

## Important Notes

- Backend contracts are treated as fixed
- Frontend API calls go through the existing service layer
- The backend currently expects MySQL to be available locally
- The frontend Axios base URL is configured for `http://localhost:8081`

## Quick Start

1. Start MySQL and ensure the `referral_db` database is available.
2. Start the backend from [backend/referral-backend](D:/Computer%20Science/projects/referral-platform/backend/referral-backend).
3. Start the frontend from [frontend/referral-frontend](D:/Computer%20Science/projects/referral-platform/frontend/referral-frontend).
4. Open the frontend in your browser and register or log in.

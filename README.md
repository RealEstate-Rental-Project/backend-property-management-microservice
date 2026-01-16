# Property Management Microservice

A core component of a real estate rental platform that manages property listings, rooms, and images while providing intelligent property recommendations, price predictions, and market analytics through ML-powered integrations.

## 📋 Overview

The Property Management Microservice is a Spring Boot-based REST API service that serves as the central hub for property management operations in a distributed real estate rental system. It handles CRUD operations for properties and their associated rooms/images, while seamlessly integrating with multiple ML-powered microservices to provide enhanced features like personalized recommendations, dynamic pricing, and market insights.

### Key Features

- **Property Management**: Complete lifecycle management for rental properties
- **Room & Image Management**: Multi-room support with image storage via Supabase
- **Smart Recommendations**: ML-based property recommendations tailored to user preferences
- **Dynamic Pricing**: AI-powered price predictions for monthly and daily rentals
- **Market Analytics**: Heat map visualizations showing rental market trends
- **Secure Authentication**: JWT-based security with role-based access control
- **Advanced Search**: Dynamic property search with multiple filter criteria
- **Microservices Integration**: OpenFeign clients for seamless inter-service communication

---

## 🏗️ Architecture

![Architecture Diagram](C:/Users/DELL/.gemini/antigravity/brain/767bb748-8405-4e97-a890-989b3409fc01/architecture_diagram_1768524175183.png)

### System Components

#### Core Service
- **Property Management Microservice**: Central service handling property CRUD operations, room management, and orchestrating calls to external services

#### External Microservices (OpenFeign Integration)
- **User Management Microservice**: Provides user profile data for personalized recommendations
- **Property Recommendation Model**: ML service delivering personalized property suggestions based on user preferences
- **Price Suggestion Service**: ML service predicting optimal rental prices (monthly/daily)
- **Heat Map Prediction Service**: Analytics service generating market heat maps by location and rental type

#### Infrastructure
- **MySQL Database**: Persistent storage for properties, rooms, and images metadata
- **Supabase Storage**: Cloud storage for property and room images
- **Spring Cloud Config Server**: Centralized configuration management
- **API Gateway**: Request routing and load balancing
- **JWT Security Layer**: Authentication and authorization

### Data Flow

1. **Client Request** → API Gateway → Property Management Microservice
2. **Authentication** → JWT token validation via Security Filter
3. **Property Operations** → Service layer → JPA Repository → MySQL
4. **Image Operations** → Supabase Storage Service → Cloud Storage
5. **ML Features** → OpenFeign Clients → External ML Microservices
6. **Response** → DTO Mapping → Client

---

## 🗂️ Core Components

### Entities

#### **Property**
Represents a rental property with comprehensive details.

**Key Fields:**
- `idProperty` (Long): Primary key
- `onChainId` (Long): Legacy identifier (initially for blockchain, now for tracking)
- `title`, `description`: Property details
- `country`, `city`, `address`: Location information
- `longitude`, `latitude`: Geo-coordinates for mapping and analytics
- `sqM` (Integer): Property size in square meters
- `typeOfProperty` (PropertyType enum): APARTMENT, HOUSE, STUDIO, etc.
- `typeOfRental` (TypeOfRental enum): MONTHLY, DAILY
- `rentAmount`, `securityDeposit` (Long): Pricing information
- `ownerId`, `ownerEthAddress`: Owner information
- `isActive`, `isAvailable` (Boolean): Status flags
- `rating`, `nombreEtoiles`: Property ratings
- `rooms` (List<Room>): One-to-many relationship with rooms

#### **Room**
Represents individual rooms within a property.

**Key Fields:**
- `idRoom` (Long): Primary key
- `name` (String): Room name/type (e.g., "Living Room", "Bedroom 1")
- `orderIndex` (Integer): Display order
- `property` (Property): Many-to-one relationship
- `roomImages` (List<RoomImage>): One-to-many relationship with images

#### **RoomImage**
Stores image metadata for room photos.

**Key Fields:**
- `id` (Long): Primary key
- `imageUrl` (String): Supabase storage URL
- `room` (Room): Many-to-one relationship

### Controllers

#### **PropertyController** (`/api/property-microservice/properties`)
- `POST /`: Create new property (LANDLORD role required)
- `GET /`: Retrieve all properties
- `GET /{id}`: Get property by ID
- `GET /recent`: Get 3 most recent available properties
- `GET /my-properties`: Get authenticated user's properties
- `PUT /{id}`: Update property details
- `DELETE /{id}`: Mark property as inactive
- `POST /search`: Advanced search with filters
- `GET /recommendations`: Get personalized property recommendations
- `GET /predict-price/{id}`: Get ML price prediction
- `GET /market-heatmap`: Get market analytics heatmap
- `PUT /{id}/availability/false`: Mark property as unavailable
- `PUT /{id}/availability/true`: Mark property as available

#### **RoomController** (`/api/property-microservice/rooms`)
- `POST /property/{propertyId}`: Create room for a property
- `GET /`: Get all rooms
- `GET /{roomId}`: Get room by ID
- `GET /property/{propertyId}`: Get all rooms for a property
- `PUT /{roomId}`: Update room details
- `DELETE /{roomId}`: Delete room and associated images

#### **RoomImageController** (`/api/property-microservice/room-images`)
- `POST /room/{roomId}`: Upload image for a room
- `GET /room/{roomId}`: Get all images for a room
- `DELETE /{imageId}`: Delete a specific image

### Services

#### **PropertyService**
Core business logic for property management:
- CRUD operations with database persistence
- Integration with User Management service for user profiles
- Orchestration of ML recommendation requests
- Price prediction coordination (monthly/daily)
- Heat map data retrieval for market analytics
- Advanced search using JPA Specifications
- Property availability management

#### **RoomService**
Manages room operations:
- Room creation, update, deletion
- Association with parent properties
- Image relationship management

#### **SupabaseStorageService**
Handles cloud storage operations:
- Image upload to Supabase buckets
- Image deletion and cleanup
- Secure URL generation for image access

### Security

#### **JWT Authentication**
- `JwtAuthFilter`: Validates JWT tokens on each request
- `JwtUtil`: Token generation, parsing, and validation
- `UserPrincipal`: Custom user details for Spring Security
- `SecurityConfig`: Configures security rules and public endpoints

**Protected Endpoints:** Most endpoints require authentication  
**Public Endpoints:** Health checks, actuator endpoints

**Role-Based Access:**
- `LANDLORD`: Can create, update, delete properties
- All authenticated users can view and search properties

---

## 🗄️ Database Schema

### Entity Relationships

```
properties (1) ─────< (N) rooms (1) ─────< (N) room_images
```

### Tables

#### **properties**
- Primary Key: `id_property`
- Unique: `on_chain_id`
- Indexes on: `owner_id`, `is_active`, `is_available`, `city`, `type_of_rental`

#### **rooms**
- Primary Key: `id_room`
- Foreign Key: `property_id` → `properties.id_property`
- Cascade delete when property is removed

#### **room_images**
- Primary Key: `id`
- Foreign Key: `room_id` → `rooms.id_room`
- Cascade delete when room is removed

---

## 🔌 External Service Integrations

### 1. User Management Microservice
**Purpose:** Retrieve user profile data for personalized recommendations

**Endpoint:**
- `GET /api/users/id/{id}`: Fetch user profile with preferences

**Integration:** `UserManagementMicroService` Feign Client

**Data Retrieved:**
- Target rent budget
- Preferred number of rooms
- Target square footage
- Search location (latitude/longitude)
- Preferred property type
- Preferred rental type (monthly/daily)

### 2. Property Recommendation Model
**Purpose:** ML-based property recommendations using collaborative filtering

**Endpoint:**
- `POST /recommend`: Get property recommendations

**Integration:** `PropertyRecommendationModel` Feign Client

**Request Data:**
- User preferences (rent budget, rooms, sqft, location, property type)
- User ID for collaborative filtering

**Response:**
- List of recommended property IDs with confidence scores

### 3. Price Suggestion Service
**Purpose:** ML-powered price predictions for properties

**Endpoints:**
- `POST /predict/monthly`: Monthly rental price prediction
- `POST /predict/daily`: Daily rental price prediction

**Integration:** `PriceSuggestionClient` Feign Client

**Request Data:**
- City, country
- Geolocation (longitude, latitude)
- Square meters
- Number of rooms
- Star rating

**Response:**
- Predicted price in Wei and ETH
- Confidence score

### 4. Heat Map Prediction Service
**Purpose:** Market analytics and visualization

**Endpoint:**
- `GET /api/v1/market/heatmap?type={MONTHLY|DAILY}`: Get market heatmap

**Integration:** `HeatMapPredictionClient` Feign Client

**Response:**
- Heatmap coordinates with density values
- Market trend indicators by location

---

## 🛠️ Technologies Used

### Backend Framework
- **Spring Boot 3.2.0**: Core framework
- **Java 17**: Programming language
- **Spring Data JPA**: Database ORM
- **Hibernate**: JPA implementation

### Security
- **Spring Security**: Authentication & authorization
- **JWT (jjwt 0.11.5)**: Token-based authentication

### Microservices
- **Spring Cloud OpenFeign 4.1.4**: Declarative REST clients
- **Spring Cloud Config**: Centralized configuration

### Database & Storage
- **MySQL**: Relational database (production)
- **H2**: In-memory database (development/testing)
- **Supabase**: Cloud storage for images

### Build & Development
- **Maven**: Build tool
- **Lombok**: Boilerplate code reduction
- **Spring Boot DevTools**: Hot reload during development
- **Spring Boot Actuator**: Application monitoring

### Utilities
- **Apache Tika**: File type detection
- **Spring WebFlux**: Reactive HTTP client

---

## ⚙️ Configuration

### Environment Variables

The service uses Spring Cloud Config Server for centralized configuration. The following properties are externalized:

#### **Database Configuration**
- `spring.datasource.url`: MySQL connection URL
- `spring.datasource.username`: Database username
- `spring.datasource.password`: Database password

#### **External Service URLs**
- `userManagement.service.url`: User Management service base URL
- `microservice.recommendation-ai.url`: Property Recommendation ML service URL
- `ml-price-suggestion.service.url`: Price Suggestion ML service URL
- `heatmap.service.url`: Heat Map service URL

#### **JWT Configuration**
- `jwt.secret`: Secret key for JWT signing
- `jwt.expiration`: Token expiration time

#### **Supabase Configuration**
- `supabase.url`: Supabase project URL
- `supabase.api.key`: Supabase API key
- `supabase.bucket.name`: Storage bucket name

#### **Config Server**
- `CONFIG_SERVER_URL`: Config server location (default: `http://localhost:8888`)

### Application Properties

**File:** `src/main/resources/application.yml`

```yaml
server:
  port: 8084

spring:
  profiles:
    active: prod
  application:
    name: property-microservice
  config:
    import: "optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,env,refresh
  security:
    enabled: false
```

---

## 🚀 Setup and Installation

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **MySQL 8.0+**
- **Config Server** running (for production configuration)
- **Docker** (optional, for containerized deployment)

### Local Development Setup

#### 1. Clone the Repository
```bash
git clone <repository-url>
cd property-management-microservice
```

#### 2. Configure Database
Create a MySQL database:
```sql
CREATE DATABASE property_management_db;
```

#### 3. Set Up Config Server
Ensure your Config Server is running and contains configuration for `property-microservice`.

Alternatively, for local development, create `application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/property_management_db
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret: your-secret-key-min-256-bits
  expiration: 86400000

supabase:
  url: https://your-project.supabase.co
  api:
    key: your-supabase-key
  bucket:
    name: property-images

userManagement:
  service:
    url: http://localhost:8081

microservice:
  recommendation-ai:
    url: http://localhost:5000

ml-price-suggestion:
  service:
    url: http://localhost:5001

heatmap:
  service:
    url: http://localhost:5002
```

#### 4. Build the Project
```bash
mvn clean install
```

#### 5. Run the Application
```bash
mvn spring-boot:run
```

Or activate the dev profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The service will start on **http://localhost:8084**

### Docker Deployment

#### Build Docker Image
```bash
docker build -t property-microservice:latest .
```

#### Run Container
```bash
docker run -p 8084:8084 \
  -e CONFIG_SERVER_URL=http://config-server:8888 \
  -e SPRING_PROFILES_ACTIVE=prod \
  property-microservice:latest
```

### Health Check

Verify the service is running:
```bash
curl http://localhost:8084/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

---

## 📦 Project Structure

```
src/
├── main/
│   ├── java/com/lsiproject/app/propertymanagementmicroservice/
│   │   ├── controllers/           # REST API endpoints
│   │   ├── services/              # Business logic layer
│   │   ├── repository/            # JPA repositories
│   │   ├── entities/              # JPA entities
│   │   ├── DTOs/                  # Data transfer objects
│   │   ├── CreationDTOs/          # DTOs for creation requests
│   │   ├── UpdateDTOs/            # DTOs for update requests
│   │   ├── ResponseDTOs/          # DTOs for responses
│   │   ├── searchDTOs/            # DTOs for search queries
│   │   ├── wrappers/              # Response wrapper classes
│   │   ├── Enums/                 # Enumeration types
│   │   ├── security/              # JWT & Spring Security config
│   │   ├── openFeignClients/      # Feign client interfaces
│   │   ├── mappers/               # Entity-DTO mappers
│   │   ├── exceptions/            # Exception handlers
│   │   └── configuration/         # Additional configurations
│   └── resources/
│       ├── application.yml        # Main configuration
│       └── application-*.yml      # Profile-specific configs
└── test/                          # Unit and integration tests
```

---

## 👨‍💻 Development Guide

### Adding a New Property Field

1. **Update Entity** (`entities/Property.java`)
2. **Update DTOs** (Creation, Update, Response DTOs)
3. **Update Mapper** (`mappers/PropertyMapper.java`)
4. **Database Migration** (if using Flyway/Liquibase)

### Adding a New Endpoint

1. **Define method in Controller**
2. **Implement business logic in Service**
3. **Add security constraints** (if needed)
4. **Update API documentation**

### Testing

Run all tests:
```bash
mvn test
```

Run specific test:
```bash
mvn test -Dtest=PropertyServiceTest
```

---

## 📊 Monitoring & Observability

### Actuator Endpoints

Available at `/actuator`:
- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus-formatted metrics
- `/actuator/env` - Environment properties
- `/actuator/refresh` - Refresh configuration from Config Server

### Logging

The service uses SLF4J with Logback for logging. Configure log levels in Config Server or application properties:

```yaml
logging:
  level:
    com.lsiproject.app.propertymanagementmicroservice: DEBUG
    org.springframework.web: INFO
```

---

## 📝 API Documentation

For detailed API documentation with request/response examples, consider integrating:
- **Springdoc OpenAPI** for auto-generated Swagger UI
- **Postman Collection** for API testing

---

## 🤝 Contributing

1. Create a feature branch from `main`
2. Make changes following coding standards
3. Write/update tests
4. Submit pull request with clear description

---

## 📄 License

[Specify your license here]

---

## 📞 Support

For issues or questions, please contact the development team or create an issue in the repository.

---

**Version:** 0.0.1-SNAPSHOT  
**Last Updated:** January 2026
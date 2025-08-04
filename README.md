# Spring Data Redis

📘 Blog Post: [Spring Data Redis](https://jarmx.blogspot.com/2023/03/spring-data-redis.html)

## Introduction

The Spring Data Redis (SDR) framework makes it easy to write Spring applications that use the Redis key-value store by eliminating the redundant tasks and boilerplate code required for interacting with the store through Spring's excellent infrastructure support.

## Requirements

- **Redis**: Version 2.6 or above
- **Java**: 17
- **Spring Boot**: 3.0.4
- **Spring Data Redis**: 3.0.4

Spring Data Redis integrates with two popular open-source Java libraries for Redis:
- [Lettuce](https://github.com/lettuce-io/lettuce-core) - Event-driven, non-blocking architecture with thread-safety
- [Jedis](https://github.com/redis/jedis) - Simple and easy to use

## Technology Stack

- Spring Boot 3.0.4
- Spring Data Redis 3.0.4
- H2 Database
- Java 17
- Docker
- Maven
- IntelliJ IDEA

## Installation & Setup

### 1. Redis Installation with Docker

```bash
docker run --name my-redis -p 6379:6379 -d redis
```

### 2. Maven Dependencies

#### Lettuce Connector (Recommended for high-concurrency)
```xml
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
    <version>6.2.3.RELEASE</version>
</dependency>
```

#### Jedis Connector (Simple applications)
```xml
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>4.3.1</version>
</dependency>
</dependency>
```

### 3. Configuration

#### Application Configuration (application.yaml)
```yaml
# Server config
server:
  port: 9000

# Redis Config
spring:
  redis:
    host: 127.0.0.1
    port: 6379
```

#### Lettuce Connection Factory
```java
@Configuration
public class AppConfig {
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
    }
}
```

#### Jedis Connection Factory
```java
@Configuration
public class AppConfig {
    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6379);
        return new JedisConnectionFactory(config);
    }
}
```

#### RedisTemplate Configuration
```java
@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.setConnectionFactory(connectionFactory);
    return template;
}
```

## Implementation

### Main Application Class
```java
@SpringBootApplication
@EnableCaching
public class SpringBootRedisCacheApplication implements CommandLineRunner {
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final PersonRepository personRepository;

    public SpringBootRedisCacheApplication(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootRedisCacheApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Current user count is {}.", personRepository.count());
        Person p1 = new Person("p1","test", 25);
        Person p2 = new Person("p2", "test",28);
        Person p3 = new Person("p3", "test", 60);
        personRepository.save(p1);
        personRepository.save(p2);
        personRepository.save(p3);
        LOG.info("Data: {}.", personRepository.findAll());
    }
}
```

### Repository
```java
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
}
```

### Controller with Caching Annotations
```java
@RestController
public class PersonController {
    private final RedisTemplate<String, Object> redisTemplate;
    private final PersonRepository PersonRepository;

    public PersonController(RedisTemplate<String, Object> redisTemplate, PersonRepository PersonRepository) {
        this.redisTemplate = redisTemplate;
        this.PersonRepository = PersonRepository;
    }

    @GetMapping("/personByRedisTemplate")
    public Person getPersonByRedisTemplate(){
        var person = (Person) redisTemplate.opsForValue().get("personId");
        return person!=null?person:null;
    }

    @Cacheable(value = "persons", key = "#personId", unless = "#result.age < 29")
    @GetMapping("/{personId}")
    public Person getPerson(@PathVariable Long personId) {
        var person = PersonRepository.findById(personId).get();
        redisTemplate.opsForValue().set("personId", person);
        return person;
    }

    @CachePut(value = "persons", key = "#person.id")
    @PutMapping("/update")
    public Person updatePersonByID(@RequestBody Person person) {
        PersonRepository.save(person);
        return person;
    }

    @CacheEvict(value = "persons", allEntries=true)
    @DeleteMapping("/{personId}")
    public void deletePersonByID(@PathVariable Long personId) {
        PersonRepository.delete(new Person(personId));
    }
}
```

## Caching Annotations

- **@Cacheable**: Caches the result of a method call. The method won't be executed if the result is already in the cache.
- **@CachePut**: Always executes the method and updates the cache with the result.
- **@CacheEvict**: Removes entries from the cache.
- **@EnableCaching**: Enables Spring's annotation-driven cache management capability.

## Usage

### Running the Application
```bash
mvn spring-boot:run
```

### API Testing with cURL

#### Get Person by ID (with caching)
```bash
curl http://127.0.0.1:9000/1
```

#### Get Person using RedisTemplate
```bash
curl http://127.0.0.1:9000/personByRedisTemplate
```

#### Update Person (cache update)
```bash
curl --location --request PUT 'http://127.0.0.1:9000/update' \
--header 'Content-Type: application/json' \
--data '{"id" : 3, "firstname" : "p4", "lastname" : "test", "age" : 30 }'
```

#### Delete Person (cache eviction)
```bash
curl --location --request DELETE 'http://127.0.0.1:9000/3'
```

## RedisTemplate Operations

The `RedisTemplate` is the central class of the Redis module, offering a high-level abstraction for Redis interactions. Use `opsForValue()` method to interact with Redis as a key-value store:

```java
// Store value
redisTemplate.opsForValue().set("key", value);

// Retrieve value
Object value = redisTemplate.opsForValue().get("key");
```

## Lettuce vs Jedis

**Lettuce** (Recommended):
- Event-driven, non-blocking architecture
- Thread-safe
- Good choice for high-concurrency environments

**Jedis**:
- Simple and easy to use
- Good choice for simpler applications
- Blocking I/O

Choose based on your specific application requirements and deployment environment.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   ├── config/
│   │   │   └── AppConfig.java
│   │   ├── controller/
│   │   │   └── PersonController.java
│   │   ├── repository/
│   │   │   └── PersonRepository.java
│   │   ├── model/
│   │   │   └── Person.java
│   │   └── Application.java
│   └── resources/
│       └── application.yaml
└── test/
```



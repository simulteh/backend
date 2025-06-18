# Net-Genius Backend

Spring Boot приложение для управления преподавателями с JWT аутентификацией и Swagger документацией.

## 🚀 Быстрый старт

### Требования
- Java 17+
- Maven 3.6+ (или используйте Maven Wrapper)

### Запуск приложения

1. **Клонируйте репозиторий**
   ```bash
   git clone <repository-url>
   cd tyui/backend
   ```

2. **Настройте базу данных**
   - Отредактируйте `src/main/resources/application.properties`
   - Укажите параметры подключения к вашей БД

3. **Запустите приложение**

   **Вариант A: Maven Wrapper (рекомендуется)**
   ```bash
   # Windows
   .\mvnw.cmd spring-boot:run
   
   # Linux/Mac
   ./mvnw spring-boot:run
   ```

   **Вариант B: Maven (если установлен)**
   ```bash
   mvn spring-boot:run
   ```

   **Вариант C: IDE**
   - Откройте проект в IntelliJ IDEA или Eclipse
   - Запустите класс `NetGeniusApplication`

4. **Откройте Swagger UI**
   ```
   http://localhost:8080/swagger-ui.html
   ```

## 📚 Документация API

### Swagger UI
Интерактивная документация доступна по адресу:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI спецификация
- JSON: `http://localhost:8080/api-docs`
- YAML: `http://localhost:8080/api-docs.yaml`

### Подробная документация
См. файл [SWAGGER_DOCUMENTATION.md](SWAGGER_DOCUMENTATION.md) для полного описания API.

## 🔐 Аутентификация

API использует JWT токены. Для получения токена:

1. Зарегистрируйтесь: `POST /auth/sign-up`
2. Авторизуйтесь: `POST /auth/sign-in`
3. Используйте полученный токен в заголовке: `Authorization: Bearer <token>`

## 📋 Основные эндпоинты

### Аутентификация
- `POST /auth/sign-up` - Регистрация
- `POST /auth/sign-in` - Авторизация

### Преподаватели
- `GET /api/teachers` - Все преподаватели
- `POST /api/teachers` - Создание преподавателя
- `GET /api/teachers/{id}` - Преподаватель по ID
- `PUT /api/teachers/{id}` - Обновление преподавателя
- `DELETE /api/teachers/{id}` - Удаление преподавателя
- `POST /api/teachers/filter` - Фильтрация с пагинацией

## 🛠️ Разработка

### Структура проекта
```
src/main/java/com/simul_tech/netgenius/
├── configurators/     # Конфигурации (Security, Swagger)
├── controllers/       # REST контроллеры
├── models/           # Сущности и DTO
├── repositories/     # Репозитории
├── services/         # Бизнес-логика
└── security/         # JWT аутентификация
```

### Полезные команды

**Сборка проекта:**
```bash
# Maven Wrapper
.\mvnw.cmd clean compile

# Maven
mvn clean compile
```

**Запуск тестов:**
```bash
# Maven Wrapper
.\mvnw.cmd test

# Maven
mvn test
```

**Сборка JAR:**
```bash
# Maven Wrapper
.\mvnw.cmd clean package

# Maven
mvn clean package
```

**Запуск JAR:**
```bash
java -jar target/netgenius-0.0.1-SNAPSHOT.jar
```

### Логирование
Логи сохраняются в файлы:
- `logs/application.log` - Общие логи
- `logs/error.log` - Ошибки
- `logs/sql.log` - SQL запросы

## 📝 Конфигурация

Основные настройки в `application.properties`:
- База данных
- JWT секреты
- Swagger UI
- Логирование

## 🐛 Решение проблем

### Maven не найден
Если команда `mvn` не работает:
1. Используйте Maven Wrapper: `.\mvnw.cmd` (Windows) или `./mvnw` (Linux/Mac)
2. Или установите Maven (см. [MAVEN_SETUP.md](MAVEN_SETUP.md))

### Порт 8080 занят
Измените порт в `application.properties`:
```properties
server.port=8081
```

### Проблемы с БД
Для разработки можно использовать H2 (встроенная БД):
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.h2.console.enabled=true
```

## 📖 Дополнительная документация

- [Swagger Documentation](SWAGGER_DOCUMENTATION.md) - Полное описание API
- [Swagger Guide](SWAGGER_GUIDE.md) - Руководство по использованию Swagger
- [Maven Setup](MAVEN_SETUP.md) - Установка и настройка Maven

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте ветку для новой функции
3. Внесите изменения
4. Добавьте тесты
5. Создайте Pull Request

## 📞 Поддержка

Для вопросов обращайтесь к команде разработки Net-Genius.


# AuthenticationAuthorizationBankApp

Этот проект представляет собой серверную часть банковского приложения, включающую функционал аутентификации и авторизации пользователей.

## Содержание

- [Описание](#описание)
- [Функциональность](#функциональность)
- [Технологии](#технологии)
- [Установка](#установка)

## Описание

Этот проект разработан для обеспечения безопасной аутентификации и авторизации пользователей в банковском приложении. Он предоставляет RESTful API для управления пользователями, их учетными записями и транзакциями.

## Функциональность

- Регистрация и аутентификация пользователей
- Такие дополнительные методы аутентификации, как: многофакторная аутентификация, аутентификация по местоположению, с использованием JWT токенов
- Авторизация на основе ролей и на основе прав доступа
- Управление банковскими счетами
- Управление транзакциями
- Получение баланса и истории транзакций

## Технологии

Проект использует следующие технологии и библиотеки:

- Spring Security
- PostgreSQL
- JSON Web Tokens (JWT)
- bcrypt
- Lombok
- Thymeleaf

## Установка

Для установки и запуска проекта выполните следующие шаги:

1. Клонируйте репозиторий:

```bash
git clone https://github.com/abai45/AuthenticationAuthorizationBankApp--backend.git
```

2. Перейдите в директорию проекта:

```bash
cd AuthenticationAuthorizationBankApp--backend
```

3. Создайте новую базу данных reactbankapp

```plaintext
spring.datasource.url=jdbc:postgresql://localhost:5432/reactbankapp
```

4. Отредактируйте данные под свои в файле application-dev.properties:

```bash
#Database
POSTGRESQL_USERNAME=*your_db_username*
POSTGRESQL_PASSWORD=*your_db_password*

#Email Config
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_ID=*your_email*
EMAIL_PASSWORD=*your_email_password*
VERIFY_EMAIL_HOST=http://localhost:8080
```

5. Запустите проект:
```bash
mvn spring-boot:run
```

## Использование

После запуска сервера, вы можете использовать Postman или аналогичный инструмент для взаимодействия с API. 

## Frontend приложения

**Ссылка на гитхаб с фронтендом**: https://github.com/Munkhadilio/ReactNative-VaBank/commits/main/

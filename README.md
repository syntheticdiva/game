Аутентификация
Используется JWT-токен, который необходимо передавать в заголовке:

Authorization: Bearer <ваш_токен>


1. Авторизация и регистрация (/api/auth)
POST /auth/login
*Аутентификация пользователя

Тело запроса:

json
{
"email": "string",
"password": "string"
}

POST /auth/register/user
*Регистрация обычного пользователя

Тело запроса:

json
{
"login": "string",
"name": "string",
"email": "string",
"password": "string"
}


POST /auth/register/admin
*Регистрация администратора 

Тело запроса: аналогично регистрации пользователя


2. Управление игровыми сессиями (/api/games)
   POST /games
*Создать новую игровую сессию
Требуется аутентификации

POST /games/{gameId}/join
*Присоединиться к существующей игре

Требуется аутентификация


POST /games/{gameId}/start
*Начать игру (только для создателя сессии)

Требуетя аутентификация


POST /games/{gameId}/turn
Описание: Сделать ход в игре

Требуется аутентификация


GET /games
*Получить список активных игр


GET /games/{gameId}/status
*Получить детализированный статус игры

3. Работа с ходами (/api/games/{gameId}/turns)
   GET /games/{gameId}/turns
*Получить все ходы в игре


GET /games/{gameId}/turns/players/{playerId}
*Получить ходы конкретного игрока в игре


4. Управление пользователями (/api/users)
   GET /users/{id}
*Получить данные пользователя
Требуется аутентификация


GET /users
*Получить всех пользователей 


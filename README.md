# backend
backend for netgenius
Пояснения:
1. Создание нового курса
HTTP-метод: POST
URL: /api/courses
Тело запроса (JSON):
{
    "title": "Название курса",
    "description": "Описание курса",
    "price": 100,
    "duration": 30,
    "active": true
}
2. Обновление существующего курса
HTTP-метод: PUT
URL: /api/courses/{id} (где {id} - ID курса)
Тело запроса (JSON):
{
    "title": "Обновленное название",
    "description": "Обновленное описание",
    "price": 120,
    "duration": 45,
    "active": false
}
3. Удаление курса
HTTP-метод: DELETE
URL: /api/courses/{id} (где {id} - ID курса)
4. Получение информации о курсах
Получение всех курсов:
GET /api/courses
Получение курса по ID:
GET /api/courses/{id}
Фильтрация по цене:


GET /api/courses/filter/price/between?minPrice=X&maxPrice=Y - поиск по диапазону цен

GET /api/courses/filter/price/min?minPrice=X - поиск ценой от X

GET /api/courses/filter/price/max?maxPrice=Y - поиск ценой до Y

Фильтрация по продолжительности:

GET /api/courses/filter/duration/min?minDuration=X - поиск с минимальной продолжительностью X

GET /api/courses/filter/duration/max?maxDuration=Y - поиск с максимальной продолжительностью Y

Фильтрация по активности:

GET /api/courses/filter/active?active=true - активные курсы

GET /api/courses/filter/active?active=false - неактивные курсы
Важные замечания:
Автоматические поля:

При создании курса автоматически генерируются:

id (уникальный идентификатор)

created_at (дата создания)

updated_at (дата последнего обновления)

Эти поля не нужно передавать в запросе
Обновление дат:
При обновлении курса автоматически обновляется updated_at
Поле created_at остается неизменным

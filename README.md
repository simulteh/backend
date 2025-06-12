# backend
backend for netgenius 

Поля модели:
•	id_work – уникальный идентификатор работы (UUID или автоинкрементное число).
•	id_user – идентификатор отправителя (студента).
•	id_recipient – идентификатор получателя (преподавателя).
•	id_departure – идентификатор отправленного сообщения (для группировки файлов).
•	id_time – дата и время отправки (автоматически заполняется).
•	file_name – название файла.
•	file_path – путь к файлу в хранилище.
•	file_size – размер файла (в МБ, валидация до 100 МБ).
•	file_type – тип файла (pdf, doc, xlsx, txt, png, jpeg).
•	comment – комментарий студента (макс. 1000 символов).
•	status – статус работы (completed, in_progress, graded).
•	grade – оценка (если выставлена преподавателем).
•	is_closed – флаг закрытия комментариев (булево значение, по умолчанию false).

Методы репозитория:
•	save(work: LaboratoryWork): LaboratoryWork – сохранение работы.
•	findById(id_work: ID): Optional<LaboratoryWork> – поиск работы по ID.
•	findAllByUserId(id_user: ID): List<LaboratoryWork> – получение всех работ студента.
•	findAllByRecipientId(id_recipient: ID): List<LaboratoryWork> – получение всех работ, отправленных преподавателю.
•	filterByStatus(status: String): List<LaboratoryWork> – фильтрация по статусу.
•	filterByDateRange(start: DateTime, end: DateTime): List<LaboratoryWork> – фильтрация по дате.
•	update(work: LaboratoryWork): LaboratoryWork – обновление данных работы.
•	deleteById(id_work: ID): void – удаление работы по ID.

•	deleteAllByUserId(id_user: ID): void – удаление всех работ студента.

Маппер для конвертации:
•	LaboratoryWork → LaboratoryWorkDTO (для передачи через API).
•	LaboratoryWorkDTO → LaboratoryWork (из запроса в сущность).
Поля LaboratoryWorkDTO:
Те же, что у LaboratoryWork, но без служебных полей (id_time, file_path).

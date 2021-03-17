# Укорачиватель ссылок

Стек: [sbt](http://www.scala-sbt.org/), [scala](https://www.scala-lang.org/), [akka-http](https://doc.akka.io/docs/akka-http/current/?language=scala), [swagger](https://swagger.io/).

Jar: `sbt assembly`

Test: `sbt test`

Docker: 
 `sbt docker`
 `docker run -d -p 9090:9090 url-short-service/url-short-service`
 
Swagger: http://localhost:9090/swagger-ui/index.html

Добавления url:
  - сложность выполнения запроса: O(1)
  
Редирект по short url:
  - сложность выполнения запроса: O(1)
  
 #### Замечания и дальнейшие доработки
 
1. Хранить данные в NoSQL или RDBMS базе данных.
2. Сейчас есть две реализации storage
  - в реализации по defoult возможны не разрешимые конфликты short url при создании.
  - во второй реализации решена проблема с конфликтами, повлияло на производительность создания short url
3. Написать нормальные unit test

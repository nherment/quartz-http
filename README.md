
Schedule HTTP callouts with a given payload through HTTP

eg:

    POST localhost:8080/api/schedule
    {
      "timestamp": 1397733237027,
      "url": "http://localhost:3000",
      "payload": "{\"Hello\":\"world\"}"
    }

will schedule a callout ```POST http://localhost:3000``` with ```{"Hello": "world"}``` as the body

Currently only supports Http POST, JSON content and one single date.


# run:

    mvn clean compile exec:java 

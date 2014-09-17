
Schedule HTTP callouts with a given payload through HTTP
========================================================

# To add a job to the queue:

    POST localhost:8080/scheduler/api
    {
      "timestamp": 1397733237027,
      "url": "http://localhost:3000",
      "payload": "{\"Hello\":\"world\"}"
    }
    
will schedule a callout ```POST http://localhost:3000/``` with ```{"Hello": "world"}``` as the body.

You will receive back a JSON object that has the group::name pair. You will need to hang on to this if you ever intend to cancel the job.

# To remove a job from the queue:

    DELETE localhost:8080/scheduler/api/group::name

This will remove a previously scheduled job with the unique key of group::name. The JSON returned will show failure if the job cannot be found or it was not possible to cancel it. 

# HTTP verbs supported

Currently supports Http POST, JSON content and one single date to add to the schedule and DELETE with the group::name on the url to remote a scheduled job.

# run server:

    mvn jetty:run

# compile war file to /target directory:

    mvn package 


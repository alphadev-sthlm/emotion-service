# Service startup

mvn spring-boot:run

# Example service usage

Please note that `@` must be present before `ABSOLUTE_PATH_OF_FILE`

``
curl -H "Content-Type: application/octet-stream" -v -X POST localhost:8080/emotions --data-binary @ABSOLUTE_PATH_OF_FILE -o /tmp/new-image.jpg
```

/tmp/new-image.jpg would be the same image with a label of the strongest emotion of each face.

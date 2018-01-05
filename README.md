# Java-Challenge

This is a simple implementation of bank account transfer API. The concurrent access of accounts is managed by Reeentrant locks taken in ascending order of account id always. The implementation depends on timeout defined AcocuntsService which can be made configurable based on requirement on number of concurrent threads. A Junit tests the concurrent implementation by spawning thousands of therads and calling REST API to transfer amount.

Further Enhancements

1. Implement NotificationService supporting asyncronous execution using Spring Async as already annotated in the code. The AsyncExcutor can be customized based on the requirement.The NotificationService itself can be developed as a micro-service.

2. Implement exception handlers in REST controller for business and validation exceptions.

3. configure spring security to prohibit unauthorised access

4. To make this application product ready, add spring actuator dependency and customize the endpoints.

5. Use Swagger to document the REST APIs.That makes REST APIs easy to read,iterate and consume.

6. Make necessary changes to Gradle configuration deploy this application as war

7. start Jenkins pipeline for auto deployment.Generate test reports get an early feedback
If the application has to be deployed as micro-service on PASS like PCF.
"# Java-Challenge" 

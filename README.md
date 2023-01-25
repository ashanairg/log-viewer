# LogViewer

LogViewer is a web application that provides REST end points for viewing logs located at /var/logs in a Linux-based system. It exposes end-points for viewing and searching within a log file. There is no UI support at this time. It will be added in the future if there is enough interest.

This application exposes two end points

* `/logs/{file-name}?page=1&size=100`

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;This is to view the logs in the given file. It retrieves the logs in `/var/log/file-name`, with the most recent logs being displayed the first.
* `/logs/{file-name}/search?q=<query string>&size=50`

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;This end point filters the given log file based on the query string. It has an optional parameter `size`, which limits the results to the specified number. The most recent results will be displayed first.
	
## QuickStart

* `git clone` or download the code from git
* Start the server with `./mvnw spring-boot:run`
* Java 17 and maven are required to run this software
 
### Reference Documentation

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Search for a string in a file in Java](https://medium.com/geekculture/search-string-in-file-with-java-as-fast-as-possible-fedafdc7a3ee)


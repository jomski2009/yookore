# Yookore
## Installation
- Ensure gradle (2.1) is installed
- run 'gradle build' to build
- 'java -jar build/libs/yookore-1.0.1.war' to run the application locally

## Usage
Access the running application at "localhost:8081"

**POST**    notification/status
```sh
{
  "userID":"1234",
  "username":"tcrews",
  "enabled":true
}
```
**GET**     notification/status/{id}
```sh
{
  "userID":"1234",
  "username":"tcrews",
  "enabled":true
}
```

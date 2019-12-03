# Clean Architecture - Spring - Kotlin  

This is a simple hands-on Spring application demonstrating how to implement [Clean Architecture (Uncle Bob)][0].

You can find the accompanying slides, [here][2].

This codebase utilizes injection, to easily swap the persistence gateway. Thus, by only swapping (injecting) another Gateway, one can go from _in-memory_ persistence to a _persistent_ storage (i.e. Mongo), see [here][4], without changing anything else (domain models / logic ect). This accounts for the notion that a database is an _implementation detail._

## Prerequisites

- [Java 8][1]

_Note: this application comes with the [Gradle Wrapper][3], therefore one does not need to install Gradle_
## Setup, build and run

- Make sure prerequisites are met, then clone the repo:

```shell
$ git clone git@github.com:srmds/clean-architecture-spring-kotlin.git
```

- Navigate to root of project, then:

#### Build the application
```shell
$ ./gradlew clean build
```

#### Run the application

```
$ ./gradlew bootRun
```

## Clean Architecture

### Seperation of concerns

Here you will find how the different layers have each own responsibilty, and which corresponding class is represented in each layer.

![](/documentation/clean_architecture_layers.png)

![](/documentation/class_diagram.png)

## Use case

### Story

Register new Companies to an online platform.

***In order to*** register a new company
***As a*** site admin
***I need to*** be able to register new Companies 

### Rules

- company must have a name

### Scenario
 
- ***Given that*** the company has approved to be listed on the public online platform

- ***When I*** register the new arrived company with platform API

- ***Then I should*** have a verification that the registration has succedeed and

- ***And overall*** the new company is listed in the overview of 


## API Endpoints

`GET` _/api/v1/companies_

## Response body

HTTP 200 OK

```json
[
  {
    "name": "CompanyName",
    "website": "https://example.com",
    "missionStatement": "Lorum ipsum dolor...",
    "logo": "https://via.placeholder.com/50x50.png"
  },
  ...
]
```

`POST` _/api/v1/companies_

## Request body

```json
{
  "name": "CompanyName",
  "website": "https://example.com",
  "missionStatement": "Lorum ipsum dolor...",
  "logo": "https://via.placeholder.com/50x50.png"
}
```

## Response body

HTTP 201 CREATED

```json
{
    "id": "eb4cce9f-2055-444c-8a21-107c9c0cb410",
    "name": "CompanyName",
    "website": "https://example.com",
    "missionStatement": "Lorum ipsum dolor...",
    "logo": "https://via.placeholder.com/50x50.png"
    "registration": {
        "status": "ACCEPTED",
        "date": "2019-12-03 22:46:58"
    }
}
```

### TODO

- Setup dockerfile to host Mongo, [enable Injecting Mongo Gateway]()

[0]: https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html
[1]: https://openjdk.java.net
[2]:/documentation/clean_architecture_slides.pdf
[3]: https://docs.gradle.org/current/userguide/gradle_wrapper.html
[4]: https://github.com/srmds/clean-architecture-spring-kotlin/blob/master/src/main/kotlin/com/screaming/architecture/MainDriver.kt#L247

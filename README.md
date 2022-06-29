# A deep dive into Spring4Shell

## Requirements
 - Java 11 or higher
 - Docker

## Overview

- RCE vulnerability in the Spring Framework
- Leaked out ahead of CVE publication
- A CVE was added on March 31st, 2022 by the Spring developers
  as [CVE-2022-22965](https://tanzu.vmware.com/security/cve-2022-22965).

## Exploitation requirements

- JDK 9+
- Vulnerable version of the Spring Framework (<5.2 | 5.2.0-19 | 5.3.0-17)
- A dependency on the Spring Web MVC and/or Spring WebFlux (transitively affected from Spring Beans)
    - Spring MVC (Model-View-Controller) is part of the Spring Framework used to develop web applications following the
      MVC design pattern
    - Spring WebFlux used to build a non-blocking web stack in order to handle concurrency with a small number of
      threads and scale with fewer hardware resources
- Packaged as a WAR and deployed on a standalone Servlet container
    - Deployments using an embedded Servlet container or reactive web server which is the typical way to deploy spring
      boot applications are not affected
- Relates to data binding used to populate an object for controller method parameters that are annotated
  with `@ModelAttribute` or optionally without it, and without any other Spring Web annotation.
- The vulnerability does not relate to `@RequestBody` controller method parameters (e.g. JSON deserialization). However,
  such methods may still be vulnerable if they have another method parameter populated via data binder from query
  parameters.

## Demo

Spring boot: makes it easy to created stand-alone Spring based Applications.
It has an opinionated view of the Spring platform and third-party libraries so one can get started with minimum fuss.
Most Spring Boot applications need minimal Spring configuration.

- [DataBinder (Spring Framework 5.3.18 API)](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/validation/DataBinder.html)

> Note that there are potential security implications in failing to set an array of allowed fields. In the case of HTTP
> form POST data for example, malicious clients can attempt to subvert an application by supplying values for fields or
> properties that do not exist on the form. In some cases this could lead to illegal data being set on command objects
> or
> their nested objects. For this reason, it is highly recommended to specify the allowedFields property on the
> DataBinder.

- `AbstractNestablePropertyAccessor::getPropertyAccessorForPropertyPath`
- `BeanWrapperImpl::getLocalPropertyHandler`

## Rules of the game

- Only public properties following the JavaBeans naming conventions are exposed for data binding
- Source: `PropertyInfo::get`
    - Non-static public methods
    - Methods with `0` parameters:
        - return `boolean` and prefix `is`
        - return `type != void` and prefix `get`
    - Methods with `1` parameter:
        - return `type == void` and prefix `set`
        - return `type != void` and first parameter `type == int` and prefix `get`
    - Methods with `2` parameters:
        - return `type == void` and first parameter `type == int` and prefix `set`
- Add elements to `Arrays`, `Lists`, `Maps`

## Demo project

```shell
curl -X POST 'http://localhost:8080/bean' -d 'name=dreamtheater'
curl -X POST 'http://localhost:8080/bean' -d "list[0]=$(uuidgen)"
curl -X POST 'http://localhost:8080/bean' -d "complexObject.map[xxx]=$(uuidgen)"
curl 'http://localhost:8080/tree'
```

Java 9 introduces a new level of abstraction above packages, known as the Java Platform Module System (JPMS), or
“Modules” for short.
A Module is a group of closely related packages and resources along with a new module descriptor file.
In other words, it's a “package of Java Packages” abstraction that allows us to make our code even more reusable.

### Mitigation before java 9

- `CachedIntrospectionResults(Class<?> beanClass)`

```
for (PropertyDescriptor pd : pds) {
    if (Class.class == beanClass &&
        ("classLoader".equals(pd.getName()) || "protectionDomain".equals(pd.getName()))) {
        // Ignore Class.getClassLoader() and getProtectionDomain() methods - nobody needs to bind to those
        continue;
    }
}
```

This allows one to escape the object meant to be used for data binding and set other properties of the application.

```shell
curl -X POST 'http://localhost:8080/bean' -d 'class.module.classLoader.URLs[0]=https://example.com' 
curl -X POST 'http://localhost:8080/bean?class.module.classLoader.defaultAssertionStatus=true'
```

## Run as war

 ```shell
 docker rm -f rce; docker build -t rce:latest . && docker run -p 8000:8000 -p 8080:8080 --name rce rce:latest
 docker exec -it XXX /bin/bash
 curl 'http://localhost:8080/demo/tree'
 curl -v -X POST 'http://localhost:8080/demo/bean' -d 'class.module.classLoader.resources.context.parent.appBase=/usr/local/tomcat/webapps.dist'
 ```

## Tomcat Access Log Valve

- A Valve allows a class to act as a preprocessor of each request within a container
- https://tomcat.apache.org/tomcat-8.0-doc/config/valve.html#Access_Log_Valve
- The Access Log Valve creates log files in the same format as those created by standard web servers
- This Valve uses self-contained logic to write its log files
  The Access Log Valve supports the following configuration attributes:
- **pattern**: `%{xxx}i` write value of incoming header with name xxx
- **suffix**: The suffix added to the end of each log file's name
- **directory**: Absolute or relative pathname of a directory in which log files created by this valve will be placed
- **prefix**: The prefix added to the start of each log file's name

## Exploit

```shell
python3 poc.py
docker exec -it XXX /bin/bash
cat ROOT/tomcatwar.jsp
```

## Fix

- [Commit](https://github.com/spring-projects/spring-framework/commit/002546b3e4b8d791ea6acccb81eb3168f51abb15)

## Learnings

- Keep your dependencies updated.
- Use a dedicated model object for each data binding use case in order to avoid exposing methods that are not meant to
  be executed by the client (e.g. JPA or Hibernate entities)
- If one cannot use a dedicated model object for each data binding use case, one must limit the properties that are
  allowed for data binding. This can be achieved by setting the allowed fields pattern via the setAllowedFields() method
  on WebDataBinder.

```java

@RestController
public class MyController {

    @InitBinder
    void initBinder(final WebDataBinder binder) {
        binder.setAllowedFields("firstName", "lastName");
    }

    // @RequestMapping methods, etc.
}
```


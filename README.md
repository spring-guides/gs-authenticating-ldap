
This guide walks you through the process creating an application and securing it with the [Spring Security](http://static.springsource.org/spring-security/site/index.html) LDAP module.

What you'll build
-----------------

You'll build a simple web application that is secured by Spring Security's embedded Java-based LDAP server. You'll load the LDAP server with a data file containing a set of users.

What you'll need
----------------

 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 6][jdk] or later
 - [Gradle 1.7+][gradle] or [Maven 3.0+][mvn]

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[gradle]: http://www.gradle.org/
[mvn]: http://maven.apache.org/download.cgi

How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/guides/gs), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [Git][u-git]:
`git clone https://github.com/springframework-meta/gs-authenticating-ldap.git`
 - cd into `gs-authenticating-ldap/initial`.
 - Jump ahead to [Creating a simple web controller](#initial).

**When you're finished**, you can check your results against the code in `gs-authenticating-ldap/complete`.
[zip]: https://github.com/springframework-meta/gs-authenticating-ldap/archive/master.zip
[u-git]: /understanding/Git


<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Gradle](http://gradle.org) and [Maven](https://maven.apache.org) is included here. If you're not familiar with either, refer to [Building Java Projects with Gradle](/guides/gs/gradle/) or [Building Java Projects with Maven](/guides/gs/maven).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello

### Create a Gradle build file

`build.gradle`
```gradle
buildscript {
    repositories {
        maven { url "http://repo.springsource.org/libs-snapshot" }
        mavenLocal()
    }
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'idea'

jar {
    baseName = 'gs-authenticating-ldap'
    version =  '0.1.0'
}

repositories {
    mavenCentral()
    maven { url "http://repo.springsource.org/libs-snapshot" }
}

dependencies {
    compile("org.springframework.boot:spring-boot-starter-web:0.5.0.BUILD-SNAPSHOT")
    compile("org.springframework.boot:spring-boot-starter-security:0.5.0.BUILD-SNAPSHOT")
    compile("org.springframework.security:spring-security-ldap:3.1.3.RELEASE")
    compile("org.apache.directory.server:apacheds-server-jndi:1.5.5")
    testCompile("junit:junit:4.11")
}

task wrapper(type: Wrapper) {
    gradleVersion = '1.7'
}
```

This guide is using [Spring Boot's starter POMs](/guides/gs/spring-boot/).


<a name="initial"></a>
Create a simple web controller
--------------------------------
In Spring, REST endpoints are just Spring MVC controllers. The following Spring MVC controller handles a `GET /` request by returning a simple message:

`src/main/java/hello/HomeController.java`
```java
package hello;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @RequestMapping("/")
    public @ResponseBody String index() {
        return "Welcome to the home page!";
    }
}
```
    
The entire class is marked up with `@Controller` so Spring MVC can autodetect the controller using it's built-in scanning features and automatically configure web routes.

The method is tagged with `@RequestMapping` to flag the path and the REST action. In this case, `GET` is the default behavior; it returns a message indicating that you are on the home page. 

`@ResponseBody` tells Spring MVC to write the text directly into the HTTP response body, because there aren't any views. Instead, when you visit the page, you'll get a simple message in the browser as the focus of this guide is securing the page with LDAP.

Build the unsecured web application
-----------------------------------
Before you secure the web application, verify that it works. To do that, you need to define some key beans. To do that, create an `Application` class.

`src/main/java/hello/Application.java`
```java
package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan
@EnableWebMvc
@EnableAutoConfiguration
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```
    
### Build an executable JAR
Now that your `Application` class is ready, you simply instruct the build system to create a single, executable jar containing everything. This makes it easy to ship, version, and deploy the service as an application throughout the development lifecycle, across different environments, and so forth.

Add the following dependency to your Gradle **build.gradle** file's `buildscript` section:

```groovy
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:0.5.0.BUILD-SNAPSHOT")
    }
```

Further down inside `build.gradle`, add the following to the list of plugins:

```groovy
apply plugin: 'spring-boot'
```

The [Spring Boot gradle plugin][spring-boot-gradle-plugin] collects all the jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.
It also searches for the `public static void main()` method to flag as a runnable class.

Now run the following command to produce a single executable JAR file containing all necessary dependency classes and resources:

```sh
$ ./gradlew build
```

Now you can run the JAR by typing:

```sh
$ java -jar build/libs/gs-authenticating-ldap-0.1.0.jar
```

[spring-boot-gradle-plugin]: https://github.com/SpringSource/spring-boot/tree/master/spring-boot-tools/spring-boot-gradle-plugin

> **Note:** The procedure above will create a runnable JAR. You can also opt to [build a classic WAR file](/guides/gs/convert-jar-to-war/) instead.

Run the unsecured web application
-------------------
Run your unsecured web application at the command line:

```sh
$ ./gradlew clean build && java -jar build/libs/gs-authenticating-ldap-0.1.0.jar
```


If you open your browser and visit <http://localhost:8080>, you should see the following plain text:

```
Welcome to the home page!
```

Set up Spring Security
----------------------------
To configure Spring Security, you need an XML application context file, **application-context.xml**. To make the file easier to read, it is configured by default to use the `security` namespace.

`src/main/resources/application-context.xml`
```xml
<beans:beans xmlns="http://www.springframework.org/schema/security"
  xmlns:beans="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.springframework.org/schema/beans 
           http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
           http://www.springframework.org/schema/security
           http://www.springframework.org/schema/security/spring-security-3.2.xsd">
     
    <ldap-server root="dc=springframework,dc=org" ldif="classpath:test-server.ldif" />

    <authentication-manager>
        <ldap-authentication-provider user-dn-pattern="uid={0},ou=people" 
                                                group-search-base="ou=groups"/>
    </authentication-manager>

    <http auto-config="true">
        <intercept-url pattern="/**" access="ROLE_DEVELOPERS" />
    </http> 

</beans:beans>
```

> **Note:** At the time of this writing, a Java-based version of Spring Security setup is still under development. That's why for now, this guide is using XML to configure the security parts.

You also need an LDAP server. Spring Security's LDAP module includes an embedded server written in pure Java, which is being used for this guide. You can eventually replace the embedded LDAP server with a production-ready server by editing **application-context.xml** like this:

```xml
<ldap-server url="ldap://<your production server>:389/dc=springframework,dc=org" />
```

Next, you declare `<authentication-manager>` as the component that handles all authentication requests. In this setup, it contains an `<ldap-authentication-provider>`. It's configured to take a username, and insert it into `{0}` and look for `uid={0},ou=people,dc=springframework,dc=org`

The `<http>` component declares a set of URL intercepts and other automatic components such as form authentication.

Using an XML configuration inside a pure Java configuration
-----------------------------------------------------------
The Spring Security beans are defined in XML format inside **application-context.xml**, but your application is launched from a pure Java `Application` class. To combine them, edit your `Application` class like this:

`src/main/java/hello/Application.java`
```java
package hello;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ImportResource("classpath:application-context.xml")
@ComponentScan
@EnableWebMvc
@EnableAutoConfiguration
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```

`@ImportResource` pulls **application-context.xml** into the [application context][u-application-context] configuration.

Set up user data
--------------------

LDAP servers can use LDIF (LDAP Data Interchange Format) files to exchange user data. `<ldap-server>` inside **application-context.xml** is configured to pull in an LDIF data file using the **ldif** parameter. This makes it easy to pre-load demonstration data.

`src/main/resources/test-server.ldif`
```ldif
dn: ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: groups

dn: ou=subgroups,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: subgroups

dn: ou=people,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: people

dn: ou=space cadets,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: space cadets

dn: ou=\"quoted people\",dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: "quoted people"

dn: ou=otherpeople,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: otherpeople

dn: uid=ben,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Ben Alex
sn: Alex
uid: ben
userPassword: {SHA}nFCebWjxfaLbHHG1Qk5UU4trbvQ=

dn: uid=bob,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Bob Hamilton
sn: Hamilton
uid: bob
userPassword: bobspassword

dn: uid=joe,ou=otherpeople,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Joe Smeth
sn: Smeth
uid: joe
userPassword: joespassword

dn: cn=mouse\, jerry,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Mouse, Jerry
sn: Mouse
uid: jerry
userPassword: jerryspassword

dn: cn=slash/guy,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: slash/guy
sn: Slash
uid: slashguy
userPassword: slashguyspassword

dn: cn=quote\"guy,ou=\"quoted people\",dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: quote\"guy
sn: Quote
uid: quoteguy
userPassword: quoteguyspassword

dn: uid=space cadet,ou=space cadets,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Space Cadet
sn: Cadet
uid: space cadet
userPassword: spacecadetspassword



dn: cn=developers,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: groupOfNames
cn: developers
ou: developer
uniqueMember: uid=ben,ou=people,dc=springframework,dc=org
uniqueMember: uid=bob,ou=people,dc=springframework,dc=org

dn: cn=managers,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: groupOfNames
cn: managers
ou: manager
uniqueMember: uid=ben,ou=people,dc=springframework,dc=org
uniqueMember: cn=mouse\, jerry,ou=people,dc=springframework,dc=org

dn: cn=submanagers,ou=subgroups,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: groupOfNames
cn: submanagers
ou: submanager
uniqueMember: uid=ben,ou=people,dc=springframework,dc=org
```
    
> **Note:** Using an LDIF file isn't standard configuration for a production system. However, it's very useful for testing purposes or guides.

Build and run the secured web application
-----------------------------------------
With Spring Security setup, you can now run the application in secured mode:

```sh
$ ./gradlew clean build && java -jar build/libs/gs-authenticating-ldap-0.1.0.jar
```

If you visit the site at <http://localhost:8080>, you should be redirected to a login page provided by Spring Security.

Enter username **ben** and password **benspassword**. You should see this message in your browser:

```
Welcome to the home page!
```

Summary
-------
Congratulations! You have just written a web application and secured it with [Spring Security](http://static.springsource.org/spring-security/site/docs/3.2.x/reference/springsecurity-single.html). In this case, you used an [LDAP-based user store](http://static.springsource.org/spring-security/site/docs/3.2.x/reference/springsecurity-single.html#ldap).

[u-application-context]: /understanding/application-context


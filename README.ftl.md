<#assign project_id="gs-authenticating-ldap">

This guide walks you through the process creating an application and securing it with the [Spring Security](http://static.springsource.org/spring-security/site/index.html) LDAP module.

What you'll build
-----------------

You'll build a simple web application that is secured by Spring Security's embedded Java-based LDAP server. You'll load the LDAP server with a data file containing a set of users.

What you'll need
----------------

 - About 15 minutes
 - <@prereq_editor_jdk_buildtools/>

## <@how_to_complete_this_guide jump_ahead='Creating a simple web controller'/>


<a name="scratch"></a>
Set up the project
------------------

<@build_system_intro/>

<@create_directory_structure_hello/>

### Create a Gradle build file

    <@snippet path="build.gradle" prefix="initial"/>

<@bootstrap_starter_pom_disclaimer/>


<a name="initial"></a>
Create a simple web controller
--------------------------------
In Spring, REST endpoints are just Spring MVC controllers. The following Spring MVC controller handles a `GET /` request by returning a simple message:

    <@snippet path="src/main/java/hello/HomeController.java" prefix="complete"/>
    
The entire class is marked up with `@Controller` so Spring MVC can autodetect the controller using it's built-in scanning features and automatically configure web routes.

The method is tagged with `@RequestMapping` to flag the path and the REST action. In this case, `GET` is the default behavior; it returns a message indicating that you are on the home page. 

`@ResponseBody` tells Spring MVC to write the text directly into the HTTP response body, because there aren't any views. Instead, when you visit the page, you'll get a simple message in the browser as the focus of this guide is securing the page with LDAP.

Build the unsecured web application
-----------------------------------
Before you secure the web application, verify that it works. To do that, you need to define some key beans. To do that, create an `Application` class.

    <@snippet path="src/main/java/hello/Application.java" prefix="initial"/>
    
<@build_an_executable_jar_subhead/>
<@build_an_executable_jar_with_gradle/>

<@run_the_application_with_gradle module="unsecured web application"/>

If you open your browser and visit <http://localhost:8080>, you should see the following plain text:

```
Welcome to the home page!
```

Set up Spring Security
----------------------------
To configure Spring Security, you need an XML application context file, **application-context.xml**. To make the file easier to read, it is configured by default to use the `security` namespace.

    <@snippet path="src/main/resources/application-context.xml" prefix="complete"/>

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

    <@snippet path="src/main/java/hello/Application.java" prefix="complete"/>

`@ImportResource` pulls **application-context.xml** into the [application context][u-application-context] configuration.

Set up user data
--------------------

LDAP servers can use LDIF (LDAP Data Interchange Format) files to exchange user data. `<ldap-server>` inside **application-context.xml** is configured to pull in an LDIF data file using the **ldif** parameter. This makes it easy to pre-load demonstration data.

    <@snippet path="src/main/resources/test-server.ldif" prefix="complete"/>
    
> **Note:** Using an LDIF file isn't standard configuration for a production system. However, it's very useful for testing purposes or guides.

Build and run the secured web application
-----------------------------------------
With Spring Security setup, you can now run the application in secured mode:

```sh
$ ./gradlew clean build && java -jar build/libs/${project_id}-0.1.0.jar
```

If you visit the site at <http://localhost:8080>, you should be redirected to a login page provided by Spring Security.

Enter username **ben** and password **benspassword**. You should see this message in your browser:

```
Welcome to the home page!
```

Summary
-------
Congratulations! You have just written a web application and secured it with [Spring Security](http://static.springsource.org/spring-security/site/docs/3.2.x/reference/springsecurity-single.html). In this case, you used an [LDAP-based user store](http://static.springsource.org/spring-security/site/docs/3.2.x/reference/springsecurity-single.html#ldap).

<@u_application_context/>


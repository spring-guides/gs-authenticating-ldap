<#assign project_id="gs-authenticating-ldap">
This guide walks you through the process creating an application and securing it with the [Spring Security](http://static.springsource.org/spring-security/site/index.html) LDAP module.

What you'll build
-----------------

You'll build a simple web application that is secured by Spring Security's embedded Java-based LDAP server loaded with a fixed data file. But it's adaptable to a production LDAP server.

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

### Create a Maven POM

    <@snippet path="pom.xml" prefix="initial"/>

<@bootstrap_starter_pom_disclaimer/>


<a name="initial"></a>
Create a simple web controller
--------------------------------
In Spring, REST endpoints are just Spring MVC controllers. The following Spring MVC controller handles a `GET /` request by returning a simple message:

    <@snippet path="src/main/java/hello/HomeController.java" prefix="complete"/>
    
The entire class is marked up with `@Controller` so Spring MVC can pick it up and look for routes.

The method is tagged with `@RequestMapping` to flag the path and the REST action. In this case, `GET` is the default behavior; it returns a message indicating that you are on the home page. 

`@ResponseBody` tells Spring MVC to write the text directly into the HTTP response body, because there aren't any views. Instead, when you visit the page, you'll get a simple message in the browser as the focus of this guide is securing the page with LDAP.

Run the unsecured web application
-------------------------------------
Before you secure the web application, verify that it works. To do that, you wire up a web application configuration.

    <@snippet path="src/main/java/hello/Application.java" prefix="initial"/>

Using Spring Bootstrap, that's enough to launch the unsecured web application.

    mvn package && java -jar target/${project_id}-complete-0.1.0.jar

Open a browser and visit <http://localhost:8080/>, and you should see a simple message.

```
Welcome to the home page!
```

Set up Spring Security
----------------------------
To configure Spring Security, you need an XML application context file, **application-context.xml**. To make the file easier to read, it is configured by default to use the `security` namespace.

    <@snippet path="src/main/resources/application-context.xml" prefix="complete"/>

> **Note:** At the time of this writing, a Java-based version of Spring Security setup is still under development.

You also need an LDAP server. Spring Security's LDAP module includes an embedded server written in pure Java. If you eventually replace the embedded LDAP server with a production-ready server, you can make a one-line change to point to it.

Next, you declare `<authentication-manager>` as the component that handles all authentication requests. In this setup, it contains an `<ldap-authentication-provider>`. It's configured to take a username, and insert it into `{0}` and look for `uid={0},ou=people,dc=springframework,dc=org`

The `<http>` component declares a set of URL intercepts and other automatic components such as form authentication.

Wire an XML application context into the Java configuration
-------------------------------------------------------------
To wire in Spring Security, update the configuration class and pull in the XML configuration you just defined.

    <@snippet path="src/main/java/hello/Application.java" prefix="complete"/>

`@ImportResource` pulls **application-context.xml** into this application context configuration.

Set up user data
--------------------

LDAP servers can use LDIF (LDAP Data Interchange Format) files to exchange user data. We have it configured to pull in a file using the **ldif** parameter of the `<ldap-server>` element in **application-context.xml**.

    <@snippet path="src/main/resources/test-server.ldif" prefix="complete"/>
    
> **Note:** Using an LDIF file isn't standard configuration for a production system. However, it's very useful for testing purposes or guides.

<@build_an_executable_jar_mainhead/>
<@build_an_executable_jar/>

Build and run the secured web application
------------------------------------------------
With Spring Security wired in, you can now run the application in secured mode:

    mvn package && java -jar target/${project_id}-complete-0.1.0.jar

If you visit the site at <http://localhost:8080>, you should be redirected to a login page provided by Spring Security.

Enter username **ben** and password **benspassword**. You should see this message in your browser:

```
Welcome to the home page!
```

Summary
-------
Congratulations! You have just written a web application and secured it with [Spring Security](http://static.springsource.org/spring-security/site/docs/3.2.x/reference/springsecurity-single.html). In this case, you used an [LDAP-based user store](http://static.springsource.org/spring-security/site/docs/3.2.x/reference/springsecurity-single.html#ldap).


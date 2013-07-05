<#assign project_id="gs-authenticating-ldap">

Getting Started: Authenticating a user with LDAP
================================================

What you'll build
-----------------

This Getting Started guide will walk you through the process configuring an application to be secured by an LDAP server.

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

    <@snippet path="pom.xml" prefix="complete"/>

<@bootstrap_starter_pom_disclaimer/>


<a name="initial"></a>
Creating a simple web controller
--------------------------------
In Spring, REST endpoints are just Spring MVC controllers. The following Spring MVC controller handles a `GET /` request by returning a simple message:

    <@snippet path="src/main/java/hello/HomeController.java" prefix="complete"/>
    
First of all, this entire class is marked up with `@Controller` so Spring MVC can pick it up and look for routes.

Next, the method has been tagged with `@RequestMapping` to flag the path and the REST action. In this case, `GET` is the default behavior, it will return back a very simple message indicating you are on the home page. 

Finally, `@ResponseBody` tells Spring MVC to write the text directly into the HTTP response body. That's because there aren't any views. Instead, when you visit the page, you'll get a very simple message in the browser. That's because the focus in this guide is on securing the page with LDAP.

Running an unsecured web application
-------------------------------------
Before you secure the web application, it's best to verify it works first. To do that, we need to wire up a web application configuration.

    <@snippet path="src/main/java/hello/Application.java" prefix="initial"/>

Using Spring Bootstrap, that's enough to launch our unsecured web application.

    mvn package && java -jar target/${project_id}-complete-0.1.0.jar

Open a browser and visit <http://localhost:8080/>, and you should see a simple message.

```
Welcome to the home page!
```

Setting up Spring Security
----------------------------
To configure Spring Security, you need an XML application context file. Let's create **application-context.xml**. To make it easier to read, it has been configured by default to use the `security` namespace.

    <@snippet path="src/main/resources/application-context.xml" prefix="complete"/>

> **Note:** Unfortunately, at the time of writing, a Java-based version of Spring Security setup is still under development.

First of all, we need an LDAP server. While we could install a full-blown directory server to provide this, Spring Security's LDAP module includes an embedded one written in pure Java. It we eventually replace the embedded LDAP server with a real one, it's a one-line change to point to the real one.

Next, we need to declare `<authentication-manager>` as the component that will handle all authentication requests. In this setup, it contains an `<ldap-authentication-provider>`. It's configured to take a username, and insert it into `{0}` and look for `uid={0},ou=people,dc=springframework,dc=org`

Finally, we need the `<http>` component to declare a set of URL intercepts as well as some other automatic components such as form authentication.

Wiring an XML application context into our Java configuration
-------------------------------------------------------------
To wire in Spring Security, we need to update our configuration class and pull in the XML configuration we just defined.

    <@snippet path="src/main/java/hello/Application.java" prefix="complete"/>

`@ImportResource` pulls **application-context.xml** into this application context configuration.

Setting up user data
--------------------

LDAP servers can exchange user data using LDIF files. We have it configured to pull in a file using the **ldif** parameter of the `<ldap-server>` element in **application-context.xml**.

    <@snippet path="src/main/resources/test-server.ldif" prefix="complete"/>
    
> **Note:** Using an LDIF file isn't standard configuration for a production system. However, it's very useful for testing purposes or guides.

## <@build_an_executable_jar/>

Building and Running the Secured Web Application
------------------------------------------------
With Spring Security wired in, you can now run it in secured mode:

    mvn package && java -jar target/${project_id}-complete-0.1.0.jar

Great! If you visit the site at <http://localhost:8080>, you should get redirected to a login page provided by Spring Security.

Enter username **ben** and password **benspassword**. It should then let you in to see a very simple message in your browser.

```
Welcome to the home page!
```

Summary
-------
Congratulations! You have just written a web application and secured it with [Spring Security](http://static.springsource.org/spring-security/site/docs/3.2.x/reference/springsecurity-single.html). In this case, you used an [LDAP-based user store](http://static.springsource.org/spring-security/site/docs/3.2.x/reference/springsecurity-single.html#ldap).


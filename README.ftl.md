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


<@create_both_builds/>

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
<@build_an_executable_jar_with_both/>

<@run_the_application_with_both module="unsecured web application"/>

If you open your browser and visit <http://localhost:8080>, you should see the following plain text:

```
Welcome to the home page!
```

Set up Spring Security
----------------------------
To configure Spring Security, you can use pure Java to configure things properly.

    <@snippet path="src/main/java/hello/WebSecurityConfig.java" prefix="complete"/>
    
The `@EnableWebSecurity` turns on a variety of beans needed to use Spring Security.

You also need an LDAP server. Spring Security's LDAP module includes an embedded server written in pure Java, which is being used for this guide. The `ldapAuthentication()` method configures things where the username at the login form is plugged into `{0}` such that it searches `uid={0},ou=people,dc=springframework,dc=org` in the LDAP server.

Set up user data
--------------------
LDAP servers can use LDIF (LDAP Data Interchange Format) files to exchange user data. The `ldif()` method inside `WebSecurityConfig` pulls in an LDIF data file. This makes it easy to pre-load demonstration data.

    <@snippet path="src/main/resources/test-server.ldif" prefix="complete"/>
    
> **Note:** Using an LDIF file isn't standard configuration for a production system. However, it's very useful for testing purposes or guides.


Create an Application class
---------------------------

    <@snippet path="src/main/java/hello/Application.java" prefix="complete"/>

The `main()` method defers to the [`SpringApplication`][] helper class, providing `Application.class` as an argument to its `run()` method. This tells Spring to read the annotation metadata from `Application` and to manage it as a component in the [Spring application context][u-application-context].

The `@ComponentScan` annotation tells Spring to search recursively through the `hello` package and its children for classes marked directly or indirectly with Spring's [`@Component`][] annotation. This directive ensures that Spring finds and registers the `WebSecurityConfig` class, because it is marked with `@Configuration`, which in turn is a kind of `@Component` annotation.

The [`@EnableAsync`][] annotation switches on Spring's ability to run `@Async` methods in a background thread pool.

The [`@EnableAutoConfiguration`][] annotation switches on reasonable default behaviors based on the content of your classpath. For example, it looks for any class that implements the `CommandLineRunner` interface and invokes its `run()` method. In this case, it runs the demo code for this guide.

<@build_an_executable_jar_subhead/>
<@build_an_executable_jar_with_both/>

<@run_the_application_with_both module="secured web application"/>

If you visit the site at [http://localhost:8080](http://localhost:8080), you should be redirected to a login page provided by Spring Security.

Enter username **ben** and password **benspassword**. You should see this message in your browser:

```
Welcome to the home page!
```

Summary
-------
Congratulations! You have just written a web application and secured it with [Spring Security](http://static.springsource.org/spring-security/site/docs/3.2.x/reference/springsecurity-single.html). In this case, you used an [LDAP-based user store](http://static.springsource.org/spring-security/site/docs/3.2.x/reference/springsecurity-single.html#ldap).

<@u_application_context/>
[`SpringApplication`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/SpringApplication.html
[`@EnableAutoConfiguration`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/context/annotation/SpringApplication.html
[`@Component`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/stereotype/Component.html


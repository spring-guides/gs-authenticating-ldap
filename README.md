Getting Started: Authenticating a user with LDAP
================================================

This Getting Started guide will walk you through the process of creating a server that can receive multi-part file uploads as well as building a client to upload a file.

To help you get started, we've provided an initial project structure as well as the completed project for you in GitHub:

```sh
$ git clone https://github.com/springframework-meta/gs-authenticating-ldap.git
```

In the `start` folder, you'll find a bare project, ready for you to copy-n-paste code snippets from this document. In the `complete` folder, you'll find the complete project code.

Before we can lock down our application with an LDAP server, there's some initial project setup that's required. Or, you can skip straight to the [fun part](#setting-up-spring-security).

Selecting Dependencies
----------------------
The sample in this Getting Started Guide will leverage Spring MVC, Spring Security, and Jetty's embedded servlet container. Therefore, the following library dependencies are needed in the project's build configuration:

- org.springframework.security:spring-security-web:3.1.3.RELEASE
- org.springframework.security:spring-security-ldap:3.1.3.RELEASE
- org.springframework.security:spring-security-config:3.1.3.RELEASE
- org.springframework:spring-context:3.2.2.RELEASE
- org.springframework:spring-webmvc:3.2.2.RELEASE
- org.springframework:spring-tx:3.2.2.RELEASE
- org.eclipse.jetty:jetty-server:8.1.10.v20130312
- org.eclipse.jetty:jetty-servlet:8.1.10.v20130312
- org.eclipse.jetty:jetty-annotations:8.1.10.v20130312
- org.apache.directory.server:apacheds-core:1.5.5
- org.apache.directory.server:apacheds-core-entry:1.5.5
- org.apache.directory.server:apacheds-protocol-shared:1.5.5
- org.apache.directory.server:apacheds-protocol-ldap:1.5.5
- org.apache.directory.server:apacheds-server-jndi:1.5.5
- org.apache.directory.shared:shared-ldap:0.9.15
- org.slf4j:slf4j-log4j12:1.7.5"

	
Refer to the [Gradle Getting Started Guide]() or the [Maven Getting Started Guide]() for details on how to include these dependencies in your build.

Setting up a Jetty Server
-------------------------
We need a servlet container to run our web app. For this, we'll use embedded Jetty.

```java
package ldapauthentication;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.build.Configuration;
import org.eclipse.jetty.build.WebAppContext;

public class ServletContext {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setConfigurations(new Configuration[]{ new SpringAppInitializingConfiguration()});

        server.setHandler(webAppContext);
        server.start();
        server.join();
    }
    
}
```

It's setup to run on port 8080 and will have a path of `/`. For configuration purposes, it's delegating everything else to `SpringAppInitializingConfiguration`. This key component searches for classes marked up with `@HandlerTypes`, a servlet 3.0 annotation attached to any WebInitializer classes.

```java
package ldapauthentication;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.annotations.AbstractDiscoverableAnnotationHandler;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.annotations.AnnotationDecorator;
import org.eclipse.jetty.annotations.AnnotationParser;
import org.eclipse.jetty.annotations.ClassNameResolver;
import org.eclipse.jetty.annotations.WebFilterAnnotationHandler;
import org.eclipse.jetty.annotations.WebListenerAnnotationHandler;
import org.eclipse.jetty.annotations.WebServletAnnotationHandler;
import org.eclipse.jetty.annotations.AnnotationParser.DiscoverableAnnotationHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.build.WebAppContext;

/**
 * This solution was drawn from http://stackoverflow.com/questions/13222071/spring-3-1-buildlicationinitializer-embedded-jetty-8-annotationconfiguration
 */
public class SpringAppInitializingConfiguration extends AnnotationConfiguration {
	
	@Override
	public void configure(WebAppContext context) throws Exception {
		boolean metadataComplete = context.getMetaData().isMetaDataComplete();
		context.addDecorator(new AnnotationDecorator(context));

		AnnotationParser parser = null;
		if (!metadataComplete) {
			if (context.getServletContext().getEffectiveMajorVersion() >= 3 || context.isConfigurationDiscovered()) {
				_discoverableAnnotationHandlers.add(new WebServletAnnotationHandler(context));
				_discoverableAnnotationHandlers.add(new WebFilterAnnotationHandler(context));
				_discoverableAnnotationHandlers.add(new WebListenerAnnotationHandler(context));
			}
		}

		createServletContainerInitializerAnnotationHandlers(context, getNonExcludedInitializers(context));

		if (!_discoverableAnnotationHandlers.isEmpty() || _classInheritanceHandler != null || !_containerInitializerAnnotationHandlers.isEmpty()) {
			parser = new AnnotationParser() {

				@Override
				public void parse(Resource dir, ClassNameResolver resolver) throws Exception {
					if (dir.isDirectory()) {
						super.parse(dir, resolver);
					} else {
						super.parse(dir.getURI(), resolver);
					}
				}

			};

			parse(context, parser);

			for (DiscoverableAnnotationHandler handler: _discoverableAnnotationHandlers) {
				context.getMetaData().addDiscoveredAnnotations(((AbstractDiscoverableAnnotationHandler)handler).getAnnotationList());
			}

		}
	}

	final private void parse(final WebAppContext context, AnnotationParser parser) throws Exception {
		List<Resource> resources = getResources(getClass().getClassLoader());
		for (Resource resource : resources) {
			if (resource == null) {
				return;
			}
			parser.clearHandlers();
			for (DiscoverableAnnotationHandler handler : _discoverableAnnotationHandlers) {
				if (handler instanceof AbstractDiscoverableAnnotationHandler) {
					((AbstractDiscoverableAnnotationHandler)handler).setResource(null);
				}
			}
			parser.registerHandlers(_discoverableAnnotationHandlers);
			parser.registerHandler(_classInheritanceHandler);
			parser.registerHandlers(_containerInitializerAnnotationHandlers);

			parser.parse(resource, new ClassNameResolver() {
				@Override
				public boolean isExcluded(String name) {
					if (context.isSystemClass(name)) return true;
					if (context.isServerClass(name)) return false;
					return false;
				}

				@Override
				public boolean shouldOverride(String name) {
					if (context.isParentLoaderPriority()) {
						return false;
					}
					return true;
				}
			});
		}
	}

	@SuppressWarnings("serial")
	final private List<Resource> getResources(final ClassLoader classLoader) throws IOException {
		if (classLoader instanceof URLClassLoader) {
			return new ArrayList<Resource>() {{
				for (URL url : ((URLClassLoader)classLoader).getURLs()) {
					add(Resource.newResource(url));
				}
			}};
		}
		return Collections.emptyList();
	}
}
```

With this component in place, we can now define our web application.

Declaring a web application
---------------------------

With servlet 3.0, we can now wire up all the components with pure Java code and not have to create or manage a web.xml file. The following code is automatically picked up by the servlet container.

```java
package ldapauthentication;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return null;
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[] { WebConfig.class } ;
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

}
```

In other guides, you may have seen something similar to `WebAppInitializer`. In this guide, we need the Spring MVC parts declared as part of the servlet configuration in `WebConfig`.

Configuring the components
------------------------------
First, we need to enable the Spring MVC parts.

```java
package ldapauthentication;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan
@EnableWebMvc
public class WebConfig {

}
```

`@EnableWebMvc` turns on the Spring MVC annotations and other components needed to create Spring MVC parts. `@ComponentScan` will find the controllers using annotations, but must explicitly avoid finding any other classes tagged with `@Configuration` to avoid loading them twice.

Creating a simple web controller
--------------------------------
In Spring, REST endpoints are just Spring MVC controllers. The following Spring MVC controller handles a `GET /upload` request by returning a simple message:

```java
package ldapauthentication;

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

First of all, this entire class is marked up with `@Controller` so Spring MVC can pick it up and look for routes.

Next, the method has been tagged with `@RequestMapping` to flag the path and the REST action. In this case, `GET` will return back a very simple message indicating we are at the home page. 

Finally, `@ResponseBody` tells Spring MVC to write the text directly into the HTTP response body. That's because we aren't using any views nor are we generating any HTML. Instead, when we visit the page, we'll get a very simple message in the browser. That's because our focus in this guide is on securing the page with LDAP.

Running our unsecured web application
-------------------------------------
Before we launch our application, we need to configure some log settings. Apache DS (directory server) is geared to use log4j, so setting up a log4j.properties file will serve us best.

```txt
# Set root logger level to DEBUG and its only appender to A1.
log4j.rootLogger=WARN, A1

# A1 is set to be a ConsoleAppender.
log4j.appender.A1=org.apache.log4j.ConsoleAppender

# A1 uses PatternLayout.
log4j.appender.A1.layout=org.apache.log4j.PatternLayout
log4j.appender.A1.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n

log4j.category.org.springframework.security=INFO
#log4j.category.org.apache.directory=DEBUG
#log4j.category.org.springframework.security.ldap=DEBUG
#log4j.category.org.apache.directory.server.protocol.shared.store=DEBUG
```

In this case, we are setting everything to WARN, and only exposing org.springframework.security things at the INFO level. It's possible to expose some of the other operations to see more of what's happening.


```sh
$ ./gradlew run
```

This should create an output like this:

```sh
:compileJava
:processResources
:classes
:run
0    [main] INFO  org.springframework.web.servlet.DispatcherServlet  - FrameworkServlet 'dispatcher': initialization started
17   [main] INFO  org.springframework.web.context.support.AnnotationConfigWebApplicationContext  - Refreshing WebApplicationContext for namespace 'dispatcher-servlet': startup date [Mon Apr 29 09:24:22 CDT 2013]; root of context hierarchy
71   [main] INFO  org.springframework.context.annotation.ClassPathBeanDefinitionScanner  - JSR-250 'javax.annotation.ManagedBean' found and supported for component scanning
72   [main] INFO  org.springframework.web.context.support.AnnotationConfigWebApplicationContext  - Registering annotated classes: [class ldapauthentication.WebConfig]
148  [main] INFO  org.springframework.context.annotation.ClassPathBeanDefinitionScanner  - JSR-250 'javax.annotation.ManagedBean' found and supported for component scanning
360  [main] INFO  org.springframework.beans.factory.support.DefaultListableBeanFactory  - Pre-instantiating singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@13328d64: defining beans [org.springframework.context.annotation.internalConfigurationAnnotationProcessor,org.springframework.context.annotation.internalAutowiredAnnotationProcessor,org.springframework.context.annotation.internalRequiredAnnotationProcessor,org.springframework.context.annotation.internalCommonAnnotationProcessor,webConfig,org.springframework.context.annotation.ConfigurationClassPostProcessor.importAwareProcessor,homeController,org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration,requestMappingHandlerMapping,mvcContentNegotiationManager,viewControllerHandlerMapping,beanNameHandlerMapping,resourceHandlerMapping,defaultServletHandlerMapping,requestMappingHandlerAdapter,mvcConversionService,mvcValidator,httpRequestHandlerAdapter,simpleControllerHandlerAdapter,handlerExceptionResolver]; root of factory hierarchy
481  [main] INFO  org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping  - Mapped "{[/],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}" onto public java.lang.String ldapauthentication.HomeController.index()
770  [main] INFO  org.springframework.web.servlet.DispatcherServlet  - FrameworkServlet 'dispatcher': initialization completed in 769 ms
```

If we open a browsser and visit <http://localhost:8080/>, we should see our simple message.

```
Welcome to the home page!
```


Setting up Spring Security
----------------------------
To configure Spring Security, we need an XML application context file. Let's create **application-context.xml**. To make it easier to read, it has been configured by default to use the `security` namespace.

```xml
<beans:beans xmlns="http://www.springframework.org/schema/security"
  xmlns:beans="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.springframework.org/schema/beans 
           http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
           http://www.springframework.org/schema/security
           http://www.springframework.org/schema/security/spring-security-3.1.xsd">
     
	<ldap-server root="dc=springframework,dc=org" />

	<authentication-manager>
		<ldap-authentication-provider user-dn-pattern="uid={0},ou=people" group-search-base="ou=groups"/>
	</authentication-manager>

	<http auto-config="true">
		<intercept-url pattern="/**" access="ROLE_DEVELOPERS" />
	</http>	

</beans:beans>
```

> Unfortunately, at the time of writing, a Java-based version of Spring Security setup isn't available without writing a lot of detailed code. There are active plans to make an equivalent to the above code available in pure Java.

First of all, we need an LDAP server. While we could install a full-blown directory server to provide this, Spring Security's LDAP module includes an embedded one written in pure Java. It we eventually replace the embedded LDAP server with a real one, it's a one-line change to point to the real one.

Next, we need to declare `<authentication-manager>` as the component that will handle all authentication requests. In this setup, it contains an `<ldap-authentication-provider>`. It's configured to take a username, and insert it into `{0}` and look for `uid={0},ou=people,dc=springframework,dc=org`

Finally, we need the `<http>` component to declare a set of URL intercepts as well as some other automatic components such as form authentication.

Wiring an XML application context into our Java configuration
-------------------------------------------------------------
Spring Security components don't work if they are in the web application context. Instead, they must be in the parent application context. To do that, we need another configuration class.

```java
package ldapauthentication;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:application-context.xml")
public class RootConfig {
	
}
```

`@ImportResource` pulls **application-context.xml** into this application context configuration.

Adding a parent application context to our web application
----------------------------------------------------------

Previously, `WebAppInitializer` returned `null` from `getRootConfigClasses`. This where we need to have it return `RootConfig` in order to create a parent application context.

```java
package ldapauthentication;

import javax.servlet.Filter;

import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[] { RootConfig.class };
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[] { WebConfig.class } ;
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

	@Override
	protected Filter[] getServletFilters() {
		DelegatingFilterProxy filterChainProxy = new DelegatingFilterProxy();
		filterChainProxy.setTargetBeanName("springSecurityFilterChain");
		return new Filter[] { filterChainProxy };
	}

}
```

We also added a servlet filter, `DelegatingFilterProxy`, to connect to some Spring Security filters. The Spring Security filters integrate with the security components from our **application-context.xml** by using the name `springSecurityFilterChain`. That is a default name that was part of the **auto-config** setting we used.

Finally, we need to avoid loading `RootConfig` twice. `WebConfig` is configured to auto-scan and will find it, causing it to get loaded twice when the servlet is started. To avoid this, we must alter `WebConfig` so that it doesn't load any other `@Configuration` classes.

```java
package ldapauthentication;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan(excludeFilters = { @ComponentScan.Filter( Configuration.class ) })
@EnableWebMvc
public class WebConfig {

}
```

Setting up user data
--------------------

LDAP servers can exchange user data using LDIF files. Spring Security's `<ldap-server>` embedded server will look for any LDIF files on the path and auto-load them. Let's put some user data in **test-server.ldif**.

```text
dn: ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: groups

dn: ou=people,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: people

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

dn: cn=submanagers,ou=subgroups,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: groupOfNames
cn: submanagers
ou: submanager
uniqueMember: uid=ben,ou=people,dc=springframework,dc=org
```

Building and Running the Secured Web Application
------------------------------------------------
With Spring Security wired in, let's adjust our log settings.

```txt
# Set root logger level to DEBUG and its only appender to A1.
log4j.rootLogger=WARN, A1

# A1 is set to be a ConsoleAppender.
log4j.appender.A1=org.apache.log4j.ConsoleAppender

# A1 uses PatternLayout.
log4j.appender.A1.layout=org.apache.log4j.PatternLayout
log4j.appender.A1.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n

log4j.category.org.springframework.security=INFO
#log4j.category.org.apache.directory=DEBUG
#log4j.category.org.springframework.security.ldap=DEBUG
#log4j.category.org.apache.directory.server.protocol.shared.store=DEBUG
```

With everything in place, let's launch our secured web application!

```sh
./gradlew run
```

It should produce some output like this:

```sh
0    [main] INFO  org.springframework.security.core.SpringSecurityCoreVersion  - You are running with Spring Security Core 3.1.3.RELEASE
0    [main] INFO  org.springframework.security.config.SecurityNamespaceHandler  - Spring Security 'config' module version is 3.1.3.RELEASE
12   [main] INFO  org.springframework.security.config.ldap.LdapServerBeanDefinitionParser  - Embedded LDAP server bean definition created for URL: ldap://127.0.0.1:33389/dc=springframework,dc=org
42   [main] INFO  org.springframework.security.config.http.AuthenticationConfigBuilder  - No login page configured. The default internal one will be used. Use the 'login-page' attribute to set the URL of the login page.
46   [main] INFO  org.springframework.security.config.http.HttpSecurityBeanDefinitionParser  - Checking sorted filter chain: [Root bean: class [org.springframework.security.web.context.SecurityContextPersistenceFilter]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null, order = 200, Root bean: class [org.springframework.security.web.authentication.logout.LogoutFilter]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null, order = 400, <org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter#0>, order = 800, Root bean: class [org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null, order = 1000, Root bean: class [org.springframework.security.web.authentication.www.BasicAuthenticationFilter]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null, order = 1200, Root bean: class [org.springframework.security.web.savedrequest.RequestCacheAwareFilter]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null, order = 1300, Root bean: class [org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMetho                                                                                                                                                                             dName=null; initMethodName=null; destroyMethodName=null, order = 1400, Root bean: class [org.springframework.security.web.authentication.AnonymousAuthenticationFilter]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null, order = 1700, Root bean: class [org.springframework.security.web.session.SessionManagementFilter]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null, order = 1800, Root bean: class [org.springframework.security.web.access.ExceptionTranslationFilter]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null, order = 1900, <org.springframework.security.web.access.intercept.FilterSecurityInterceptor#0>, order = 2000]
384  [main] INFO  org.springframework.security.ldap.server.ApacheDSContainer  - Setting working directory for LDAP_PROVIDER: /Users/gturnquist/apache-ds-server
427  [main] INFO  org.springframework.security.ldap.server.ApacheDSContainer  - Starting directory server...
427  [main] WARN  org.apache.directory.server.core.DefaultDirectoryService  - ApacheDS shutdown hook has NOT been registered with the runtime.  This default setting for standalone operation has been overriden.
967  [main] ERROR org.apache.directory.server.schema.registries.DefaultAttributeTypeRegistry  - attributeType w/ OID 2.5.4.16 not registered!
1502 [main] INFO  org.springframework.security.ldap.server.ApacheDSContainer  - Loading LDIF file: /Users/gturnquist/src/gs-authenticating-ldap/complete/build/resources/main/test-server.ldif
1506 [main] WARN  org.apache.directory.shared.ldap.ldif.LdifReader  - No version information : assuming version: 1
1562 [main] INFO  org.springframework.security.ldap.DefaultSpringSecurityContextSource  -  URL 'ldap://127.0.0.1:33389/dc=springframework,dc=org', root DN is 'dc=springframework,dc=org'
1773 [main] INFO  org.springframework.security.web.DefaultSecurityFilterChain  - Creating filter chain: org.springframework.security.web.util.AnyRequestMatcher@1, [org.springframework.security.web.context.SecurityContextPersistenceFilter@4ceb3e97, org.springframework.security.web.authentication.logout.LogoutFilter@685a52f4, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter@1fc46b2, org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter@59855122, org.springframework.security.web.authentication.www.BasicAuthenticationFilter@3f91bd18, org.springframework.security.web.savedrequest.RequestCacheAwareFilter@79159342, org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter@77771028, org.springframework.security.web.authentication.AnonymousAuthenticationFilter@29c5ef5e, org.springframework.security.web.session.SessionManagementFilter@1e619bf, org.springframework.security.web.access.ExceptionTranslationFilter@29a44b92, org.springframework.security.web.access.intercept.FilterSecurityInterceptor@615d7399]
1787 [main] INFO  org.springframework.security.config.http.DefaultFilterChainValidator  - Checking whether login URL '/spring_security_login' is accessible with your configuration
```

Great! Let's try and visit the site at <http://localhost:8080>. When you visit that page, you should get redirected to a login page provided by Spring Security.

Enter username **ben** and password **benscredentials**. It should let you in to see a very simple message in your browser.

```
Welcome to the home page!
```

If you want to see more details about what is happening, you can adjust **log4j.properties** and increase logging levels to DEBUG. To narrow the logging to just the LDAP parts, add:

```txt
log4j.category.org.springframework.security.ldap=DEBUG
```

Next Steps
----------
Congratulations! You have just written a web application and secured it with Spring Security.

- You can find detailed documentation at [Spring Security 3.2.x reference](http://static.springsource.org/spring-security/site/docs/3.2.x/reference/springsecurity-single.html).
- See [LDAP-specific Spring Security references](http://static.springsource.org/spring-security/site/docs/3.2.x/reference/springsecurity-single.html#ldap)
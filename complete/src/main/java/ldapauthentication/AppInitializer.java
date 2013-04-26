package ldapauthentication;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

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

//	@Override
//	protected Filter[] getServletFilters() {
//		DelegatingFilterProxy filterChainProxy = new DelegatingFilterProxy();
//		filterChainProxy.setTargetBeanName("springSecurityFilterChain");
//		return new Filter[] { filterChainProxy };
//	}

}

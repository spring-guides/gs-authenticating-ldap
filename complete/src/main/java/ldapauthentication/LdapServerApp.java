package ldapauthentication;





public class LdapServerApp {
	
	public static void main(String[] args) throws Exception {
//		DirectoryService service = new DefaultDirectoryService();
//		JdbmPartition partition = new JdbmPartition();
//		partition.setId("rootPartition");
//		partition.setSuffix("dc=springframework,dc=org");
//		service.addPartition(partition);
//        service.setExitVmOnShutdown(false);
//        service.setShutdownHookEnabled(true);
//        service.getChangeLog().setEnabled(false);
//        
//        LdapServer server = new LdapServer();
//        server.setDirectoryService(service);
//        server.setTransports(new TcpTransport(53389));
//        
//        service.startup();
//        server.start();
//        
//		ApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class);
//		
//		Resource[] ldifs = ctx.getResources("classpath:test-server.ldif");
//		for (Resource ldif : ldifs) {
//			System.out.println("Seeking " + ldif.getFilename());
//			String ldiffile;
//			try {
//				ldiffile = ldif.getFile().getAbsolutePath();
//			} catch (IOException e) {
//				ldiffile = ldif.getURI().toString();
//			}
//			System.out.println("Loading LDIF " + ldiffile);
//			LdifFileLoader loader = new LdifFileLoader(service.getAdminSession(), ldiffile);
//			loader.execute();
//		}
	}
}

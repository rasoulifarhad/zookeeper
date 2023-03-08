package com.farhad.example.demo.zookeeper.curator.example.discovery;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;
import org.apache.zookeeper.KeeperException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiscoveryExample {
   
    private static final String PATH = "/com/farhad/example/discovery";

    public static void main(String[] args) throws Exception {
               // This method is scaffolding to get the example up and running

               TestingServer                                   server = new TestingServer();
               CuratorFramework                                client = null;
               ServiceDiscovery<InstanceDetails>               serviceDiscovery = null;
               Map<String, ServiceProvider<InstanceDetails>>   providers = new HashMap<>();
               try
               {
                   client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
                   client.start();
       
                   JsonInstanceSerializer<InstanceDetails> serializer = new JsonInstanceSerializer<InstanceDetails>(InstanceDetails.class);
                   serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class).client(client).basePath(PATH).serializer(serializer).build();
                   serviceDiscovery.start();
       
                   processCommands(serviceDiscovery, providers, client);
               }
               finally
               {
                   for ( ServiceProvider<InstanceDetails> cache : providers.values() )
                   {
                       CloseableUtils.closeQuietly(cache);
                   }
       
                   CloseableUtils.closeQuietly(serviceDiscovery);
                   CloseableUtils.closeQuietly(client);
                   CloseableUtils.closeQuietly(server);
               }
    }

    private static void processCommands(ServiceDiscovery<InstanceDetails> serviceDiscovery,
            Map<String, ServiceProvider<InstanceDetails>> providers, CuratorFramework client) throws Exception {

                printHelp() ;

                List<ExampleServer> servers = new ArrayList<>() ;

                try {
                    
                    BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
                    boolean done  = false ;

                    while ( !done ) {
                        

                        log.info("> ");

                        String line = in.readLine();

                        if ( line == null  ) {
                            break;
                        }

                        String      command = line.trim();
                        String[]    parts = command.split("\\s");
                        if ( parts.length == 0 )
                        {
                            continue;
                        }

                        String      operation = parts[0];
                        String      args[] = Arrays.copyOfRange(parts, 1, parts.length);

                        if ( operation.equalsIgnoreCase("help") || operation.equalsIgnoreCase("?") ) {
                            printHelp();
                        } else if ( operation.equalsIgnoreCase("q") || operation.equalsIgnoreCase("quit") ) {
                            done = true;
                        } else if ( operation.equalsIgnoreCase("add") ) {
                            addInstance(args, client, command, servers);
                        } else if ( ( operation.equalsIgnoreCase("delete") ) ) {
                            deleteInstance(args, command, servers);
                        } else if ( ( operation.equalsIgnoreCase("random") ) ) {
                            listRandomInstance(args, serviceDiscovery, providers, command);
                        } else if ( ( operation.equalsIgnoreCase("list") ) ) {
                            listInstances(serviceDiscovery);
                        }
                            
                    }
                } finally {
                    for (ExampleServer server : servers) {
                        CloseableUtils.closeQuietly(server);
                    }
                }
    }

    private static void listRandomInstance(String[] args, ServiceDiscovery<InstanceDetails> serviceDiscovery, Map<String, 
                                                            ServiceProvider<InstanceDetails>> providers, 
                                                            String command) throws Exception {
        // this shows how to use a ServiceProvider
        // in a real application you'd create the ServiceProvider early for the service(s) you're interested in

        if ( args.length != 1 ) {
            log.error("syntax error (expected random <name>): {}", command);
            return;
        }

        String                              serviceName = args[0];
        ServiceProvider<InstanceDetails>    provider = providers.get(serviceName);

        if ( provider == null ) {

            provider = serviceDiscovery.serviceProviderBuilder().serviceName(serviceName).providerStrategy(new RandomStrategy<InstanceDetails>()).build();

            providers.put(serviceName, provider);

            provider.start();

            Thread.sleep(2500); // give the provider time to warm up - in a real application you wouldn't need to do this
        }

        ServiceInstance<InstanceDetails>    instance = provider.getInstance();
        
        if ( instance == null ) {
            log.error("No instances named: {}", serviceName);
        } else {
            outputInstance(instance);
        }
    }

    private static void listInstances(ServiceDiscovery<InstanceDetails> serviceDiscovery) throws Exception {
       
        // This shows how to query all the instances in service discovery

        try {
            Collection<String>  serviceNames = serviceDiscovery.queryForNames();

            log.info("{} type(s)", serviceNames.size());

            for ( String serviceName : serviceNames ) {

                Collection<ServiceInstance<InstanceDetails>> instances = serviceDiscovery.queryForInstances(serviceName);
                log.info(serviceName);

                for ( ServiceInstance<InstanceDetails> instance : instances ) {
                    outputInstance(instance);
                }
            }

        } catch ( KeeperException.NoNodeException e ) {
            System.err.println("There are no registered instances.");
        } finally {
            CloseableUtils.closeQuietly(serviceDiscovery);
        }
    }

    private static void outputInstance(ServiceInstance<InstanceDetails> instance) {
        log.info("\t {} : {} ", instance.getPayload().getDescription(), instance.buildUriSpec());
    }

    private static void deleteInstance(String[] args, String command, List<ExampleServer> servers) {

        // simulate a random instance going down
        // in a real application, this would occur due to normal operation, a crash, maintenance, etc.

        if ( args.length != 1 ) {
            log.error("syntax error (expected delete <name>): {} ", command);
            return;
        }

        final String    serviceName = args[0];
        ExampleServer   server = Iterables.find(
                                                servers,
                                                new Predicate<ExampleServer>() {
                                                    @Override
                                                    public boolean apply(ExampleServer server) {
                                                        return server.getThisInstance().getName().endsWith(serviceName);
                                                    }
                                                },
                                                null);

        if ( server == null ) {
            log.error("No servers found named: {}", serviceName);
            return;
        }

        servers.remove(server);
        CloseableUtils.closeQuietly(server);
        log.info("Removed a random instance of: {}", serviceName);
    }


    private static void addInstance(String[] args, CuratorFramework client, String command,
            List<ExampleServer> servers) throws Exception {

        // simulate a new instance coming up
        // in a real application, this would be a separate process
        if ( args.length < 2  )  {
            log.error("syntax error (expected add <name> <description>): {}", command);
            return;
        }
        
        StringBuilder desc = new StringBuilder();

        for (int i = 1; i < args.length; i++) {
            if ( i >  1 ) {
                desc.append(' ');
            } 
            desc.append(args[i]);
        }

        String serviceName = args[0];
        ExampleServer server = new ExampleServer(client, PATH, serviceName, desc.toString());

        servers.add(server);
        server.start();

        log.info("{} added", serviceName);

        
    }

    private static void printHelp()
    {
        log.info("An example of using the ServiceDiscovery APIs. This example is driven by entering commands at the prompt:");
        log.info("add <name> <description>: Adds a mock service with the given name and description");
        log.info("delete <name>: Deletes one of the mock services with the given name");
        log.info("list: Lists all the currently registered services");
        log.info("random <name>: Lists a random instance of the service with the given name");
        log.info("quit: Quit the example");
        log.info("");
    }

}

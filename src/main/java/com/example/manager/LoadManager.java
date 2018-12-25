package com.example.manager;

import com.example.common.LoadMetric;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import java.io.IOException;
import java.util.Set;

/**
 * Created by Sujeet on 25/12/18.
 */

@Path("/")
public class LoadManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadManager.class);
    private static final HttpClient httpClient = new DefaultHttpClient();
    private static ServiceProvider<LoadMetric> serviceProvider;
    private static int managerPort;

    /**
     * Manager Start
     * java LoadManager 18000
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("Port number not specified");
        }

        managerPort = Integer.parseInt(args[0]);

        startRestServer();

        startDiscovery();

        LOGGER.info("Manager started on port : {}", managerPort);
    }

    private static void startDiscovery() throws Exception {
        //curator framework start
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("localhost:2181", new RetryNTimes(5, 10000));
        curatorFramework.start();

        //service discovery start
        ServiceDiscovery<LoadMetric> serviceDiscovery = ServiceDiscoveryBuilder
                .builder(LoadMetric.class)
                .basePath("rest-worker-group") //should be same as base path in worker service discovery
                .client(curatorFramework)
                .build();
        serviceDiscovery.start();

        //service provide start
        serviceProvider = serviceDiscovery
                .serviceProviderBuilder()
                .serviceName("worker")
                .providerStrategy(new BalancedLoadStrategy()) // default is round robin
                .build();

        serviceProvider.start();

    }

    private static void startRestServer() {
        System.setProperty("org.jboss.resteasy.port", "" + managerPort);
        UndertowJaxrsServer server = new UndertowJaxrsServer().start();
        server.deploy(ManagerApp.class);
    }

    @GET
    @Path("/delegate")
    public String delegate() throws Exception {
        ServiceInstance<LoadMetric> instance = serviceProvider.getInstance();// get worker instance
        String address = instance.buildUriSpec();
        String response = getResponse(address + "/work");
        LOGGER.info("Response : {} ", response);
        return response;
    }

    private String getResponse(String url) {
        HttpGet getRequest = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(getRequest);
            return IOUtils.toString(response.getEntity().getContent());
        } catch (IOException e) {
            LOGGER.error("Error occurred while connecting to worker ", e);
            return "Error occurred : " + e.getMessage();
        }
    }


    @ApplicationPath("/")
    public static class ManagerApp extends Application {
        public ManagerApp() {

        }

        @Override
        public Set<Class<?>> getClasses() {
            return Sets.newHashSet(LoadManager.class);
        }
    }

}

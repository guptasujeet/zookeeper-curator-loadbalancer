package com.example.worker;

import com.example.common.LoadMetric;
import com.google.common.collect.Sets;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Sujeet on 25/12/18.
 */

@Path("/")
public class TaskWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskWorker.class);

    private static final AtomicReference<Double> load = new AtomicReference<>(0.0);
    private static final LoadMetric metric = new LoadMetric(load.get());
    private static String workerName;
    private static int workerPort;
    private static ServiceInstance<LoadMetric> serviceInstance;

    /**
     * Worker Start
     * java TaskWorker Worker_1 18005
     * java TaskWorker Worker_2 18006
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Please specify workerName and workerPort");
        }

        workerName = args[0];
        workerPort = Integer.parseInt(args[1]);

        startRestServer();
        registerInZookeeper();

        LOGGER.info("Worker : " + workerName + " , started on port : " + workerPort);
    }

    private static void registerInZookeeper() throws Exception {
        //curator framework start
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("localhost:2181", new RetryNTimes(5, 10000));
        curatorFramework.start();

        //create service instance
        serviceInstance = ServiceInstance.<LoadMetric>builder()
                .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
                .address("localhost")
                .port(workerPort)
                .name("worker")
                .payload(metric) // mutable metric object, used to update data while updating service
                .build();

        //register serviceInstance
        ServiceDiscovery<LoadMetric> serviceDiscovery = ServiceDiscoveryBuilder
                .builder(LoadMetric.class)
                .basePath("rest-worker-group") //should be same as base path in manager service discovery
                .client(curatorFramework)
                .thisInstance(serviceInstance)
                .watchInstances(true) // watch change in instance details
                .build();
        serviceDiscovery.start();

        mockLoad(serviceDiscovery);
    }

    private static void mockLoad(ServiceDiscovery<LoadMetric> serviceDiscovery) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(() -> updateLoadMetric(serviceDiscovery), 5, 30, TimeUnit.SECONDS); // update load every few seconds ;
    }

    private static void updateLoadMetric(ServiceDiscovery<LoadMetric> serviceDiscovery) {
        load.set(Math.random());
        try {
            metric.setLoad(load.get());
            serviceDiscovery.updateService(serviceInstance);
            LOGGER.info("Updated load to : {} ", load.get());
        } catch (Exception e) {
            LOGGER.error("Error updating load ", e);
        }
    }

    private static void startRestServer() {
        System.setProperty("org.jboss.resteasy.port", "" + workerPort);
        UndertowJaxrsServer server = new UndertowJaxrsServer().start();
        server.deploy(WorkerApp.class);
    }

    @GET
    @Path("/work")
    public String work() {
        String response = "Work done by : " + workerName + " , @ Load : " + load.get() + " , @ Time : " + LocalTime.now();
        LOGGER.info("Response : {}", response);
        return response;
    }


    @ApplicationPath("/")
    public static class WorkerApp extends Application {
        public WorkerApp() {

        }

        @Override
        public Set<Class<?>> getClasses() {
            return Sets.newHashSet(TaskWorker.class);
        }
    }

}

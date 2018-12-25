package com.example.manager;

import com.example.common.LoadMetric;
import org.apache.curator.x.discovery.ProviderStrategy;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by Sujeet on 25/12/18.
 */
public class BalancedLoadStrategy implements ProviderStrategy<LoadMetric> {

    private final Random random = new Random();

    @Override
    public ServiceInstance<LoadMetric> getInstance(InstanceProvider<LoadMetric> instanceProvider) throws Exception {
        List<ServiceInstance<LoadMetric>> instances = instanceProvider.getInstances();
        if (instances.size() == 0) {
            return null;
        }
        TreeMap<Double, List<ServiceInstance<LoadMetric>>> sortedInstances = toSortedInstances(instances);
        List<ServiceInstance<LoadMetric>> lightLoadInstances = sortedInstances.get(sortedInstances.firstKey());
        if (lightLoadInstances.size() == 1) {
            return lightLoadInstances.get(0);
        } else { // pick random
            int randomIndex = random.nextInt(lightLoadInstances.size());
            return lightLoadInstances.get(randomIndex);
        }
    }

    private TreeMap<Double, List<ServiceInstance<LoadMetric>>> toSortedInstances(List<ServiceInstance<LoadMetric>> instances) {
        //ascending order minimum load at first
        TreeMap<Double, List<ServiceInstance<LoadMetric>>> sortedMap = new TreeMap<>();
        for (ServiceInstance<LoadMetric> instance : instances) {
            double load = instance.getPayload().getLoad();
            List<ServiceInstance<LoadMetric>> data = sortedMap.computeIfAbsent(load, k -> new LinkedList<>());
            data.add(instance);
        }
        return sortedMap;
    }
}

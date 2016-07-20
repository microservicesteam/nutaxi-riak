package com.msteam.riak;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NuRiakCluster {

    @Autowired
    private AmazonEC2Client ec2;

    @Autowired
    private Filter instanceFilterForRiak;

    @Value("aws.instance.statusCode.running")
    private static int RUNNING;

    @Value("aws.instance.statusCode.stopped")
    private static int STOPPED;

    private static Logger LOG = LoggerFactory.getLogger(NuRiakCluster.class);

    public List<String> discoverRiakClusterInstancePublicIps() {
        List<String> instanceIps = discoverRiakClusterInstances()
                .stream()
                .map(Instance::getPublicIpAddress)
                .collect(Collectors.toList());

        LOG.info("Found the following instance IP addresses-s: ");
        instanceIps.stream().forEach(LOG::info);

        return instanceIps;
    }

    public StartInstancesResult startRiakCluster() {
        LOG.info("Starting Riak Cluster");
        StartInstancesRequest startInstancesRequest = new StartInstancesRequest(discoverRiakClusterInstanceIds());
        return ec2.startInstances(startInstancesRequest);
    }

    public StopInstancesResult stopRiakCluster() {
        LOG.info("Stopping Riak Cluster");
        StopInstancesRequest stopInstancesRequest = new StopInstancesRequest(discoverRiakClusterInstanceIds());
        return ec2.stopInstances(stopInstancesRequest);
    }


    //TODO LM: Search for a better/safer alternative
    public void pollRiakClusterStarted() throws InterruptedException {
        boolean isStartingUp = true;

        while (isStartingUp) {
            isStartingUp = pollRiakCluster(RUNNING);
            if (isStartingUp) {
                LOG.info("Cluster is still starting up, sleeping for 5 seconds");
            } else {
                LOG.info("Cluster stopped successfully");
            }
            Thread.sleep(5000);
        }
    }

    //TODO LM: Search for a better/safer alternative
    public void pollRiakClusterStopped() throws InterruptedException {
        boolean isStopped = true;

        while (isStopped) {
            isStopped = pollRiakCluster(STOPPED);
            if (isStopped) {
                LOG.info("Cluster is still shutting down, sleeping for 5 seconds");
            } else {
                LOG.info("Cluster stopped successfully");
            }
            Thread.sleep(5000);
        }
    }

    private boolean pollRiakCluster(int statusCode) {
        return ec2.describeInstanceStatus()
                .getInstanceStatuses()
                .stream()
                .map(InstanceStatus::getInstanceState)
                .map(InstanceState::getCode)
                .filter(code -> code != statusCode)
                .findAny()
                .isPresent();
    }

    private DescribeInstancesResult getInstancesResult() {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        LOG.info("Fetching instance information on Riak related clusters");
        return ec2.describeInstances(
                describeInstancesRequest.withFilters(instanceFilterForRiak));
    }

    private List<Instance> discoverRiakClusterInstances() {
        return getInstancesResult()
                .getReservations()
                .stream()
                .flatMap(reservation -> reservation.getInstances().stream())
                .collect(Collectors.toList());
    }

    private List<String> discoverRiakClusterInstanceIds() {
        List<String> instanceIds = discoverRiakClusterInstances()
                .stream()
                .map(Instance::getInstanceId)
                .collect(Collectors.toList());

        LOG.info("Found the following instance id-s: ");
        instanceIds.stream().forEach(LOG::info);

        return instanceIds;
    }

}

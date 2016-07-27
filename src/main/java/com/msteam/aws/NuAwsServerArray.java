package com.msteam.aws;

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
import com.msteam.aws.model.InstanceStateCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.msteam.aws.model.InstanceStateCode.RUNNING;
import static com.msteam.aws.model.InstanceStateCode.STOPPED;

@Component
public class NuAwsServerArray {

    private final AmazonEC2Client amazonEC2Client;
    private final Filter instanceFilter;
    private final int maxRetry;

    private static Logger LOG = LoggerFactory.getLogger(NuAwsServerArray.class);

    public NuAwsServerArray(AmazonEC2Client amazonEC2Client, Filter instanceFilter, int maxRetry) {
        this.amazonEC2Client = amazonEC2Client;
        this.instanceFilter = instanceFilter;
        this.maxRetry = maxRetry;
    }

    public List<String> discoverPublicIps() {
        List<String> instanceIps = discoverClusterInstances()
                .stream()
                .map(Instance::getPublicIpAddress)
                .collect(Collectors.toList());

        LOG.info("Found the following instance IP addresses-s: ");
        instanceIps.stream().forEach(LOG::info);

        return instanceIps;
    }

    public StartInstancesResult start() throws InterruptedException {
        LOG.info("Starting Riak Cluster");
        StartInstancesRequest startInstancesRequest = new StartInstancesRequest(discoverClusterInstanceIds());
        StartInstancesResult startInstancesResult = amazonEC2Client.startInstances(startInstancesRequest);
        pollInstances(RUNNING);
        return startInstancesResult;
    }

    public StopInstancesResult stop() throws InterruptedException {
        LOG.info("Stopping Riak Cluster");
        StopInstancesRequest stopInstancesRequest = new StopInstancesRequest(discoverClusterInstanceIds());
        StopInstancesResult stopInstancesResult = amazonEC2Client.stopInstances(stopInstancesRequest);
        pollInstances(STOPPED);
        return stopInstancesResult;
    }


    //TODO LM: Search for a better/safer alternative
    public synchronized void pollInstances(InstanceStateCode instanceStatus) throws InterruptedException {
        boolean isStatusReached = true;
        int retry = 0;

        while (isStatusReached) {
            isStatusReached = pollCluster(instanceStatus.statusCode);
            if (isStatusReached) {
                LOG.info("Cluster is still not in sate: " + instanceStatus.name + ", sleeping for 5 seconds");
                Thread.sleep(5000);
                retry++;
            } else {
                LOG.info("Cluster state: " + instanceStatus.name + " reached successfully");
            }

            if (retry >= maxRetry) {
                LOG.info(instanceStatus.name + " Cluster state is not reached in " + maxRetry
                        + " retries. Please check aws console manually");
                return;
            }
        }
    }

    private boolean pollCluster(int statusCode) {
        return amazonEC2Client.describeInstanceStatus()
                .getInstanceStatuses()
                .stream()
                .map(InstanceStatus::getInstanceState)
                .map(InstanceState::getCode)
                .filter(code -> code != statusCode)
                .findAny()
                .isPresent();
    }

    private DescribeInstancesResult getFilteredInstances() {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        LOG.info("Fetching instance information on filter related clusters");
        return amazonEC2Client.describeInstances(
                describeInstancesRequest.withFilters(instanceFilter));
    }

    private List<Instance> discoverClusterInstances() {
        return getFilteredInstances()
                .getReservations()
                .stream()
                .flatMap(reservation -> reservation.getInstances().stream())
                .collect(Collectors.toList());
    }

    private List<String> discoverClusterInstanceIds() {
        List<String> instanceIds = discoverClusterInstances()
                .stream()
                .map(Instance::getInstanceId)
                .collect(Collectors.toList());

        LOG.info("Found the following instance id-s: ");
        instanceIds.stream().forEach(LOG::info);

        return instanceIds;
    }

}

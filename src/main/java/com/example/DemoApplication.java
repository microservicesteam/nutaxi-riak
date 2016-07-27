package com.example;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.cap.Quorum;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.RiakObject;
import com.basho.riak.client.core.util.BinaryValue;
import com.msteam.aws.NuAwsServerArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.basho.riak.client.core.RiakNode.Builder.buildNodes;

public class DemoApplication {

    @Autowired
    private RiakNode.Builder riakNodeBuilder;

    @Autowired
    private NuAwsServerArray nuRiakCluster;

    private static Logger LOG = LoggerFactory.getLogger(DemoApplication.class);

    private RiakCluster riakCluster;

    private RiakClient riakClient;

    public DemoApplication() {
        List<RiakNode> nodes = buildNodes(riakNodeBuilder, nuRiakCluster.discoverPublicIps());
        riakCluster = new RiakCluster.Builder(nodes).build();
        riakClient = new RiakClient(riakCluster);
    }

    public static void main(String[] args) throws Exception {
        DemoApplication demo = new DemoApplication();
        demo.run();
    }

    public void run() throws Exception {
        nuRiakCluster.start();
        riakCluster.start();

        writeAValueToRiakCluster();
        readAValueFromRiakCluster();

        riakCluster.shutdown();
        nuRiakCluster.stop();
    }

    private void writeAValueToRiakCluster() throws Exception {
        Namespace namespace = new Namespace("default", "my_bucket");
        Location location = new Location(namespace, "my_location");

        RiakObject riakObject = new RiakObject();
        riakObject.setValue(BinaryValue.create("my_value"));
        StoreValue store = new StoreValue.Builder(riakObject)
                .withLocation(location)
                .withOption(StoreValue.Option.W, new Quorum(3)).build();
        riakClient.execute(store);

        LOG.info("Written 'my_value' into riak");
    }

    private void readAValueFromRiakCluster() throws Exception {
        Namespace namespace = new Namespace("default", "my_bucket");
        Location location = new Location(namespace, "my_location");

        FetchValue fetchValue = new FetchValue.Builder(location).build();
        FetchValue.Response response = riakClient.execute(fetchValue);

        RiakObject obj = response.getValue(RiakObject.class);

        LOG.info("Read the following value from riak: " + obj.getValue().toString());
    }
}

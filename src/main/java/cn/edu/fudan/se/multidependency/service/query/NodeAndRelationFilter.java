package cn.edu.fudan.se.multidependency.service.query;

import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeAndRelationFilter {

    public static Map<String, Boolean> listOfPackagesForCassandra(){
        Map<String, Boolean> selectedPcks = new HashMap<>();
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/auth/", true);
        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/cache/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/cli/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/client/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/concurrent/", true);
        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/config/", true);
        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/cql/", true);
        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/cql3/", false);
        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/db/", true);
        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/dht/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/exceptions/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/gms/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/hadoop/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/io/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/locator/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/metrics/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/net/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/notifications/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/repair/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/scheduler/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/security/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/serializers/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/service/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/sink/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/streaming/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/thrift/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/tools/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/tracing/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/transport/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/triggers/", true);
//        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/utils/", true);
        return selectedPcks;
    }


    public static Map<String, Boolean> listOfPackagesForAtlas(){
        Map<String, Boolean> selectedPcks = new HashMap<>();
        selectedPcks.put("/atlas/addons/", true);
        selectedPcks.put("/atlas/authorization/src/main/java/org/apache/atlas/authorize/", true);
        selectedPcks.put("/atlas/client/", true);
        selectedPcks.put("/atlas/common/src/main/java/org/apache/atlas/", true);
        selectedPcks.put("/atlas/graphdb/", true);
        selectedPcks.put("/atlas/intg/src/main/java/org/apache/atlas/", true);
        selectedPcks.put("/atlas/notification/src/main/java/org/apache/atlas/", true);
        selectedPcks.put("/atlas/plugin-classloader/src/main/java/org/apache/atlas/plugin/classloader/", true);
        selectedPcks.put("/atlas/repository/src/main/java/org/apache/atlas/", true);
        selectedPcks.put("/atlas/server-api/src/main/java/org/apache/atlas/", true);
        selectedPcks.put("/atlas/tools/", true);
        selectedPcks.put("/atlas/webapp/src/main/java/org/apache/atlas/", true);
        return selectedPcks;
    }

}

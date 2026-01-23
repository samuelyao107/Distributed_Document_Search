package cluster.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher {

    private static final String ELECTION_PATH = "/election";
    private String currentZnodeName;
    private ZooKeeper zookeeperServer;
    private final OnElectionCallback onElectionCallback;

    public LeaderElection(ZooKeeper zookeeperServer, OnElectionCallback onElectionCallback) {
        this.zookeeperServer = zookeeperServer;
        this.onElectionCallback = onElectionCallback;
    }


    public void wantToBeLeader() throws InterruptedException, KeeperException {
        String znodePrefix = ELECTION_PATH + "/c_";
        String znodeFullPath = zookeeperServer.create(znodePrefix,
                new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Created leader election node: " + znodeFullPath);
        this.currentZnodeName = znodeFullPath.replace(ELECTION_PATH + "/", "");

    }

    public void electTheLeader() throws InterruptedException, KeeperException {
        Stat predecessorStat = null;
        String predecessorZnodeName = "";

        while (predecessorStat == null) {
            List<String> children = zookeeperServer.getChildren(ELECTION_PATH, false);
            Collections.sort(children);
            String smallestZnodeName = children.get(0);
            if(currentZnodeName.equals(smallestZnodeName)){
                System.out.println(currentZnodeName + " is the leader.");
                onElectionCallback.onElectedLeader();
                return;
            }else{
                System.out.println(currentZnodeName + " is not the leader.");
                int predecessorIndex = Collections.binarySearch(children, currentZnodeName) -1;
                predecessorZnodeName = children.get(predecessorIndex);
                predecessorStat = zookeeperServer.exists(ELECTION_PATH + "/"+ predecessorZnodeName, this);
            }
        }
        onElectionCallback.onWorker();

        System.out.println("Watching znode " + predecessorZnodeName);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        //be called on a separate thread in case of a new event
        switch(watchedEvent.getType()) {
            case NodeDeleted:
                try{
                    electTheLeader();
                }catch(Exception e){
                    e.printStackTrace();
                }
        }

    }


}
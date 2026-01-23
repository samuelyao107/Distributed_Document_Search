import cluster.management.LeaderElection;
import cluster.management.ServiceRegistry;
import org.apache.zookeeper.*;

import java.io.IOException;

public class Application implements Watcher {
    private static final String ZOOKEEPER_SERVER = "localhost:2181";
    private static final int TIMEOUT = 3000 ;
    private static final int DEFAULT_PORT = 8080;
    private ZooKeeper zookeeperServer;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        int currentPort = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        Application application = new Application();
        ZooKeeper zooKeeper = application.connectToServer();
        ServiceRegistry serviceRegistry = new ServiceRegistry(zooKeeper);

        OnElectionAction onElectionAction = new OnElectionAction(serviceRegistry, currentPort);
        LeaderElection leaderElection = new LeaderElection(zooKeeper, onElectionAction);
        leaderElection.wantToBeLeader();
        leaderElection.electTheLeader();

        application.run();
        application.close();
    }



    public ZooKeeper connectToServer() throws IOException {
        this.zookeeperServer = new ZooKeeper(ZOOKEEPER_SERVER, TIMEOUT, this);
        return this.zookeeperServer;
    }


    public void run() throws InterruptedException {
        synchronized (zookeeperServer){
            zookeeperServer.wait();
        }
    }

    public void close() throws InterruptedException {
        zookeeperServer.close();
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                if(watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("Connected to ZooKeeper");
                }else{
                    synchronized (zookeeperServer){
                        System.out.println("Disconnected from ZooKeeper");
                        zookeeperServer.notifyAll();
                    }
                }
        }

    }
}

package cluster.management;


import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher {
    private static final String REGISTRY_ZNODE = "/service_registry";
    private final ZooKeeper zookeeper;
    private String currenZnode = null;
    private List<String> allAddresses = new ArrayList<>();

    public ServiceRegistry(ZooKeeper zookeeper) {
        this.zookeeper = zookeeper;
        createServiceRegistryNode();
    }

    public void registerService(String address)
            throws KeeperException, InterruptedException {
        this.currenZnode = zookeeper.create(REGISTRY_ZNODE+"/n", address.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Service registry node created: " + currenZnode);
    }

    private void createServiceRegistryNode() {
        try{
            if(zookeeper.exists(REGISTRY_ZNODE, false) == null){
                zookeeper.create(REGISTRY_ZNODE, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        }catch(KeeperException e){

        }catch(InterruptedException e){

        }
    }
    public void registerForUpdates(){
        try {
            updateAddresses();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized List<String> getAllAddresses() throws InterruptedException, KeeperException {
        if(allAddresses.isEmpty()){
            updateAddresses();
        }
        return allAddresses;
    }

    public void unregisterFromCluster() throws InterruptedException, KeeperException {
        if(currenZnode != null && zookeeper.exists(currenZnode,false)!=null){
            zookeeper.delete(currenZnode,-1);
        }
    }

    private synchronized void updateAddresses() throws InterruptedException, KeeperException {
        List<String> workerZnodes = zookeeper.getChildren(REGISTRY_ZNODE, this);
        List<String> workerAddresses = new ArrayList<>(workerZnodes.size());

        for(String workerZnode : workerZnodes){
            String workerZnodeFullPath = REGISTRY_ZNODE+"/"+workerZnode;
            Stat stat = zookeeper.exists(workerZnodeFullPath, false);
            if(stat == null){
                continue;
            }
            byte[] data = zookeeper.getData(workerZnodeFullPath, false, stat);
            String address = new String(data);
            workerAddresses.add(address);
        }
        this.allAddresses = Collections.unmodifiableList(workerAddresses);
        System.out.println("All addresses: " + this.allAddresses);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            updateAddresses();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }
    }
}
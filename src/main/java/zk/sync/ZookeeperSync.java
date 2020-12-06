package zk.sync;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZookeeperSync {

	private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
	private static ZooKeeper zk = null;
	private static Stat stat = new Stat();
	private static final String zkServer = "192.168.1.6:2181";
	
	public static void main(String[] args) throws Exception {
		// zk配置数据存放路径
		String path = "/username";
		// 连接zk并且注册一个监听器
		zk = new ZooKeeper(zkServer, 5000, new Watcher() {

			@Override
			public void process(WatchedEvent event) {
				if (KeeperState.SyncConnected == event.getState()) { // zk连接成功通知事件
					if (EventType.None == event.getType() && event.getPath() == null) {
						connectedSemaphore.countDown();
					} else if (event.getType() == EventType.NodeDataChanged) { // zk目录节点数据变化通知事件
						try {
							System.out.println("user name updated, new value: " + new String(zk.getData(event.getPath(), true, stat)));
						} catch (KeeperException | InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
		});
		// 等待zk连接成功的通知
		connectedSemaphore.await();
		// 获取path目录节点的配置数据, 并注册默认的监听器
		System.out.println(new String(zk.getData(path, true, stat)));
		while (true) {
			Thread.sleep(5000);
		}
	}

}

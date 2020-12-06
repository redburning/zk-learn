package zk.lock;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner2 {
	
	private static Logger logger = LoggerFactory.getLogger(Runner2.class);
	private static final String zkServer = "192.168.1.6";
	private static final String lockDir = "/zklock";
	private static WriteLock lock;
	
	public static void main(String[] args) throws InterruptedException, IOException {
		ZookeeperConnection zooKeeperConnection = new ZookeeperConnection();
		ZooKeeper zk = zooKeeperConnection.connect(zkServer);
		lock = new WriteLock(zk, lockDir, null, new LockListener() {

			@Override
			public void lockAcquired() {
				logger.info("Worker1 lockAcquired");
				System.out.println("Worker1 lockAcquired");
				synchronized (lock) {
					lock.notify();
				}
			}

			@Override
			public void lockReleased() {
				logger.info("Worker1 lockReleased");
				System.out.println("Worker1 lockReleased");
			}
			
		});
		
		while (true) {
			try {
				boolean getLock = lock.lock();
				if (!getLock) {
					synchronized (lock) {
						lock.wait();
					}
				}
				logger.info("Worker1 woking...");
				System.out.println("Worker1 woking...");
				Thread.sleep(5000);
				lock.unlock();
			} catch (KeeperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
package zk.lock;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner1 {
	
	public static Logger logger = LoggerFactory.getLogger(Runner1.class);
	public static final String zkServer = "192.168.1.6";
	public static final String lockDir = "/zklock";
	
	public static void main(String[] args) throws InterruptedException, IOException {
		Thread worker1 = new Thread(new Worker1());
		Thread worker2 = new Thread(new Worker2());
		worker1.setDaemon(true);
		worker2.setDaemon(true);

		worker1.start();
		worker2.start();

		worker1.join();
	}

}

class Worker1 implements Runnable {

	private WriteLock lock;

	public Worker1() throws IOException, InterruptedException {
		ZookeeperConnection zooKeeperConnection = new ZookeeperConnection();
		ZooKeeper zk = zooKeeperConnection.connect(Runner1.zkServer);
		lock = new WriteLock(zk, Runner1.lockDir, null, new LockListener() {

			@Override
			public void lockAcquired() {
				Runner1.logger.info("Worker1 lockAcquired");
				System.out.println("Worker1 lockAcquired");
				synchronized (lock) {
					lock.notify();
				}
			}

			@Override
			public void lockReleased() {
				Runner1.logger.info("Worker1 lockReleased");
				System.out.println("Worker1 lockReleased");
			}
			
		});
		
	}

	@Override
	public void run() {

		while (true) {
			try {
				boolean getLock = lock.lock();
				if (!getLock) {
					synchronized (lock) {
						lock.wait();
					}
				}
				Runner1.logger.info("Worker1 woking...");
				System.out.println("Worker1 woking...");
				Thread.sleep(7000);
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

class Worker2 implements Runnable {

	WriteLock lock;

	public Worker2() throws IOException, InterruptedException {
		ZookeeperConnection zooKeeperConnection = new ZookeeperConnection();
		ZooKeeper zk = zooKeeperConnection.connect(Runner1.zkServer);
		lock = new WriteLock(zk, Runner1.lockDir, null);
		lock.setLockListener(new LockListener() {

			@Override
			public void lockAcquired() {
				Runner1.logger.info("Worker2 lockAcquired");
				System.out.println("Worker2 lockAcquired");
				synchronized (lock) {
					lock.notify();
				}
			}

			@Override
			public void lockReleased() {
				Runner1.logger.info("Worker2 lockReleased");
				System.out.println("Worker2 lockReleased");
			}
		});
	}

	@Override
	public void run() {
		while (true) {
			try {
				boolean getLock = lock.lock();
				if (!getLock) {
					synchronized (lock) {
						lock.wait();
					}
				}
				Runner1.logger.info("Worker2 woking...");
				System.out.println("Worker2 woking...");
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
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Generic thread pool executor that can be paused
 * and schedules job as per the priority
 * 
 * @author amokashi
 *
 */
public class PausableComparableThreadPoolExecutor extends ThreadPoolExecutor {

	private static final long TOO_MANY_JOBS_SLEEP_TIME = 5 * 60 * 1000;
	private static final int QUEUE_INIT_SIZE = 200;

	// Note: PriorityBlockingQueue is currently unbounded
	// Hence, we will never execute executionHandler in the current implementation
	// TODO fix it by implementing BoundedPriorityBlokingQueue
	static RejectedExecutionHandler executionHandler  = new RejectedExecutionHandler() {
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			System.out.println("too many outstanding merge jobs");
			// make the current thread sleep so that it does not add more
			// tasks for the executors
			try {
				Thread.sleep(TOO_MANY_JOBS_SLEEP_TIME);
			} catch (InterruptedException ie) {
				log.error("Interrupted ", ie);
			}
		}
	};
	
	public PausableComparableThreadPoolExecutor(
			int poolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
		super(poolSize,
				poolSize,
				keepAliveTime,
				unit,
				new PriorityBlockingQueue<Runnable>(QUEUE_INIT_SIZE, new CustomComparator()),
				threadFactory,
				executionHandler
			);
	}
	
	public PausableComparableThreadPoolExecutor(
			int poolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(poolSize,
				poolSize,
				keepAliveTime,
				unit,
				new PriorityBlockingQueue<Runnable>(QUEUE_INIT_SIZE, new CustomComparator()),
				threadFactory,
				handler
			);
	}
	
	private boolean isPaused;
	private ReentrantLock pauseLock = new ReentrantLock();
	private Condition unpaused = pauseLock.newCondition();

	protected <T> RunnableFuture<T> newTaskFor (Runnable runnable, T value) {
		return new ComparableFutureTask<T> (runnable, value);
	}

	protected <T> RunnableFuture<T> newTaskFor (Callable<T> callable) {
		return new ComparableFutureTask<T> (callable);
	}

	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		pauseLock.lock();
		try {
			while (isPaused) unpaused.await();
		} catch (InterruptedException ie) {
			t.interrupt();
		} finally {
			pauseLock.unlock();
		}
	}

	public void pause() {
		pauseLock.lock();
		try {
			isPaused = true;
		} finally {
			pauseLock.unlock();
		}
	}

	public void resume() {
		pauseLock.lock();
		try {
			isPaused = false;
			unpaused.signalAll();
		} finally {
			pauseLock.unlock();
		}
	}

	/**
	 * This is needed to make PriorityBlockingQueue work with ThreadPoolExecutor
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6539720
	 * 
	 * @author amokashi
	 *
	 * @param <V>
	 */
	@SuppressWarnings("rawtypes")
	static class ComparableFutureTask<V> extends FutureTask<V> implements Comparable<ComparableFutureTask<V>> {
		Comparable comparable;

		ComparableFutureTask (Callable<V> callable) {
			super (callable);
			comparable = (Comparable) callable;
		}

		ComparableFutureTask (Runnable runnable, V result) {
			super (runnable, result);
			comparable = (Comparable) runnable;
		}

		@SuppressWarnings("unchecked")
		public int compareTo (ComparableFutureTask<V> ftask) {
			return comparable.compareTo (ftask.comparable);
		}
	}

	@SuppressWarnings("unchecked")
	static class CustomComparator implements Comparator<Runnable> {
		@SuppressWarnings("rawtypes")
		@Override
		public int compare(Runnable o1, Runnable o2) {
			//System.out.println("MergeJobComparator " + o1.getClass());
			if(o1 instanceof Comparable && o2 instanceof Comparable) {
				//System.out.println("MergeJobComparator inside");
				return ((Comparable)o1).compareTo((Comparable)o2);
			}
			return 0;
		}
	}
}

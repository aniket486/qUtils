import java.util.ArrayList;
import java.util.Iterator;


public class LongPowers implements Iterator<Long> {

	ArrayList<Long> primes = new ArrayList<Long>();
	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public Long next() {
		
		return null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}

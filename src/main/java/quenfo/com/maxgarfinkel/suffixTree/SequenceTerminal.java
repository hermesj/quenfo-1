package quenfo.com.maxgarfinkel.suffixTree;

/**
 * Represents the terminating item of a sequence.
 * 
 * @author maxgarfinkel
 * 
 */
class SequenceTerminal<S> {
	
	int id;

	private final S sequence;
	
	SequenceTerminal(S sequence, int id2){
		this.sequence = sequence;
		this.id = id2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		if(o == null || o.getClass() != this.getClass())
			return false;
		return ((SequenceTerminal<S>)o).sequence.equals(this.sequence);
	}
	
	public int hashCode(){
		return sequence.hashCode();	
	}

	@Override
	public String toString() {
		//return "$"+sequence.toString()+"$";
		return "$"+id;
	}
	
	public S getSequence(){
		return sequence;
	}
	
	public int getId(){
		return id;
	}
	


}

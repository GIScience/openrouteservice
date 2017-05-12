package heigit.ors.io;

import java.io.ByteArrayOutputStream;

public class ByteArrayOutputStreamEx extends ByteArrayOutputStream  {
	 public ByteArrayOutputStreamEx() { super(); }
	    public ByteArrayOutputStreamEx(int size) { super(size); }

	    /** Returns the internal buffer of this ByteArrayOutputStream, without copying. */
	    public synchronized byte[] getBuffer() {
	        return this.buf;
	    }
}

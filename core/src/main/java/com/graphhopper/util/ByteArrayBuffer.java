package com.graphhopper.util;

import java.nio.ByteBuffer;

public class ByteArrayBuffer {
	private ByteBuffer _buffer;
	
	public ByteArrayBuffer()
	{
		this(4);
	}
	
	public ByteArrayBuffer(int capacity)
	{
		_buffer = ByteBuffer.allocate(capacity);
	}
	
	public void ensureCapacity(int capacity)
	{
		if (_buffer.capacity() < capacity)
			_buffer = ByteBuffer.allocate(capacity);
	}
	
	public byte[] array()
	{
		return _buffer.array();
	}
}

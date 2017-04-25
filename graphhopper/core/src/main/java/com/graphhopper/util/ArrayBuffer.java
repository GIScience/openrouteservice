package com.graphhopper.util;

import java.nio.ByteBuffer;

public class ArrayBuffer {
	private ByteBuffer _buffer;
	
	public ArrayBuffer()
	{
		this(4);
	}
	
	public ArrayBuffer(int capacity)
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

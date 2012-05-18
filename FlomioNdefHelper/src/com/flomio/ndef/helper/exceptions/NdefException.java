package com.flomio.ndef.helper.exceptions;

public class NdefException extends Exception {

	public NdefException(String msg) {
		super(msg);
	}

	public NdefException(String msg, Throwable t) {
		super(msg, t);
	}

}

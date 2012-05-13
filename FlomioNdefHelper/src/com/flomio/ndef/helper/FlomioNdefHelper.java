package com.flomio.ndef.helper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Formatter;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.util.Log;

public class FlomioNdefHelper {

	/**
	 * NFC Forum "URI Record Type Definition"
	 * 
	 * This is a mapping of "URI Identifier Codes" to URI string prefixes, per
	 * section 3.2.2 of the NFC Forum URI Record Type Definition document.
	 */
	private static final BiMap<Byte, String> URI_PREFIX_MAP = ImmutableBiMap
			.<Byte, String> builder().put((byte) 0x00, "")
			.put((byte) 0x01, "http://www.").put((byte) 0x02, "https://www.")
			.put((byte) 0x03, "http://").put((byte) 0x04, "https://")
			.put((byte) 0x05, "tel:").put((byte) 0x06, "mailto:")
			.put((byte) 0x07, "ftp://anonymous:anonymous@")
			.put((byte) 0x08, "ftp://ftp.").put((byte) 0x09, "ftps://")
			.put((byte) 0x0A, "sftp://").put((byte) 0x0B, "smb://")
			.put((byte) 0x0C, "nfs://").put((byte) 0x0D, "ftp://")
			.put((byte) 0x0E, "dav://").put((byte) 0x0F, "news:")
			.put((byte) 0x10, "telnet://").put((byte) 0x11, "imap:")
			.put((byte) 0x12, "rtsp://").put((byte) 0x13, "urn:")
			.put((byte) 0x14, "pop:").put((byte) 0x15, "sip:")
			.put((byte) 0x16, "sips:").put((byte) 0x17, "tftp:")
			.put((byte) 0x18, "btspp://").put((byte) 0x19, "btl2cap://")
			.put((byte) 0x1A, "btgoep://").put((byte) 0x1B, "tcpobex://")
			.put((byte) 0x1C, "irdaobex://").put((byte) 0x1D, "file://")
			.put((byte) 0x1E, "urn:epc:id:").put((byte) 0x1F, "urn:epc:tag:")
			.put((byte) 0x20, "urn:epc:pat:").put((byte) 0x21, "urn:epc:raw:")
			.put((byte) 0x22, "urn:epc:").put((byte) 0x23, "urn:nfc:").build();

	/*
	 * Returns the Tag UUID
	 */
	public static String getTagUuid(Tag tag) {
		byte[] tagIDBytes = tag.getId();
		return mBytesToHexString(tagIDBytes);
	}

	/*
	 * Returns NDEF Messages from the tag
	 */
	public static NdefMessage[] getNdefMessages(Parcelable[] rawMsgs) {
		// Parse the intent
		NdefMessage[] msgs = null;
		if (rawMsgs != null) {
			msgs = new NdefMessage[rawMsgs.length];
			for (int i = 0; i < rawMsgs.length; i++) {
				msgs[i] = (NdefMessage) rawMsgs[i];
			}
		} else {
			// Unknown tag type
			byte[] empty = new byte[] {};
			NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty,
					empty, empty);
			NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
			msgs = new NdefMessage[] { msg };
		}
		return msgs;
	}
	
	/*
	 * Returns NDEF Messages from the tag
	 */
	public static URL getUrlFromTag(Parcelable[] rawMsgs) {
		NdefMessage[] ndefMessages = FlomioNdefHelper.getNdefMessages(rawMsgs);
		byte[] payload = ndefMessages[0].getRecords()[0].getPayload();
		return mUrlNdefDecode(payload);
	}
	
	/*
	 * Writes a URI to the tag
	 */
	public static boolean writeUrlToTag(URI uri, Tag tag) {
		try {
			return writeUrlToTag(uri.toURL(), tag);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * Writes a URL to the tag
	 */
	public static boolean writeUrlToTag(URL url, Tag tag) {
		byte[] payload = mUrlNdefEncode(url);
		NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_URI, new byte[0], payload);
		NdefMessage textMessage = new NdefMessage(
				new NdefRecord[] { textRecord });
		return writeNdefTag(textMessage, tag);
	}
	
	/*
	 * Writes text to tag
	 */
	public static boolean writeTextToTag(String text, Tag tag) {
		NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_TEXT, new byte[0], text.getBytes());
		NdefMessage textMessage = new NdefMessage(
				new NdefRecord[] { textRecord });
		return FlomioNdefHelper.writeNdefTag(textMessage, tag);
	}
	
	/*
	 * Writes vCard to tag
	 */
	public static boolean writeVcardToTag(String text, Tag tag) {
		NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				"text/x-vcard".getBytes(), new byte[0], text.getBytes());
		
		NdefMessage textMessage = new NdefMessage(
				new NdefRecord[] { textRecord });
		return FlomioNdefHelper.writeNdefTag(textMessage, tag);
	}
	
	

	/*
	 * Writes an NdefMessage to a NFC tag
	 */
	public static boolean writeNdefTag(NdefMessage message, Tag tag) {
		int size = message.toByteArray().length;
		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();
				if (!ndef.isWritable()) {
					return false;
				}
				if (ndef.getMaxSize() < size) {
					return false;
				}
				ndef.writeNdefMessage(message);
				return true;
			} else {
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
						return true;
					} catch (IOException e) {
						return false;
					}
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
	}

	/*
	 * Helper functions
	 */
	private static String mBytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);

		Formatter formatter = new Formatter(sb);
		for (byte b : bytes) {
			formatter.format("%02x", b);
		}

		return "0x" + sb.toString().toUpperCase();
	}

	/*
	 * Encodes the URL with NFC URI Prefix
	 */
	private static byte[] mUrlNdefEncode(URL url) {
		// protocol
		String prefix = url.getProtocol() + "://";
		String path = url.getHost() + url.getPath();
		String sub = path.substring(0, 4);

		// check for "www" prefix
		if (path.substring(0, 4).equalsIgnoreCase("www.")) {
			prefix += "www.";
			path = path.substring(4);
		}

		byte[] urlPathByte = path.getBytes();
		byte[] urlPrefixByte = new byte[] { URI_PREFIX_MAP.inverse()
				.get(prefix) };

		byte[] payload = new byte[urlPathByte.length + 1];
		System.arraycopy(urlPrefixByte, 0, payload, 0, urlPrefixByte.length);
		System.arraycopy(urlPathByte, 0, payload, urlPrefixByte.length,
				urlPathByte.length);
		return payload;
	}

	/*
	 * Encodes the URL with NFC URI Prefix
	 */
	private static URL mUrlNdefDecode(byte[] payload) {
		// protocol

		byte[] pathByte = new byte[payload.length];
		System.arraycopy(payload, 1, pathByte, 0, payload.length-2);

		String prefix = URI_PREFIX_MAP.get(payload[0]);
		String path = new String(pathByte);
		URL url = null;
		try {
			url = new URL(prefix + path);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return url;

	}

}

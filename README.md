
FlomioNdefHelper
================
This open source Java library helps you write and read NFC Tags using the NFC Data Exchange Format (NDEF).

NDEF is the most widely supported format for writing and reading NFC tag data. It’s a lightweight, binary message format used to encapsulate arbitrary data like URL’s or MIME-TYPE's. 

The Android SDK does a pretty good job exposing a robust NFC + NDEF API. However it still requires you have an understaing of the underlying NFC Forum spec's - which most people don't. That's where this library comes in, it complements the existing Android SDK and let's you get started fast.

Getting Started
-----------------
Getting started is easy, just import the 'src' folder into your Eclipse working directory as a library project. 

1. In Eclipse, create a new Android project using the src/ folder as the existing source. 
2. In your project properties add the created project to the ‘Libraries’ section of the ‘Android’ category.

Usage
-----------------
After you've parsed the NDEF Discovered or Tag Discovered intent that started your activity, the next step is to interact with the tag itself. This library aims to make common actions simpler - ideally a single line of code.  



Example - Reading URL's from Tags
-----------------

	private void resolveIntent(Intent intent) {
		
		// get the action
		String action = intent.getAction();

		if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			
			// get the tag from the intent
			Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			
			try {

				// read the tag's uuid
				final String tagId = FlomioNdefHelper.getTagUuid(tag);

				// read the url off the tag
				URL url = FlomioNdefHelper.getUrlFromTag(tag);

				// open this URI externally
				startActivity(new Intent(Intent.ACTION_VIEW,
										url));

			} catch (NdefException e) {
				Toast.makeText(this, 
					"Sorry, " + e.getMessage(), Toast.LENGTH_LONG).show();
			}

		}
	}



Example - Writing URL's to Tags
-----------------


	private void resolveIntent(Intent intent, URL url) {
		
		// get the action
		String action = intent.getAction();

		if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			
			// get the tag from the intent
			Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			
			try {
				
				// write the url to the tag
				FlomioNdefHelper.writeUrlToTag(url, tag);
				
				return;

			} catch (NdefException e) {
				Toast.makeText(this, 
					"Sorry, " + e.getMessage(), Toast.LENGTH_LONG).show();
			}

		}
	}


Resources
-----------------
Tutorial on Android's NFC API - http://flomio.com/blog/index.php/2012/05/android-nfc-tutorial-part-1/
Overview of NDEF Basics - http://flomio.com/blog/index.php/2012/05/ndef-basics

License
-----------------
This library is licensed under the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0.html)
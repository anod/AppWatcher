/*
 * Copyright 2013 Philip Schiffer
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.psdev.licensesdialog;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

import java.io.IOException;
import java.io.InputStream;

public final class NoticesXmlParser {


	private NoticesXmlParser() {
    }

    public static Notices parse(final InputStream inputStream) throws Exception {
		LicenseResolver lr = new LicenseResolver();
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(inputStream, null);
			parser.nextTag();
			return readNotices(parser, null, lr);
		} finally {
			inputStream.close();
		}
    }

	private static Notices readNotices(XmlPullParser parser, String ns, LicenseResolver lr) throws XmlPullParserException, IOException {
		Notices notices = new Notices();

		parser.require(XmlPullParser.START_TAG, ns, "notices");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (name.equals("notice")) {
				notices.add(readNotice(parser, ns, lr));
			} else {
				skip(parser);
			}
		}
		return notices;
	}

	private static Notice readNotice(XmlPullParser parser, String ns, LicenseResolver lr) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "notice");
		String name = null;
		String url = null;
		String copyright = null;
		License license = null;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String tagName = parser.getName();
			if (tagName.equals("name")) {
				name = readTagText(parser, "name", ns);
			} else if (tagName.equals("url")) {
				url = readTagText(parser, "url", ns);
			} else if (tagName.equals("copyright")) {
				copyright = readTagText(parser, "copyright", ns);
			} else if (tagName.equals("license")) {
				license = readLicense(parser, ns, lr);
			} else {
				skip(parser);
			}
		}

		return new Notice(name, url, copyright, license);
	}

	private static License readLicense(XmlPullParser parser, String ns, LicenseResolver lr) throws IOException, XmlPullParserException {
		String licenseType = readTagText(parser, "license", ns);
		return lr.read(licenseType);
	}


	// Processes title tags in the feed.
	private static String readTagText(XmlPullParser parser, String tag, String ns) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, tag);
		String title = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, tag);
		return title;
	}

	// For the tags title and summary, extracts their text values.
	private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
				case XmlPullParser.END_TAG:
					depth--;
					break;
				case XmlPullParser.START_TAG:
					depth++;
					break;
			}
		}
	}
}

//
//  ========================================================================
//  Copyright (c) 1995-2016 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package hu.qgears.quickjs.utils;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.QuotedStringTokenizer;
import org.eclipse.jetty.util.ReadLineInputStream;

/**
 * MultiPartInputStream
 *
 * Handle a MultiPart Mime input stream, breaking it up on the boundary into
 * files and strings.
 */
public class InMemoryMultiPartInputStreamParser {
	protected ReadLineInputStream _in;
	protected String _contentType;
	protected IMultipartHandler handler;
	private long maxRequestSize = Long.MAX_VALUE;

	/**
	 * @param in
	 *            Request input stream
	 * @param contentType
	 *            Content-Type header
	 * @param config
	 *            MultipartConfigElement
	 * @param contextTmpDir
	 *            javax.servlet.context.tempdir
	 */
	public InMemoryMultiPartInputStreamParser(InputStream in, String contentType, IMultipartHandler handler) {
		_in = new ReadLineInputStream(in);
		_contentType = contentType;
		this.handler = handler;
	}

	private boolean parsed = false;

	/**
	 * Parse the multipart stream.
	 */
	@SuppressWarnings("resource")
	public void parse() throws Exception {
		if (parsed) {
			throw new IOException("Already parsed");
		}
		parsed = true;

		// initialize
		long total = 0; // keep running total of size of bytes read from input
						// and throw an exception if exceeds
						// MultipartConfigElement._maxRequestSize

		// if its not a multipart request, don't parse it
		if (_contentType == null || !_contentType.startsWith("multipart/form-data")) {
			throw new IOException("Request is not multipart.");
		}

		String contentTypeBoundary = "";
		int bstart = _contentType.indexOf("boundary=");
		if (bstart >= 0) {
			int bend = _contentType.indexOf(";", bstart);
			bend = (bend < 0 ? _contentType.length() : bend);
			contentTypeBoundary = QuotedStringTokenizer.unquote(value(_contentType.substring(bstart, bend)).trim());
		}

		String boundary = "--" + contentTypeBoundary;
		String lastBoundary = boundary + "--";
		byte[] byteBoundary = lastBoundary.getBytes(StandardCharsets.ISO_8859_1);

		// Get first boundary
		String line = _in.readLine();
		if(line==null)
		{
			throw new IOException("Missing initial multi part boundary.");
		}
		line = line.trim();
		if (!line.equals(boundary) && !line.equals(lastBoundary)) {
			throw new IOException("Missing initial multi part boundary.");
		}

		// Empty multipart.
		if (line.equals(lastBoundary)) {
			return;
		}

		// Read each part
		boolean lastPart = false;

		outer: while (!lastPart) {
			String contentDisposition = null;
			String contentType = null;
			String contentTransferEncoding = null;

			MultiMap<String> headers = new MultiMap<>();
			while (true) {
				line = _in.readLine();

				// No more input
				if (line == null)
					break outer;

				// end of headers:
				if ("".equals(line))
					break;

				total += line.length();
				if (total > maxRequestSize)
					throw new IllegalStateException("Request exceeds maxRequestSize (" + maxRequestSize + ")");

				// get content-disposition and content-type
				int c = line.indexOf(':', 0);
				if (c > 0) {
					String key = line.substring(0, c).trim().toLowerCase(Locale.ENGLISH);
					String value = line.substring(c + 1, line.length()).trim();
					headers.put(key, value);
					if (key.equalsIgnoreCase("content-disposition"))
						contentDisposition = value;
					if (key.equalsIgnoreCase("content-type"))
						contentType = value;
					if (key.equals("content-transfer-encoding"))
						contentTransferEncoding = value;
				}
			}

			// Extract content-disposition
			boolean form_data = false;
			if (contentDisposition == null) {
				throw new IOException("Missing content-disposition");
			}

			QuotedStringTokenizer tok = new QuotedStringTokenizer(contentDisposition, ";", false, true);
			String name = null;
			String filename = null;
			while (tok.hasMoreTokens()) {
				String t = tok.nextToken().trim();
				String tl = t.toLowerCase(Locale.ENGLISH);
				if (t.startsWith("form-data"))
					form_data = true;
				else if (tl.startsWith("name="))
					name = value(t);
				else if (tl.startsWith("filename="))
					filename = filenameValue(t);
			}

			// Check disposition
			if (!form_data) {
				continue;
			}
			// It is valid for reset and submit buttons to have an empty
			// name.
			// If no name is supplied, the browser skips sending the info
			// for that field.
			// However, if you supply the empty string as the name, the
			// browser sends the
			// field, with name as the empty string. So, only continue this
			// loop if we
			// have not yet seen a name field.
			if (name == null) {
				continue;
			}

			// Have a new Part
			try (OutputStream part = handler.createPart(name, filename, headers, contentType)) {
				InputStream partInput = null;
				if ("base64".equalsIgnoreCase(contentTransferEncoding)) {
					partInput = new Base64InputStream((ReadLineInputStream) _in);
				} else if ("quoted-printable".equalsIgnoreCase(contentTransferEncoding)) {
					partInput = new FilterInputStream(_in) {
						@Override
						public int read() throws IOException {
							int c = in.read();
							if (c >= 0 && c == '=') {
								int hi = in.read();
								int lo = in.read();
								if (hi < 0 || lo < 0) {
									throw new IOException("Unexpected end to quoted-printable byte");
								}
								char[] chars = new char[] { (char) hi, (char) lo };
								c = Integer.parseInt(new String(chars), 16);
							}
							return c;
						}
					};
				} else
					partInput = _in;

				int state = -2;
				int c;
				boolean cr = false;
				boolean lf = false;

				// loop for all lines
				while (true) {
					int b = 0;
					while ((c = (state != -2) ? state : partInput.read()) != -1) {
						total++;
						if (total > maxRequestSize)
							throw new IllegalStateException("Request exceeds maxRequestSize (" + maxRequestSize + ")");

						state = -2;

						// look for CR and/or LF
						if (c == 13 || c == 10) {
							if (c == 13) {
								partInput.mark(1);
								int tmp = partInput.read();
								if (tmp != 10)
									partInput.reset();
								else
									state = tmp;
							}
							break;
						}

						// Look for boundary
						if (b >= 0 && b < byteBoundary.length && c == byteBoundary[b]) {
							b++;
						} else {
							// Got a character not part of the boundary, so
							// we don't have the boundary marker.
							// Write out as many chars as we matched, then
							// the char we're looking at.
							if (cr)
								part.write(13);

							if (lf)
								part.write(10);

							cr = lf = false;
							if (b > 0)
								part.write(byteBoundary, 0, b);

							b = -1;
							part.write(c);
						}
					}

					// Check for incomplete boundary match, writing out the
					// chars we matched along the way
					if ((b > 0 && b < byteBoundary.length - 2) || (b == byteBoundary.length - 1)) {
						if (cr)
							part.write(13);

						if (lf)
							part.write(10);

						cr = lf = false;
						part.write(byteBoundary, 0, b);
						b = -1;
					}

					// Boundary match. If we've run out of input or we
					// matched the entire final boundary marker, then this
					// is the last part.
					if (b > 0 || c == -1) {

						if (b == byteBoundary.length)
							lastPart = true;
						if (state == 10)
							state = -2;
						break;
					}

					// handle CR LF
					if (cr)
						part.write(13);

					if (lf)
						part.write(10);

					cr = (c == 13);
					lf = (c == 10 || state == 10);
					if (state == 10)
						state = -2;
				}
			}
		}
		if (lastPart) {
			while (line != null)
				line = ((ReadLineInputStream) _in).readLine();
		} else
			throw new IOException("Incomplete parts");
	}

	/* ------------------------------------------------------------ */
	private String value(String nameEqualsValue) {
		int idx = nameEqualsValue.indexOf('=');
		String value = nameEqualsValue.substring(idx + 1).trim();
		return QuotedStringTokenizer.unquoteOnly(value);
	}

	/* ------------------------------------------------------------ */
	private String filenameValue(String nameEqualsValue) {
		int idx = nameEqualsValue.indexOf('=');
		String value = nameEqualsValue.substring(idx + 1).trim();

		if (value.matches(".??[a-z,A-Z]\\:\\\\[^\\\\].*")) {
			// incorrectly escaped IE filenames that have the whole path
			// we just strip any leading & trailing quotes and leave it as is
			char first = value.charAt(0);
			if (first == '"' || first == '\'')
				value = value.substring(1);
			char last = value.charAt(value.length() - 1);
			if (last == '"' || last == '\'')
				value = value.substring(0, value.length() - 1);

			return value;
		} else
			// unquote the string, but allow any backslashes that don't
			// form a valid escape sequence to remain as many browsers
			// even on *nix systems will not escape a filename containing
			// backslashes
			return QuotedStringTokenizer.unquoteOnly(value, true);
	}

	private static class Base64InputStream extends InputStream {
		ReadLineInputStream _in;
		String _line;
		byte[] _buffer;
		int _pos;

		public Base64InputStream(ReadLineInputStream rlis) {
			_in = rlis;
		}

		@Override
		public int read() throws IOException {
			if (_buffer == null || _pos >= _buffer.length) {
				// Any CR and LF will be consumed by the readLine() call.
				// We need to put them back into the bytes returned from this
				// method because the parsing of the multipart content uses them
				// as markers to determine when we've reached the end of a part.
				_line = _in.readLine();
				if (_line == null)
					return -1; // nothing left
				if (_line.startsWith("--"))
					_buffer = (_line + "\r\n").getBytes(); // boundary marking
															// end of part
				else if (_line.length() == 0)
					_buffer = "\r\n".getBytes(); // blank line
				else {
					ByteArrayOutputStream baos = new ByteArrayOutputStream((4 * _line.length() / 3) + 2);
					B64Code.decode(_line, baos);
					baos.write(13);
					baos.write(10);
					_buffer = baos.toByteArray();
				}

				_pos = 0;
			}

			return _buffer[_pos++];
		}
	}

	public InMemoryMultiPartInputStreamParser setMaxRequestSize(long maxRequestSize) {
		this.maxRequestSize = maxRequestSize;
		return this;
	}
}

/*
 * Created on Feb 19, 2005
 *
 */
package com.aptana.editor.html;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import com.aptana.editor.common.CompositePartitionScanner;
import com.aptana.editor.common.DocumentContentTypeManager;
import com.aptana.editor.common.ExtendedFastPartitioner;
import com.aptana.editor.common.NullPartitionerSwitchStrategy;
import com.aptana.editor.common.NullSubPartitionScanner;
import com.aptana.editor.css.CSSSourceConfiguration;
import com.aptana.editor.js.JSSourceConfiguration;

/**
 * @author Chris
 * @author Sandip
 */
public class HTMLSourcePartitionScannerTest extends TestCase
{

	private ExtendedFastPartitioner partitioner;

	private void assertContentType(String contentType, String code, int offset)
	{
		assertEquals("Content type doesn't match expectations for: " + code.charAt(offset), contentType,
				getContentType(code, offset));
	}

	@Override
	protected void tearDown() throws Exception
	{
		partitioner = null;
		super.tearDown();
	}

	private String getContentType(String content, int offset)
	{
		if (partitioner == null)
		{
			IDocument document = new Document(content);
			CompositePartitionScanner partitionScanner = new CompositePartitionScanner(HTMLSourceConfiguration
					.getDefault().createSubPartitionScanner(), new NullSubPartitionScanner(),
					new NullPartitionerSwitchStrategy());
			partitioner = new ExtendedFastPartitioner(partitionScanner, HTMLSourceConfiguration.getDefault()
					.getContentTypes());
			partitionScanner.setPartitioner(partitioner);
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
			DocumentContentTypeManager.getInstance().setDocumentContentType(document, IHTMLConstants.CONTENT_TYPE_HTML);
			DocumentContentTypeManager.getInstance().registerConfiguration(document,
					HTMLSourceConfiguration.getDefault());
		}
		return partitioner.getContentType(offset);
	}

	// TODO Add tests for script/style/tag/cdata/doctype/default

	public void testPartitioningOfCommentSpanningSingleLine()
	{
		String source = "<!-- This is HTML comment on one Line -->\n";

		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 0);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 1);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 2);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 3);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 4);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 5);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 37);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 38);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 39);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 40);
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 41);
	}

	public void testPartitioningOfCommentSpanningMultipleLines()
	{
		String source = "<!-- This is HTML comment\nspanning multiple lines -->\n";

		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 0);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 1);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 2);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 3);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 4);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 5);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 48);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 49);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 50);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 51);
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 52);
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 53);
	}

	public void testAllPartitionTypes()
	{
		String source = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n"
				+ "<html><head>\n"
				+ "<style>html {color: red;}</style>\n"
				+ "<script>var one = 1;</script>\n"
				+ "</head><body>\n" + "<!-- This is an HTML comment -->\n" + "<p>Text</p>\n" + "</body></html>";
		// DOCTYPE
		assertContentType(HTMLSourceConfiguration.HTML_DOCTYPE, source, 0);
		// html tag
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 91); // '<'html
		// Style tag
		assertContentType(HTMLSourceConfiguration.HTML_STYLE, source, 104); // '<'style>
		assertContentType(HTMLSourceConfiguration.HTML_STYLE, source, 110); // style'>'html
		// Inlined CSS
		assertContentType(CSSSourceConfiguration.DEFAULT, source, 111); // style>'h'tml
		assertContentType(CSSSourceConfiguration.DEFAULT, source, 128); // '}'
		// End style tag
		assertContentType(HTMLSourceConfiguration.HTML_STYLE, source, 104); // '<'/style>
		assertContentType(HTMLSourceConfiguration.HTML_STYLE, source, 110); // /style'>'
		// script tag
		assertContentType(HTMLSourceConfiguration.HTML_SCRIPT, source, 138); // '<'script>
		assertContentType(HTMLSourceConfiguration.HTML_SCRIPT, source, 145); // <script'>'
		// Inlined JS
		assertContentType(JSSourceConfiguration.DEFAULT, source, 146); // <script>'v'
		assertContentType(JSSourceConfiguration.DEFAULT, source, 157); // ';'</script>
		// HTML Comment
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 182); // '<'!--
		assertContentType(HTMLSourceConfiguration.HTML_COMMENT, source, 213); // --'>'
		// Text inside tag
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 218); // <p>'T'ext
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 221); // <p>Tex't'</p>
		// Close p tag
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 222); // Text'<'/p>
	}

}

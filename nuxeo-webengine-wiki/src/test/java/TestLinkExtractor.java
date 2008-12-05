import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import junit.framework.TestCase;

import org.nuxeo.ecm.wiki.listener.WikiHelper;
import org.nuxeo.ecm.wiki.listener.WordExtractor;
import org.wikimodel.wem.WikiParserException;
import org.wikimodel.wem.common.CommonWikiParser;

public class TestLinkExtractor extends TestCase{

    public void testOne() throws WikiParserException{
        InputStream in = TestLinkExtractor.class.getClassLoader().getResourceAsStream("test.txt");
        CommonWikiParser parser = new CommonWikiParser();
        StringBuffer sb = new StringBuffer();
        parser.parse(new InputStreamReader(in), new WordExtractor(sb));
        System.out.println(sb.toString());

        List<String> workLinks = WikiHelper.getWordLinks(sb.toString());
        assertEquals(2, workLinks.size());
        assertEquals("WikiName.PageName", workLinks.get(0));
        assertEquals("PageParsing", workLinks.get(1));
    }

}

package tk.skuro.idea.orgmode.parser;

import com.intellij.lexer.LexerPosition;
import com.intellij.psi.tree.IElementType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LexerTest {

    private volatile OrgLexer lexer;

    @Before
    public void setup() {
        lexer = new OrgLexer();
    }

    @Test
    public void canReadComments() {
        final String comment = "# I'm a comment";
        lexer.start(comment);

        assertEquals("Comment not properly parsed", OrgTokenTypes.COMMENT, lexer.getTokenType());
    }

    @Test
    public void canReadCommentsWithHeadingWhitespaces() {
        final String comment = "  # I'm a comment";
        lexer.start(comment);

        assertEquals("Comment not properly parsed", OrgTokenTypes.COMMENT, lexer.getTokenType());
    }

    @Test
    public void canReadOutlines() {
        final String outlines =
                "* I'm an outline\n" +
                "** I'm a second outline";
        lexer.start(outlines);
        assertEquals("Outline not properly parsed", OrgTokenTypes.OUTLINE, lexer.getTokenType());

        eatWhitespace();

        lexer.advance();
        assertEquals("Outline not properly parsed", OrgTokenTypes.OUTLINE, lexer.getTokenType());
    }

    @Test
    public void canReadBlocks() {
        final String block =
                "#+BEGIN_SRC\n" +
                "(defn foobar)\n" +
                "#+END_SRC";

        lexer.start(block);
        assertEquals("Block start not properly parsed", OrgTokenTypes.BLOCK_DELIMITER, lexer.getTokenType());

        eatWhitespace();

        lexer.advance();
        assertEquals("Block content not properly parsed", OrgTokenTypes.BLOCK_CONTENT, lexer.getTokenType());

        eatBlockContent();
        eatWhitespace();

        lexer.advance();
        assertEquals("Block end not properly parsed", OrgTokenTypes.BLOCK_DELIMITER, lexer.getTokenType());
    }

    @Test
    public void canReadKeyword(){
        final String keyword = "#+FOOBAR: foobar all the way down";

        lexer.start(keyword);
        assertEquals("Keyword not properly parsed", OrgTokenTypes.KEYWORD, lexer.getTokenType());
    }

    @Test
    public void canReadKeywordWithHeadingWhitespace(){
        final String keyword = "   #+FOOBAR: foobar all the way down";

        lexer.start(keyword);
        assertEquals("Keyword not properly parsed", OrgTokenTypes.KEYWORD, lexer.getTokenType());
    }

    @Test
    public void canReadUnderline(){
        final String underlined = "_Ima underline text_";

        lexer.start(underlined);
        assertEquals("Underline not properly parsed", OrgTokenTypes.UNDERLINE, lexer.getTokenType());
        assertEquals("Underline not properly parsed", underlined, lexer.getTokenText());
    }

    @Test
    public void canReadBold(){
        final String underlined = "*Ima bold text*";

        lexer.start(underlined);
        assertEquals("Underline not properly parsed", OrgTokenTypes.BOLD, lexer.getTokenType());
        assertEquals("Underline not properly parsed", underlined, lexer.getTokenText());
    }

    @Test
    public void canReadProperties(){
        final String properties =
                "    :PROPERTIES:\n" +
                "       :TEST: foo\n" +
                "    :END:";

        lexer.start(properties);
        assertEquals("Properties block start not properly parsed", OrgTokenTypes.DRAWER_DELIMITER, lexer.getTokenType());

        eatWhitespace();

        lexer.advance();
        assertEquals("Properties block content not properly parsed", OrgTokenTypes.DRAWER_CONTENT, lexer.getTokenType());

        eatPropertiesContent();
        eatWhitespace();

        lexer.advance();
        assertEquals("Properties block end not properly parsed", OrgTokenTypes.DRAWER_DELIMITER, lexer.getTokenType());
    }

    private void eatWhitespace() {
        lexer.advance();
    }

    /**
     * Eats all characters inside a block
     */
    private void eatBlockContent() {
        eatUntil(OrgTokenTypes.BLOCK_CONTENT);
    }

    private void eatPropertiesContent(){
        eatUntil(OrgTokenTypes.DRAWER_CONTENT);
    }

    private void eatUntil(final IElementType stop) {
        LexerPosition previous = lexer.getCurrentPosition();
        while(lexer.getTokenType() == stop) {
            previous = lexer.getCurrentPosition();
            lexer.advance();
        }

        lexer.restore(previous); // do not eat the trailing token after the block content
    }

}

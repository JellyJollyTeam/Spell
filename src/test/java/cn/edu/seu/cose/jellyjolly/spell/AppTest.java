package cn.edu.seu.cose.jellyjolly.spell;

import cn.edu.seu.cose.jellyjolly.spell.parser.SpellParserImpl;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AppTest extends TestCase {

    private static final String SAMPLE_PATH =
            "cn/edu/seu/cose/jellyjolly/spell/sample.spell";
    private static final String CASE1_PATH =
            "cn/edu/seu/cose/jellyjolly/spell/case1.spell";
    private final Class<?>[] SAMPLE_CLZS = {
            QuizTitle.class,
            QuizText.class,
            SingleChoice.class,
            MultipleChoice.class,
            SingleTextbox.class,
            MultipleTextbox.class,
            QuizTitle.class,
            SingleChoice.class,
            MultipleChoice.class,
            SingleTextbox.class,
            MultipleTextbox.class
    };
    private final Object[] SAMPLE_VALS = {
            "Part 1",
            "This is a comment.",
            "What's your favorite color?",
            "What's your hobby?",
            "What's your name?",
            "Introduce yourself",
            "Part 2",
            "Lorem ipsum dolor sit amet, consectetur adipisicing elit, "
                    + "sed do eiusmod tempor incididun.",
            "Consectetur adipisicing elit",
            "Sed ut oersoucuatus unde omnis natus error.",
            "Sit voluptatem accusanitium doloremque."
    };
    public AppTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    public void testPrintCase1() throws IOException {
        /*
        System.out.println("testPrintCase1");
        SpellParser parser = new SpellParserImpl();
        String source = getSourceFromClassPath(SAMPLE_PATH);
        Quiz quiz = parser.getQuiz(source);
        QuizVisitor quizVisitor = new QuizPrinter();
        quiz.accept(quizVisitor);
        */
    }

    public void testSpellParser() throws IOException {
        System.out.println("testSpellParser");
        SpellParser parser = new SpellParserImpl();
        String source = getSourceFromClassPath(SAMPLE_PATH);
        Quiz quiz = parser.getQuiz(source);
        testQuiz(quiz);
    }

    private void testQuiz(Quiz quiz) {
        QuizTester tester = new QuizTester(SAMPLE_CLZS, SAMPLE_VALS);
        quiz.accept(tester);
        tester.finish();
    }

    private String getSourceFromClassPath(String path) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream in = AppTest.class.getClassLoader()
                .getResourceAsStream( path);
        byte[] buffer = new byte[1024];
        while (true) {
            int len = in.read(buffer);
            if (len <= 0) {
                break;
            }
            baos.write(buffer, 0, len);
        }
        byte[] byteArr = baos.toByteArray();
        String source = new String(byteArr, Charset.forName("UTF-8"));
        return source;
    }
}

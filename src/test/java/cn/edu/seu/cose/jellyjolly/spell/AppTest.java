package cn.edu.seu.cose.jellyjolly.spell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AppTest extends TestCase {

    public AppTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    public void testSpellParser() throws IOException {
        SpellParser parser = new MockSpellParser(); // replace with the concrete spell parser here
        String source = getSourceFromClassPath();
        Quiz quiz = parser.getQuiz(source);
        testQuiz(quiz);
    }

    private void testQuiz(Quiz quiz) {
        QuizTester tester = new QuizTester();
        quiz.accept(tester);
    }

    private String getSourceFromClassPath() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream in = AppTest.class.getClassLoader().getResourceAsStream(
                "cn/edu/seu/cose/jellyjolly/spell/sample.spell");
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
        System.out.println("Source:");
        System.out.println();
        System.out.println(source);
        System.out.println();
        return source;
    }
}

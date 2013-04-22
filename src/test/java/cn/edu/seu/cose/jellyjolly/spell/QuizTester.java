/*
 * The MIT License
 *
 * Copyright 2013 Jelly Jolly Team.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cn.edu.seu.cose.jellyjolly.spell;

import junit.framework.Assert;

/**
 *
 * @author rAy <predator.ray@gmail.com>
 */
public class QuizTester implements QuizVisitor {

    private final Class<?>[] clzs = {
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
    private final Object[] values = {
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
    private int index = 0;

    public void visit(QuizTitle quizTitle) {
        Assert.assertEquals(QuizTitle.class, clzs[index]);
        Assert.assertEquals(quizTitle.getText(), values[index]);
        ++index;
    }

    public void visit(QuizText quizText) {
        Assert.assertEquals(QuizText.class, clzs[index]);
        Assert.assertEquals(quizText.getText(), values[index]);
        ++index;
    }

    public void visit(SingleChoice singleChoice) {
        Assert.assertEquals(SingleChoice.class, clzs[index]);
        Assert.assertEquals(singleChoice.getTitle(), values[index]);
        ++index;
    }

    public void visit(MultipleChoice multipleChoice) {
        Assert.assertEquals(MultipleChoice.class, clzs[index]);
        Assert.assertEquals(multipleChoice.getTitle(), values[index]);
        ++index;
    }

    public void visit(SingleTextbox singleTextbox) {
        Assert.assertEquals(SingleTextbox.class, clzs[index]);
        Assert.assertEquals(singleTextbox.getTitle(), values[index]);
        ++index;
    }

    public void visit(MultipleTextbox multipleTextbox) {
        Assert.assertEquals(MultipleTextbox.class, clzs[index]);
        Assert.assertEquals(multipleTextbox.getTitle(), values[index]);
        ++index;
    }

    public void visit(Quiz quiz) {
        Assert.fail(String.format("it should be unreachable here: "
                + "public void visit(Quiz quiz)\n"
                + "Class: %s",
                quiz.getClass().getName()));
    }

    public void visit(QuizElement quizElement) {
        Assert.fail(String.format("it should be unreachable here: "
                + "public void visit(QuizElement quizElement)\n"
                + "Class: %s",
                quizElement.getClass().getName()));
    }
}

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

/**
 *
 * @author rAy <predator.ray@gmail.com>
 */
class MockSpellParser implements SpellParser {

    public Quiz getQuiz(CharSequence charSequence) {
        Quiz quiz = new Quiz();

        quiz.addQuizElement(new QuizTitle("Part 1"));

        quiz.addQuizElement(new QuizText("This is a comment."));

        SingleChoice sc1 = new SingleChoice();
        sc1.setTitle("What's your favorite color?");
        sc1.setOptions(new String[]{"Red", "Yellow", "Blue", "Green"});
        sc1.setDefaultIndex(0);
        quiz.addQuizElement(sc1);

        MultipleChoice mc1 = new MultipleChoice();
        mc1.setTitle("What's your hobby?");
        mc1.setOptions(new String[]{
            "Soccer", "Basketball", "Badminton", "Others"
        });
        mc1.setDefaultIndexes(new Integer[]{0, 2});
        quiz.addQuizElement(mc1);

        SingleTextbox st1 = new SingleTextbox();
        st1.setTitle("What's your name?");
        quiz.addQuizElement(st1);

        MultipleTextbox mt1 = new MultipleTextbox();
        mt1.setTitle("Introduce yourself");
        quiz.addQuizElement(mt1);

        quiz.addQuizElement(new QuizTitle("Part 2"));

        SingleChoice sc2 = new SingleChoice();
        sc2.setTitle("Lorem ipsum dolor sit amet, consectetur adipisicing elit,"
                + " sed do eiusmod tempor incididun.");
        sc2.setOptions(new String[]{"Lorem", "Ipsum", "Dolor", "Sit"});
        quiz.addQuizElement(sc2);

        MultipleChoice mc2 = new MultipleChoice();
        mc2.setTitle("Consectetur adipisicing elit");
        mc2.setOptions(new String[]{"Lorem", "Ipsum", "Dolor", "Sit"});
        quiz.addQuizElement(mc2);

        SingleTextbox st2 = new SingleTextbox();
        st2.setTitle("Sed ut oersoucuatus unde omnis natus error.");
        st2.setDefaultValue("lorem ipsum");
        quiz.addQuizElement(st2);

        MultipleTextbox mt2 = new MultipleTextbox();
        mt2.setTitle("Sit voluptatem accusanitium doloremque.");
        mt2.setDefaultValue("Consectetur adipisicing\noersoucuatus unde omnis");
        quiz.addQuizElement(mt2);

        return quiz;
    }
}

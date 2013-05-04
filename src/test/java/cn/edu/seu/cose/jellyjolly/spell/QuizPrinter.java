package cn.edu.seu.cose.jellyjolly.spell;

import junit.framework.Assert;

import static java.lang.System.out;
/**
 * Copyright (c) 2013 Ray <predator.ray@gmail.com>
 */
public class QuizPrinter implements QuizVisitor {

    @Override
    public void visit(QuizTitle quizTitle) {
        out.println("quiz title");
        out.println(quizTitle.getText());
        out.println();
    }

    @Override
    public void visit(QuizText quizText) {
        out.println("quiz text");
        out.println(quizText.getText());
        out.println();
    }

    @Override
    public void visit(SingleChoice singleChoice) {
        out.println("single choice");
        out.println(singleChoice.getTitle());
        out.println("options:");
        for (String options : singleChoice.getOptions()) {
            out.println(options);
        }
        out.println("default:");
        out.println(singleChoice.getDefaultIndex());
        out.println();
    }

    @Override
    public void visit(MultipleChoice multipleChoice) {
        out.println("multiple choice");
        out.println(multipleChoice.getTitle());
        out.println("options:");
        for (String options : multipleChoice.getOptions()) {
            out.println(options);
        }
        out.println("default:");
        for (int index : multipleChoice.getDefaultIndices()) {
            out.print(index + " ");
        }
        out.println();
        out.println();
    }

    @Override
    public void visit(SingleTextbox singleTextbox) {
        out.println("single textbox");
        out.println(singleTextbox.getTitle());
        out.println("default: " + singleTextbox.getDefaultValue());
        out.println();
    }

    @Override
    public void visit(MultipleTextbox multipleTextbox) {
        out.println("multiple textbox");
        out.println(multipleTextbox.getTitle());
        out.println("default: " + multipleTextbox.getDefaultValue());
        out.println();
    }

    @Override
    public void visit(Quiz quiz) {
        Assert.fail("public void visit(Quiz quiz) is not reachable");
    }

    @Override
    public void visit(QuizElement quizElement) {
        Assert.fail("public void visit(QuizElement quizElement) is not reachable");
    }
}

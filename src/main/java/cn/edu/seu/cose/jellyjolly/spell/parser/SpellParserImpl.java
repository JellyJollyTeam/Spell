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
package cn.edu.seu.cose.jellyjolly.spell.parser;

import cn.edu.seu.cose.jellyjolly.spell.MultipleChoice;
import cn.edu.seu.cose.jellyjolly.spell.MultipleTextbox;
import cn.edu.seu.cose.jellyjolly.spell.Quiz;
import cn.edu.seu.cose.jellyjolly.spell.QuizText;
import cn.edu.seu.cose.jellyjolly.spell.QuizTitle;
import cn.edu.seu.cose.jellyjolly.spell.SingleChoice;
import cn.edu.seu.cose.jellyjolly.spell.SingleTextbox;
import cn.edu.seu.cose.jellyjolly.spell.SpellParser;
import cn.edu.seu.cose.jellyjolly.spell.parser.Interpretation.Description;
import cn.edu.seu.cose.jellyjolly.spell.parser.Interpretation.Option;
import cn.edu.seu.cose.jellyjolly.spell.parser.Interpretation.TextInput;
import cn.edu.seu.cose.jellyjolly.spell.parser.Interpretation.Token;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zc <cottyard@gmail.com>
 */
public class SpellParserImpl implements SpellParser{
    private Quiz quiz = new Quiz();
    Token t = null;
    Description tbuffer = null;
    Interpretation interpretation = null;
    public Quiz getQuiz(CharSequence charSequence) {
        interpretation = new Interpretation(charSequence);
        t = interpretation.getNextToken();
        while(t!=null){
            if(t.getClass().equals(Option.class)){
                parseOptions();
            } else if(t.getClass().equals(Interpretation.TextInput.class)){
                parseInputs();
            } else if(t.getClass().equals(Interpretation.Section.class)){
                parseSection();
            } else if(t.getClass().equals(Interpretation.Description.class)){
                parseDescription();
            }
        }
        return quiz;
    }
    private void parseDescription(){//QuizText
        if(tbuffer!=null){
            quiz.addQuizElement(new QuizText(tbuffer.getContent()));
        }
        tbuffer = (Description)t;
        t = interpretation.getNextToken();
    }
    private void parseSection(){//QuizTitle
        if(tbuffer!=null){
            quiz.addQuizElement(new QuizTitle(tbuffer.getContent()));
            tbuffer = null;
        }
        t = interpretation.getNextToken();
    }
    private void parseOptions(){//SingleChoice/MultipleChoice
        if(tbuffer!=null){
            Option op = (Option)t;
            if(op.isSingle()){
                constructSingleChoice(op);
            } else {
                constructMultipleChoice(op);
            }
        }
    }
    private void constructSingleChoice(Option op){
        SingleChoice sc = new SingleChoice();
        sc.setTitle(tbuffer.getContent());
        tbuffer = null;
        List<String> options = new ArrayList<String>();
        int defaultIndex = -1;
        do{
            options.add(op.getContent());
            if(op.isDefault()){
                defaultIndex = options.size()-1;
            }
            t = interpretation.getNextToken();
            if(t==null)break;
            if(!t.getClass().equals(Option.class))break;
            op = (Option)t;
            if(!op.isSingle())break;
        }while(true);

        sc.setOptions(options.toArray(new String[0]));
        if(defaultIndex>=0){
            sc.setDefaultIndex(defaultIndex);
        }
        quiz.addQuizElement(sc);
    }
    private void constructMultipleChoice(Option op){
        MultipleChoice mc = new MultipleChoice();
        mc.setTitle(tbuffer.getContent());
        tbuffer = null;
        List<String> options = new ArrayList<String>();
        List<Integer> defaultIndexes = new ArrayList<Integer>();
        do{
            options.add(op.getContent());
            if(op.isDefault()){
                defaultIndexes.add(options.size()-1);
            }
            t = interpretation.getNextToken();
            if(t==null)break;
            if(!t.getClass().equals(Option.class))break;
            op = (Option)t;
            if(op.isSingle())break;
        }while(true);

        mc.setOptions(options.toArray(new String[0]));
        mc.setDefaultIndexes(defaultIndexes.toArray(new Integer[0]));
        quiz.addQuizElement(mc);
    }
    private void parseInputs(){
        if(tbuffer!=null){
            TextInput input = (TextInput)t;
            StringBuilder defaultValue = new StringBuilder();
            int inputs = 0;
            do{
                inputs++;
                if(input.hasDefaultValue()){
                    defaultValue.append(input.getDefaultValue());
                }
                t = interpretation.getNextToken();
            }while(t!=null&&t.getClass().equals(TextInput.class));
            
            if(inputs==1){
                SingleTextbox st = new SingleTextbox();
                st.setTitle(tbuffer.getContent());
                st.setDefaultValue(defaultValue.toString());
                quiz.addQuizElement(st);
            }else{
                MultipleTextbox mt = new MultipleTextbox();
                mt.setTitle(tbuffer.getContent());
                mt.setDefaultValue(defaultValue.toString());
                quiz.addQuizElement(mt);
            }
            tbuffer = null;
        }
    }
}

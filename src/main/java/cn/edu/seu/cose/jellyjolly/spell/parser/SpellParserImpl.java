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
import cn.edu.seu.cose.jellyjolly.spell.QuizElement;
import cn.edu.seu.cose.jellyjolly.spell.QuizText;
import cn.edu.seu.cose.jellyjolly.spell.QuizTitle;
import cn.edu.seu.cose.jellyjolly.spell.SingleChoice;
import cn.edu.seu.cose.jellyjolly.spell.SingleTextbox;
import cn.edu.seu.cose.jellyjolly.spell.SpellParser;
import cn.edu.seu.cose.jellyjolly.spell.parser.Interpretation.OptionToken;
import cn.edu.seu.cose.jellyjolly.spell.parser.Interpretation.TextToken;
import cn.edu.seu.cose.jellyjolly.spell.parser.Interpretation.Token;
import cn.edu.seu.cose.jellyjolly.spell.parser.Interpretation.Token.Tag;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zc <cottyard@gmail.com>
 */
public class SpellParserImpl implements SpellParser{
    private Quiz quiz = new Quiz();
    Token peek = null;
    Interpretation interpretation = null;
    ParsingTree parsingTree = new ParsingTree();
    /*
    TokenParser parser = new TokenParser();*/
    public Quiz getQuiz(CharSequence charSequence) {
        interpretation = new Interpretation(charSequence);
        QuizElement qe;
        peek = interpretation.getNextToken();
        while(peek!=null){
            /*
            System.out.println(peek.tag);
            if(peek.tag==Tag.TEXT){
                System.out.println("text: "+((TextToken)peek).lexeme);
            }
            peek = interpretation.getNextToken();
            * */
            qe = parse(parsingTree.root,null,peek);
            if(qe!=null){
                quiz.addQuizElement(qe);
            }
        }
        return quiz;
    }
    //parse利用ParsingTree解析Token，生成中间表示
    //对于能够识别的Token组合，返回对应的QuizElement中间表示
    //不能解析的Token则返回其原有字符串构成的QuizText对象
    
    //每个Token都有一个lexeme，是它原来的字符表示
    //解析过程中Token的lexeme被保存到parsingContext中，
    //调用各节点failure函数时，如果满足构造条件就返回对应的QuizElement对象
    //如果不满足构造条件，返回null
    //此时parse函数就使用parsingContext中的数据构造QuizText作为返回值
    public QuizElement parse(TagNode node, Token current, Token next){
        if(node!=null)
            System.out.println("\n\nparsing node "+node.tag);
        if(current!=null){
            System.out.println("current token "+current.tag);
            if(current.tag == Tag.TEXT){
                System.out.println("with text: " + ((TextToken)current).lexeme);
            }
        }
        if(next!=null)
            System.out.println("next token "+next.tag);
        /////////////////////////////////////////////////////////
        if(current!=null){
            parsingTree.context.lexeme.append(current.lexeme);
        }
        node.contextFn.visit(parsingTree.context, current);
        System.out.println("visit finished");
        if(next!=null){
            for(TagNode child : node.children){
                if(child.tag == next.tag){
                    return parse(child, next, interpretation.getNextToken());
                }
            }
        }
        System.out.println("node "+node.tag+" failed");
        
        peek = next;
        if(node.tag==null){//失败发生在根节点，就有跳过Token的特殊情况发生
            peek = interpretation.getNextToken();
            parsingTree.context.lexeme.append(next.lexeme);
        }
        QuizElement ret = node.failureFn.fail(parsingTree.context);
        if(ret==null){
            String s = parsingTree.context.lexeme.toString().trim();
            if(!s.equals("")){
                ret = new QuizText(s);
            }
        }
        return ret;
    }
    public interface FailureFn{
        QuizElement fail(ParsingContext context);
    }
    public interface ContextFn{
        //访问节点时也可能发生failure，visit返回false则failure，
        //此时调用同一节点上的failure函数
        boolean visit(ParsingContext context,Token token);
    }
    public class TagNode{
        public Tag tag;
        public TagNode(Tag t){
            tag = t;
        }
        public List<TagNode> children = new ArrayList<TagNode>();
        public void addChild(TagNode c){
            children.add(c);
        }
        public FailureFn failureFn = new FailureFn(){
            public QuizElement fail(ParsingContext context) {
                return null;
            }
        };
        public ContextFn contextFn = new ContextFn(){
            public boolean visit(ParsingContext context,Token t) {
                return true;
            }
        };
    }
    public class ParsingContext{
        //Lexeme
        StringBuilder lexeme = new StringBuilder();
        //TEXT
        TextToken tBuffer;
        //SingleChoice/MultipleChoice
        Boolean isSingle = null;
        List<String> options = new ArrayList<String>();
        List<Integer> defaultIndexes = new ArrayList<Integer>();
        //SingleTextbox/MultipleTextbox
        int inputs = 0;
        StringBuilder defaultValue = new StringBuilder();
        
        public void clearAll(){
            lexeme.delete(0,lexeme.length());
            tBuffer = null;
            isSingle = null;
            options.clear();
            defaultIndexes.clear();
            inputs = 0;
            defaultValue.delete(0,defaultValue.length());
        }
    }
    public class ParsingTree{
        TagNode root = new TagNode(null);
        ParsingContext context = new ParsingContext();
        public ParsingTree(){
            //自下向上构造文法树
            
            //构造SingleTextbox/MultipleTextbox的子树
            TagNode textBox_INPUT = new TagNode(Tag.INPUT);
            TagNode textBox_TEXT = new TagNode(Tag.TEXT);
            TagNode textBox_NEWLINE = new TagNode(Tag.NEWLINE);
            
            textBox_INPUT.addChild(textBox_NEWLINE);
            textBox_INPUT.addChild(textBox_TEXT);
            textBox_NEWLINE.addChild(textBox_INPUT);
            textBox_TEXT.addChild(textBox_NEWLINE);
            
            textBox_INPUT.contextFn = new ContextFn(){
                public boolean visit(ParsingContext context,Token token) {
                    context.inputs++;
                    return true;
                }
            };
            textBox_TEXT.contextFn = new ContextFn(){
                public boolean visit(ParsingContext context,Token token) {
                    StringBuilder dv = new StringBuilder(
                            ((TextToken)token).lexeme);
                    while(dv.charAt(dv.length()-1)=='_'){
                        dv.deleteCharAt(dv.length()-1);
                    }
                    if(dv.length()>0){
                        context.defaultValue.append(" ");
                    }
                    context.defaultValue.append(dv);
                    return true;
                }
            };
            textBox_NEWLINE.failureFn = new FailureFn(){
                public QuizElement fail(ParsingContext context) {
                    if(context.inputs>1){
                        MultipleTextbox mt = new MultipleTextbox();
                        mt.setTitle(context.tBuffer.lexeme);
                        mt.setDefaultValue(context.defaultValue.toString());
                        return mt;
                    } else {
                        SingleTextbox st = new SingleTextbox();
                        st.setTitle(context.tBuffer.lexeme);
                        st.setDefaultValue(context.defaultValue.toString());
                        return st;
                    }
                }
            };
            textBox_INPUT.failureFn = textBox_TEXT.failureFn
                    = textBox_NEWLINE.failureFn;
            /////////////////////////////////////////////////////////////////
            //构造SingleChoice/MultipleChoice的子树
            TagNode choice_OPTION = new TagNode(Tag.OPTION);
            TagNode choice_TEXT = new TagNode(Tag.TEXT);
            TagNode choice_NEWLINE = new TagNode(Tag.NEWLINE);
            
            choice_OPTION.addChild(choice_NEWLINE);
            choice_OPTION.addChild(choice_TEXT);
            choice_TEXT.addChild(choice_NEWLINE);
            choice_NEWLINE.addChild(choice_OPTION);
            choice_NEWLINE.addChild(choice_TEXT);
            
            choice_OPTION.contextFn = new ContextFn(){
                public boolean visit(ParsingContext context,Token token) {
                    OptionToken ot = (OptionToken)token;
                    if(context.isSingle==null){
                        context.isSingle = ot.isSingle;
                    }
                    if(ot.isDefault){
                        context.defaultIndexes.add(context.options.size());
                    }
                    context.options.add("");
                    return true;
                }
            };
            choice_OPTION.failureFn = new FailureFn(){
                public QuizElement fail(ParsingContext context){
                    if(context.isSingle){
                        SingleChoice sc = new SingleChoice();
                        sc.setTitle(context.tBuffer.lexeme);
                        sc.setOptions(context.options.toArray(new String[0]));
                        if(context.defaultIndexes.size()>0){
                            sc.setDefaultIndex(context.defaultIndexes.get(0));
                        }
                        return sc;
                    } else {
                        MultipleChoice mc = new MultipleChoice();
                        mc.setTitle(context.tBuffer.lexeme);
                        mc.setOptions(context.options.toArray(new String[0]));
                        mc.setDefaultIndexes(
                            context.defaultIndexes.toArray(new Integer[0]));
                        return mc;
                    }
                }
            };
            choice_TEXT.contextFn = new ContextFn(){
                public boolean visit(ParsingContext context,Token token) {
                    String s = context.options.get(context.options.size()-1);
                    if(!s.equals("")){
                        s += " ";
                    }
                    s += ((TextToken)token).lexeme;
                    context.options.set(context.options.size()-1, s);
                    return true;
                }
            };
            choice_NEWLINE.failureFn = 
                    choice_TEXT.failureFn = choice_OPTION.failureFn;
            /////////////////////////////////////////////////////////
            //构造INDEX-TEXT-NEWLINE主干
            TagNode question_INDEX = new TagNode(Tag.INDEX);
            TagNode question_TEXT = new TagNode(Tag.TEXT);
            TagNode question_NEWLINE = new TagNode(Tag.NEWLINE);
            
            question_INDEX.addChild(question_TEXT);
            question_TEXT.addChild(question_NEWLINE);
            question_NEWLINE.addChild(question_TEXT);
            question_NEWLINE.addChild(choice_OPTION);
            question_NEWLINE.addChild(textBox_INPUT);
            
            question_TEXT.contextFn = new ContextFn(){
                public boolean visit(ParsingContext context,Token token){
                    if(context.tBuffer == null){
                        context.tBuffer = (TextToken)token;
                    } else {
                        context.tBuffer.lexeme += " "+((TextToken)token).lexeme;
                    }
                    return true;
                }
            };
            ///////////////////////////////////////////////
            //构造QuizTitle分支
            TagNode title_TEXT = new TagNode(Tag.TEXT);
            TagNode title_NEWLINE = new TagNode(Tag.NEWLINE);
            TagNode title_SECTION = new TagNode(Tag.SECTION);
            
            title_TEXT.addChild(title_NEWLINE);
            title_NEWLINE.addChild(title_TEXT);
            title_NEWLINE.addChild(title_SECTION);
            
            title_TEXT.contextFn = question_TEXT.contextFn;
            
            title_SECTION.failureFn = new FailureFn(){
                public QuizElement fail(ParsingContext context) {
                    return new QuizTitle(context.tBuffer.lexeme);
                }
            };
            /////////////////////////////////////////////////////////
            //构造树根
            root.addChild(title_TEXT);
            root.addChild(question_INDEX);
            root.contextFn = new ContextFn(){
                public boolean visit(ParsingContext context, Token token) {
                    context.clearAll();
                    return true;
                }
            };
            //////////////////////////////////////////////////////////////////
        }
    }
}

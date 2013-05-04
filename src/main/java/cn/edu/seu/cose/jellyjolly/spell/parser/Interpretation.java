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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author zc <cottyard@gmail.com>
 */
public class Interpretation {
    private List<Token> tokenList = new ArrayList<Token>();
    private Iterator<Token> iterator;
    private int index = 0;
    private CharSequence source;
    public Interpretation(CharSequence s){
        source = s;
        Token k;
        while((k = scan())!=null){
            tokenList.add(k);
        }
        iterator = tokenList.iterator();
        System.out.println(tokenList.size()+" tokens created");
    }
    public Token getNextToken(){
        return iterator.hasNext() ? iterator.next() : null;
    }
    private Token scan(){
        if(index>=source.length()){
            return null;
        }
        char peek;
        //忽略token之间的空字符
        do{
            peek = source.charAt(index++);
        }while(peek==' '||peek=='\t');
        
        switch(peek){
            case '\n':
                return new NewlineToken();
            case '-':
                index = locate(new Locater(){
                            public boolean pass(char c){
                                return c=='-';
                            }
                        });
                return new SectionToken();
            case '(':{
                int rightBorder = locate(new Locater(){
                    public boolean pass(char c){
                        return c!=')'&&c!='\n';
                    }
                });
                if(source.charAt(rightBorder)==')'){
                    boolean isDefault = index!=rightBorder;
                    index = rightBorder + 1;
                    return new OptionToken(true,isDefault);
                }
            }
            case '[':{
                int rightBorder = locate(new Locater(){
                    public boolean pass(char c){
                        return c!=']'&&c!='\n';
                    }
                });
                if(source.charAt(rightBorder)==']'){
                    boolean isDefault = index!=rightBorder;
                    index = rightBorder + 1;
                    return new OptionToken(false,isDefault);
                }
            }
            case '_':
                index = locate(new Locater(){
                            public boolean pass(char c){
                                return c=='_';
                            }
                        });
                return new InputToken();
        }
        //IndexToken，由数字+'.'组成
        if(Character.isDigit(peek)){
            int l = locate(new Locater(){
                public boolean pass(char c){
                    return Character.isDigit(c);
                }
            });
            if(source.charAt(l)=='.'){
                index = l+1;
                return new IndexToken();
            }
        }
        //构造TextToken，止于换行符
        int l = locate(new Locater(){
            public boolean pass(char c){
                return c!='\n';
            }
        });
        String text = source.subSequence(index-1, l).toString();
        text = text.trim();
        index = l;
        return new TextToken(text);
    }
    public static abstract class Token{
        public static enum Tag{
            OPTION, TEXT, SECTION, INDEX, INPUT, NEWLINE
        }
        public Tag tag;
    }
    public static class OptionToken extends Token{
        public boolean isDefault;
        public boolean isSingle;
        public OptionToken(boolean sin,boolean def){
            tag = Tag.OPTION;
            isDefault = def;
            isSingle = sin;
        }
    }
    public static class TextToken extends Token{
        public String content;
        public TextToken(String s){
            tag = Tag.TEXT;
            content = s;
        }
    }
    public static class SectionToken extends Token{
        public SectionToken(){
            tag = Tag.SECTION;
        }
    }
    public static class IndexToken extends Token{
        public IndexToken(){
            tag = Tag.INDEX;
        }
    }
    public static class InputToken extends Token{
        public InputToken(){
            tag = Tag.INPUT;
        }
    }
    public static class NewlineToken extends Token{
        public NewlineToken(){
            tag = Tag.NEWLINE;
        }
    }
    private int locate(Locater l){
        int i;
        for(i=index;i<source.length();++i){
            if(l.pass(source.charAt(i))){
                continue;
            }
            break;
        }
        return i;
    }
    private interface Locater{
        boolean pass(char c);
    }
    /*
    private class NonDescriptionTokenLocater implements Locater{
        NonDescriptionTokenLocater self = this;
        public boolean pass(char c){
            return c!='('&&c!='['&&c!='_'&&c!='-';
        }
        public Locater combine(final Locater l){
            return new Locater(){
                public boolean pass(char c){
                    return self.pass(c)&&l.pass(c);
                }
            };
        }
    }
    NonDescriptionTokenLocater nonDescriptionTokenLocater 
            = new NonDescriptionTokenLocater();
    
    
    
    public class Description extends Token{//text, could be one of
        //the three: a part notion, a comment, or a title
        public Description(String c){
            content = c;
        }
        private String content;
        public String getContent(){
            return content;
        }
        @Override
        public void accept(TokenVisitor visitor){
            visitor.visit(this);
        }
    }
    public class Section extends Token{//-------------
        @Override
        public void accept(TokenVisitor visitor){
            visitor.visit(this);
        }
    }
    public class Option extends Token{//(xxx)
        public Option(boolean s,boolean d,String c){
            isSingle = s;
            isDefault = d;
            content = c;
        }
        private String content;
        private boolean isSingle;
        private boolean isDefault;
        public boolean isSingle(){
            return isSingle;
        }
        public boolean isDefault(){
            return isDefault;
        }
        public String getContent(){
            return content;
        }
        @Override
        public void accept(TokenVisitor visitor){
            visitor.visit(this);
        }
    }
    public class TextInput extends Token{//_xxx_
        public TextInput(){
            hasDefaultValue = false;
        }
        public TextInput(String c){
            hasDefaultValue = true;
            content = c;
        }
        private boolean hasDefaultValue;
        private String content;
        public boolean hasDefaultValue(){
            return hasDefaultValue;
        }
        public String getDefaultValue(){
            return content;
        }
        @Override
        public void accept(TokenVisitor visitor){
            visitor.visit(this);
        }
    }
    */
}

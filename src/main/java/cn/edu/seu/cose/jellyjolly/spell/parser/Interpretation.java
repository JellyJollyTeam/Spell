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
        System.out.println(tokenList.size()+" tokens created in total");
    }
    private Token scan(){
        //Description是一种特殊的token，其边界是所有其它token(或\n?)
        //Description最后加以处理，把头尾的空格，\t，（\n？）去除
        if(index>=source.length()){
            return null;
        }
        int nextloc;
        boolean loop = true;
        do{
            nextloc = locate(nonDescriptionTokenLocater.combine(
                    new Locater(){
                        public boolean pass(char c){
                            return c!='\n';
                    }
                }));
            String description = source.subSequence(index, nextloc).toString();
            description = description.trim();
            if(source.charAt(nextloc)=='\n'){
                index = nextloc+1;
            } else {
                index = nextloc;
                loop = false;
            }
            if(!description.isEmpty()){
                return new Description(description);
            }
        }while(loop);
        switch(source.charAt(index)){
            case '(':{
                int i = locate(new Locater(){
                    public boolean pass(char c){
                        return c!=')';
                    }
                });
                boolean isDefault = i!=(index+1);
                index = i;
                int j = locate(nonDescriptionTokenLocater.combine(
                    new Locater(){
                        public boolean pass(char c){
                            return c!='\n';
                    }
                }));
                index = j;
                return new Option(true,isDefault,
                        source.subSequence(i+1, j).toString().trim());
            }
            case '[':{
                int i = locate(new Locater(){
                    public boolean pass(char c){
                        return c!=']';
                    }
                });
                boolean isDefault = i!=index+1;
                index = i;
                int j = locate(nonDescriptionTokenLocater.combine(
                    new Locater(){
                        public boolean pass(char c){
                            return c!='\n';
                    }
                }));
                index = j;
                return new Option(false,isDefault,
                        source.subSequence(i+1, j).toString().trim());
            }
            case '-':{
                index = locate(new Locater(){
                    public boolean pass(char c){
                        return c=='-';
                    }
                });
                return new Section();
            }
            case '_':{
                //换行符在这里有效，充当后边界
                int i = locate(new Locater(){
                    public boolean pass(char c){
                        return c=='_';
                    }
                });
                index = i;
                if(source.charAt(i)=='\n'){
                    index++;
                    return new TextInput();
                }
                int j = locate(new Locater(){
                    public boolean pass(char c){
                        return c!='_'&&c!='\n';
                    }
                });
                index = j;
                if(source.charAt(j)=='\n'){
                    index++;
                } else {
                    index = locate(new Locater(){
                        public boolean pass(char c){
                            return c=='_';
                        }
                    });
                }
                return new TextInput(
                        source.subSequence(i, j).toString());
            }
            default:
                System.out.println("unexpected character");
                return null;
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
        //System.out.println("located position "+i+" where char is "+source.charAt(i));
        return i;
    }
    
    private interface Locater{
        boolean pass(char c);
    }
    
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
    
    
    public Token getNextToken(){
        return iterator.hasNext() ? iterator.next() : null;
    }
    public abstract class Token{
        public abstract void accept(TokenVisitor visitor);
    }
    public interface TokenVisitor{
        void visit(Description description);
        void visit(Section section);
        void visit(Option option);
        void visit(TextInput textInput);
    }
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
}

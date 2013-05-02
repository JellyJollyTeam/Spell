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
    private Iterator<Token> iterator = tokenList.iterator();
    private int index = 0;
    private CharSequence source;
    public Interpretation(CharSequence s){
        source = s;
        Token k;
        while((k = scan())!=null){
            tokenList.add(k);
        }
    }
    private Token scan(){
        //Description是一种特殊的token，其边界是所有其它token
        //Description最后加以处理，把头尾的空格，\t，（\n？）去除
        if(index>=source.length()){
            return null;
        }
        int nextloc = locate(nonDescriptionTokenLocater.combine(
            new Locater(){
                public boolean pass(char c){
                    return c!='\n';
                }
            }));
        if(nextloc!=index){
            String description = 
                    source.subSequence(index, nextloc-1).toString();
            index = nextloc;
            description = description.trim();
            if(!description.isEmpty()){
                return new Description(description);
            }
        }
        switch(source.charAt(index)){
            case '(':{
                int i = locate(new Locater(){
                    public boolean pass(char c){
                        return c!=')';
                    }
                });
                boolean isDefault = i!=index+1;
                index = i;
                int j = locate(nonDescriptionTokenLocater)-1;
                index = j;
                return new Option(true,isDefault,
                        source.subSequence(i, j).toString());
            }
            case '[':{
                int i = locate(new Locater(){
                    public boolean pass(char c){
                        return c!=']';
                    }
                });
                boolean isDefault = i!=index+1;
                index = i;
                int j = locate(nonDescriptionTokenLocater)-1;
                index = j;
                return new Option(false,isDefault,
                        source.subSequence(i, j).toString());
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
                if(source.charAt(i)=='\n'){
                    return new TextInput();
                }
                int j = locate(new Locater(){
                    public boolean pass(char c){
                        return c!='_'&&c!='\n';
                    }
                });
                if(source.charAt(j)=='\n'){
                    index = j+1;
                } else {
                    index = locate(new Locater(){
                        public boolean pass(char c){
                            return c=='_';
                        }
                    });
                }
                return new TextInput(
                        source.subSequence(i, j-1).toString());
            }
            default:
                return null;
        }
    }
    private int locate(Locater l){
        int i;
        for(i=index+1;i<source.length();++i){
            if(l.pass(source.charAt(i))){
                continue;
            }
        }
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
    }
    public class Section extends Token{//-------------
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
    }
}

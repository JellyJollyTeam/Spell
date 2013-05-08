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

import cn.edu.seu.cose.jellyjolly.spell.parser.Interpretation.Token.Tag;
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
    private char peek;
    private CharSequence source;
    
    boolean lastTokenIsNEWLINE = false;
    private Scanner scanner;
    private Scanner scanner_1 = new Scanner(){
        public Token scan(){
            Token t;
            if((t = scan_NEWLINE())!=null){
                return t;
            }
            if((t = scan_INDEX())!=null){
                scanner = scanner_3;
                return t;
            }
            scanner = scanner_2;
            return scan_TEXT();
        }
    };
    private Scanner scanner_2 = new Scanner(){
        public Token scan(){
            Token t;
            if((t = scan_NEWLINE())!=null){
                return t;
            }
            if((t = scan_SECTION())!=null){
                scanner = scanner_1;
                return t;
            }
            if((t = scan_INDEX())!=null){
                scanner = scanner_3;
                return t;
            }
            return scan_TEXT();
        }
    };
    private Scanner scanner_3 = new Scanner(){
        public Token scan(){
            Token t;
            if((t = scan_NEWLINE())!=null){
                return t;
            }
            if((t = scan_OPTION())!=null){
                return t;
            }
            if((t = scan_INPUT())!=null){
                return t;
            }
            return scan_TEXT();
        }
    };
    
    public Interpretation(CharSequence s){
        source = s;
        scanner = scanner_1;
        Token k;
        while((k = scan())!=null){
            tokenList.add(k);
        }
        iterator = tokenList.iterator();
    }
    public Token getNextToken(){
        return iterator.hasNext() ? iterator.next() : null;
    }
    /* 词法分析器的所有Token：TEXT,NEWLINE,SECTION,INDEX,OPTION,INPUT
     * 
     * 具有3个状态：
     * 1. 生成 TEXT, NEWLINE, INDEX
     * 2. 生成 TEXT, NEWLINE, SECTION, INDEX
     * 3. 生成 TEXT, NEWLINE, OPTION, INPUT
     * 
     * 转换方案：
     * 开始处于1
     * 生成 TEXT 时 1->2
     * 生成 INDEX 时 1->3 / 2->3
     * 生成 SECTION 时 2->1
     * 生成连续两个 NEWLINE 时 3->1 / 2->1
     * */
    private Token scan(){
        if(index>=source.length()){
            return null;
        }
        //忽略token之间的空字符
        do{
            peek = source.charAt(index++);
        }while(peek==' '||peek=='\t');
        
        Token t = scanner.scan();
        if(t.tag == Tag.NEWLINE){
            if(lastTokenIsNEWLINE){
                scanner = scanner_1;
            } else {
                lastTokenIsNEWLINE = true;
            }
        } else {
            lastTokenIsNEWLINE = false;
        }
        return t;
    }
    private Token scan_NEWLINE(){
        if(peek=='\n'){
            return new NewlineToken();
        }
        return null;
    }
    private Token scan_SECTION(){
        if(peek=='-'){
            index = locate(new Locater(){
                public boolean pass(char c){
                    return c=='-';
                }
            });
            return new SectionToken();
        }
        return null;
    }
    private Token scan_OPTION(){
        if(peek=='('){
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
        } else if(peek=='['){
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
        return null;
    }
    private Token scan_INPUT(){
        if(peek=='_'){
            index = locate(new Locater(){
                public boolean pass(char c){
                    return c=='_';
                }
            });
            return new InputToken();
        }
        return null;
    }
    private Token scan_INDEX(){
        //IndexToken，由数字+'.'组成
        if(Character.isDigit(peek)){
            int l = locate(new Locater(){
                public boolean pass(char c){
                    return Character.isDigit(c);
                }
            });
            if(source.charAt(l)=='.'){
                Token it = new IndexToken();
                it.lexeme = source.subSequence(index-1, l+1).toString();
                index = l+1;
                return it;
            }
        }
        return null;
    }
    private Token scan_TEXT(){
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
    private interface Scanner{
        Token scan();
    }
    public static abstract class Token{
        public static enum Tag{
            OPTION, TEXT, SECTION, INDEX, INPUT, NEWLINE
        }
        public Tag tag;
        public String lexeme = "";
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
        public TextToken(String s){
            tag = Tag.TEXT;
            lexeme = s;
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
            lexeme = " ";
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
}

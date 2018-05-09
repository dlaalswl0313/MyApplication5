package com.example.a510.myapplication;

/*-------------------------------------------------------------------
StringTok.java: String with Token Operations
Copyright ⓒ 2016-Forever ACE Lab, Korea.
All Rights Reserved. Personal and non-commercial purpose only.
-------------------------------------------------------------------*/

public class StringTok extends Object {
    private static final String m_sDelimit = " +-*/<>=()&|!,;:@#$%^\t\r\n";
    private static final String m_sWhite = " \t\r\n";
    private static final char EOL = '\n'; // End of line
    private String m_str;
    private int m_nPosTok;

    public StringTok() {
        empty();
    }

    public StringTok(String str) {
        setString(str);
    }

    public StringTok(StringTok str) {
        setString(str);
    }

    public StringTok(char ch) {
        setString(ch);
    }

    public StringTok(char[] ptr) {
        setString(ptr);
    }

    public char getAt(int nPos) {
        return m_str.charAt(nPos);
    }

    public char getLastAt() {
        return getAt(getLength() - 1);
    }    // Get the last character.

    public int getLength() {
        return (m_str == null) ? 0 : m_str.length();
    }

    public StringTok getToken() {
        int nInput = getLength();
        if (nInput <= 0) return new StringTok();
        while (isWhite(m_str.charAt(m_nPosTok))) {
            m_nPosTok++;
            if (m_nPosTok >= nInput) return new StringTok();
        }
        String str = String.valueOf(m_str.charAt(m_nPosTok));
        m_nPosTok++;
        if (isDelimit(str.charAt(0))) return new StringTok(str);
        while (m_nPosTok < nInput) {
            if (isDelimit(m_str.charAt(m_nPosTok))) return new StringTok(str);
            str += m_str.charAt(m_nPosTok);
            m_nPosTok++;
        }
        return new StringTok(str);
    }

    public StringTok getTokenWhite() {
        int nInput = getLength();
        if (nInput <= 0) return new StringTok();
        while (isWhite(m_str.charAt(m_nPosTok))) {
            m_nPosTok++;
            if (m_nPosTok >= nInput) return new StringTok();
        }
        String str = String.valueOf(m_str.charAt(m_nPosTok));
        m_nPosTok++;
        while (m_nPosTok < nInput) {
            if (isWhite(m_str.charAt(m_nPosTok))) return new StringTok(str);
            str += m_str.charAt(m_nPosTok);
            m_nPosTok++;
        }
        return new StringTok(str);
    }

    public StringTok getTokenNum() {
        StringTok sToken = getToken();
        StringTok sTokenAdd;
        if ((sToken.charAt(0) == '-' || sToken.charAt(0) == '+') && (m_nPosTok < getLength()) && !isDelimit(charAt(m_nPosTok))) {
            sTokenAdd = getToken();
            sToken = new StringTok(sToken.toString() + sTokenAdd.toString());
        }
        if (sToken.charLastAt() == 'e' || sToken.charLastAt() == 'E')    // Exponent
        {
            sTokenAdd = getToken();
            sToken = new StringTok(sToken.toString() + sTokenAdd.toString());
            if ((sTokenAdd.charAt(0) == '-' || sTokenAdd.charAt(0) == '+') && (m_nPosTok < getLength()) && !isDelimit(charAt(m_nPosTok))) {
                sTokenAdd = getToken();
                sToken = new StringTok(sToken.toString() + sTokenAdd.toString());
            }
        }
        return sToken;
    }

    public int getPosTok() {
        return m_nPosTok;
    }

    public String getLeft(int nCount) {
        return m_str.substring(0, nCount);
    }

    public String getRight(int nCount) {
        return m_str.substring(m_str.length() - nCount);
    }

    public String getMid(int nFirst, int nCount) {
        return m_str.substring(nFirst, nFirst + nCount);
    }

    public String getString() {
        return m_str;
    }

    public int getPosLine() {
        return m_str.indexOf(EOL);
    }

    public String getLine() {
        int nPos = m_str.indexOf(EOL);
        if (nPos < 0) return "";
        return m_str.substring(0, nPos);
    }

    public String cutLine() {
        int nPos = m_str.indexOf(EOL);
        if (nPos < 0) return "";
        String sLine = m_str.substring(0, nPos);
        if (nPos + 1 < getLength()) setString(m_str.substring(nPos + 1));
        else empty();
        return sLine;
    }

    public void setString(String str) {
        m_str = str;
        initPosTok();
    }

    public void setString(char ch) {
        setString(String.valueOf(ch));
    }

    public void setString(char[] ptr) {
        setString(new String(ptr));
    }

    public void setString(StringTok str) {
        setString(str.toString());
    }

    public void setPosTok(int nPos) {
        m_nPosTok = nPos;
    }

    public void appendString(char ch) {
        m_str += ch;
    }

    public void appendString(char[] ptr) {
        m_str += new String(ptr);
    }

    public void appendString(String str) {
        m_str += str;
    }

    public void appendString(StringTok str) {
        appendString(str.getString());
    }

    public void initPosTok() {
        setPosTok(0);
    }

    public char charAt(int nPos) {
        return getAt(nPos);
    }

    public char charLastAt() {
        return getLastAt();
    }

    public int length() {
        return getLength();
    }

    public String toString() {
        return getString();
    }

    public long atoi() {
        return Long.parseLong(m_str);
    }

    public double atof() {
        return Double.parseDouble(m_str);
    }

    public long toInt() {
        return atoi();
    }

    public long toLong() {
        return toInt();
    }

    public double toFloat() {
        return atof();
    }

    public double toDouble() {
        return toFloat();
    }

    public void empty() {
        setString("");
    }

    public static boolean isDelimit(char ch) {
        for (int i = 0; i < m_sDelimit.length(); i++) if (ch == m_sDelimit.charAt(i)) return true;
        return false;
    }

    public static boolean isWhite(char ch) {
        for (int i = 0; i < m_sWhite.length(); i++) if (ch == m_sWhite.charAt(i)) return true;
        return false;
    }

    public boolean isEmpty() {
        return length() <= 0;
    }

    public boolean hasLine() {
        return m_str.indexOf(EOL) >= 0;
    }

    public boolean equals(char ch) {
        return equals(String.valueOf(ch));
    }

    public boolean equals(char[] ptr) {
        return equals(new String(ptr));
    }

    public boolean equals(String str) {
        return m_str.equals(str);
    }

    public boolean equals(StringTok str) {
        return equals(str.toString());
    }
}

/*-------------------------------------------------------------------
Revision Records
---------------------------------------------------------------------
C(2016-12-05)
R(2018-04-01): the line operations were added.
-------------------------------------------------------------------*/
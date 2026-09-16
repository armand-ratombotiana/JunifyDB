package org.junify.db.sql.parser;

import java.util.*;

public class SqlLexer {

    public enum TokenType {
        KEYWORD, IDENTIFIER, STRING_LITERAL, NUMBER_LITERAL, PARAMETER,
        SYMBOL, EOF
    }

    public static class Token {
        private final TokenType type;
        private final String value;
        private final int position;

        public Token(TokenType type, String value, int position) {
            this.type = type;
            this.value = value;
            this.position = position;
        }

        public TokenType getType() { return type; }
        public String getValue() { return value; }
        public int getPosition() { return position; }

        public boolean isKeyword(String kw) {
            return type == TokenType.KEYWORD && value.equalsIgnoreCase(kw);
        }

        public boolean isSymbol(String sym) {
            return type == TokenType.SYMBOL && value.equals(sym);
        }

        @Override
        public String toString() {
            return type + "[" + value + "]";
        }
    }

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "SELECT", "DISTINCT", "FROM", "WHERE", "AND", "OR", "NOT",
            "ORDER", "BY", "ASC", "DESC", "LIMIT", "OFFSET",
            "JOIN", "INNER", "LEFT", "ON", "GROUP", "HAVING",
            "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE",
            "CREATE", "TABLE", "DROP", "IF", "EXISTS", "AS",
            "IN", "IS", "NULL", "LIKE", "TRUE", "FALSE", "BETWEEN"
    ));

    public static List<Token> tokenize(String sql) {
        List<Token> tokens = new ArrayList<>();
        if (sql == null || sql.isBlank()) {
            tokens.add(new Token(TokenType.EOF, "", 0));
            return tokens;
        }

        int i = 0;
        int len = sql.length();

        while (i < len) {
            char c = sql.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // String literals: 'string' or "string"
            if (c == '\'' || c == '"') {
                char quote = c;
                int start = i;
                i++;
                StringBuilder sb = new StringBuilder();
                while (i < len && sql.charAt(i) != quote) {
                    if (sql.charAt(i) == '\\' && i + 1 < len) {
                        i++;
                        sb.append(sql.charAt(i));
                    } else {
                        sb.append(sql.charAt(i));
                    }
                    i++;
                }
                if (i < len && sql.charAt(i) == quote) {
                    i++; // consume closing quote
                }
                tokens.add(new Token(TokenType.STRING_LITERAL, sb.toString(), start));
                continue;
            }

            // Parameter: ? or :name
            if (c == '?') {
                tokens.add(new Token(TokenType.PARAMETER, "?", i));
                i++;
                continue;
            }
            if (c == ':') {
                int start = i;
                i++;
                StringBuilder sb = new StringBuilder();
                while (i < len && (Character.isLetterOrDigit(sql.charAt(i)) || sql.charAt(i) == '_')) {
                    sb.append(sql.charAt(i));
                    i++;
                }
                tokens.add(new Token(TokenType.PARAMETER, sb.toString(), start));
                continue;
            }

            // Numbers: 123 or 123.45
            if (Character.isDigit(c) || (c == '-' && i + 1 < len && Character.isDigit(sql.charAt(i + 1)))) {
                int start = i;
                StringBuilder sb = new StringBuilder();
                sb.append(c);
                i++;
                boolean hasDot = false;
                while (i < len && (Character.isDigit(sql.charAt(i)) || (!hasDot && sql.charAt(i) == '.'))) {
                    if (sql.charAt(i) == '.') hasDot = true;
                    sb.append(sql.charAt(i));
                    i++;
                }
                tokens.add(new Token(TokenType.NUMBER_LITERAL, sb.toString(), start));
                continue;
            }

            // Identifiers / Keywords
            if (Character.isLetter(c) || c == '_' || c == '`') {
                int start = i;
                boolean isBacktick = (c == '`');
                if (isBacktick) i++;
                StringBuilder sb = new StringBuilder();
                while (i < len) {
                    char ch = sql.charAt(i);
                    if (isBacktick) {
                        if (ch == '`') { i++; break; }
                        sb.append(ch);
                    } else {
                        if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '.') {
                            sb.append(ch);
                        } else {
                            break;
                        }
                    }
                    i++;
                }
                String word = sb.toString();
                if (!isBacktick && KEYWORDS.contains(word.toUpperCase())) {
                    tokens.add(new Token(TokenType.KEYWORD, word.toUpperCase(), start));
                } else {
                    tokens.add(new Token(TokenType.IDENTIFIER, word, start));
                }
                continue;
            }

            // Multi-char symbols: !=, <>, <=, >=
            if (i + 1 < len) {
                String two = sql.substring(i, i + 2);
                if (two.equals("!=") || two.equals("<>") || two.equals("<=") || two.equals(">=")) {
                    tokens.add(new Token(TokenType.SYMBOL, two, i));
                    i += 2;
                    continue;
                }
            }

            // Single-char symbols: *, ,, (, ), =, <, >, +, -, /, ;, etc.
            tokens.add(new Token(TokenType.SYMBOL, String.valueOf(c), i));
            i++;
        }

        tokens.add(new Token(TokenType.EOF, "", len));
        return tokens;
    }
}

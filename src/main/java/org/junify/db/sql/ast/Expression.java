package org.junify.db.sql.ast;

import java.util.*;

public interface Expression {

    Object evaluate(Map<String, Object> context, List<Object> params);

    enum Operator {
        EQ, NEQ, LT, LTE, GT, GTE, LIKE, AND, OR, ADD, SUB, MUL, DIV
    }

    class ColumnExpr implements Expression {
        private final String tableAlias;
        private final String columnName;

        public ColumnExpr(String tableAlias, String columnName) {
            this.tableAlias = tableAlias;
            this.columnName = columnName;
        }

        public String getTableAlias() { return tableAlias; }
        public String getColumnName() { return columnName; }

        @Override
        public Object evaluate(Map<String, Object> context, List<Object> params) {
            if (context == null) return null;
            if (tableAlias != null) {
                String qualified = tableAlias + "." + columnName;
                if (context.containsKey(qualified)) return context.get(qualified);
            }
            if (context.containsKey(columnName)) {
                return context.get(columnName);
            }
            // Case-insensitive fallback
            for (Map.Entry<String, Object> e : context.entrySet()) {
                if (e.getKey().equalsIgnoreCase(columnName)) return e.getValue();
                if (tableAlias != null && e.getKey().equalsIgnoreCase(tableAlias + "." + columnName)) return e.getValue();
            }
            return null;
        }

        @Override
        public String toString() {
            return (tableAlias != null ? tableAlias + "." : "") + columnName;
        }
    }

    class LiteralExpr implements Expression {
        private final Object value;

        public LiteralExpr(Object value) {
            this.value = value;
        }

        public Object getValue() { return value; }

        @Override
        public Object evaluate(Map<String, Object> context, List<Object> params) {
            return value;
        }

        @Override
        public String toString() {
            return value instanceof String ? "'" + value + "'" : String.valueOf(value);
        }
    }

    class ParameterExpr implements Expression {
        private final int index; // 1-based or 0-based
        private final String name;

        public ParameterExpr(int index, String name) {
            this.index = index;
            this.name = name;
        }

        public int getIndex() { return index; }
        public String getName() { return name; }

        @Override
        public Object evaluate(Map<String, Object> context, List<Object> params) {
            if (params != null && index >= 0 && index < params.size()) {
                return params.get(index);
            }
            return null;
        }

        @Override
        public String toString() {
            return name != null ? ":" + name : "?";
        }
    }

    class BinaryExpr implements Expression {
        private final Expression left;
        private final Operator operator;
        private final Expression right;

        public BinaryExpr(Expression left, Operator operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public Expression getLeft() { return left; }
        public Operator getOperator() { return operator; }
        public Expression getRight() { return right; }

        @Override
        public Object evaluate(Map<String, Object> context, List<Object> params) {
            Object l = left != null ? left.evaluate(context, params) : null;
            Object r = right != null ? right.evaluate(context, params) : null;

            switch (operator) {
                case AND:
                    return isTruthy(l) && isTruthy(r);
                case OR:
                    return isTruthy(l) || isTruthy(r);
                case EQ:
                    return compare(l, r) == 0;
                case NEQ:
                    return compare(l, r) != 0;
                case LT:
                    return compare(l, r) < 0;
                case LTE:
                    return compare(l, r) <= 0;
                case GT:
                    return compare(l, r) > 0;
                case GTE:
                    return compare(l, r) >= 0;
                case LIKE:
                    if (l == null || r == null) return false;
                    String str = l.toString();
                    String raw = r.toString();
                    if (raw.contains("%") || raw.contains("_")) {
                        String regex = "^" + java.util.regex.Pattern.quote(raw)
                                .replace("%", "\\E.*\\Q")
                                .replace("_", "\\E.\\Q") + "$";
                        regex = regex.replace("\\Q\\E", "");
                        return java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE).matcher(str).matches();
                    }
                    return str.toLowerCase().contains(raw.toLowerCase());
                case ADD:
                    if (l instanceof Number nl && r instanceof Number nr) {
                        return nl.doubleValue() + nr.doubleValue();
                    }
                    return (l != null ? l.toString() : "") + (r != null ? r.toString() : "");
                case SUB:
                    if (l instanceof Number nl2 && r instanceof Number nr2) {
                        return nl2.doubleValue() - nr2.doubleValue();
                    }
                    return 0;
                case MUL:
                    if (l instanceof Number nl3 && r instanceof Number nr3) {
                        return nl3.doubleValue() * nr3.doubleValue();
                    }
                    return 0;
                case DIV:
                    if (l instanceof Number nl4 && r instanceof Number nr4 && nr4.doubleValue() != 0) {
                        return nl4.doubleValue() / nr4.doubleValue();
                    }
                    return 0;
                default:
                    return false;
            }
        }

        private static boolean isTruthy(Object o) {
            if (o == null) return false;
            if (o instanceof Boolean b) return b;
            if (o instanceof Number n) return n.doubleValue() != 0;
            return !o.toString().isBlank();
        }

        private static int compare(Object a, Object b) {
            if (a == null && b == null) return 0;
            if (a == null) return -1;
            if (b == null) return 1;
            if (a.equals(b)) return 0;
            if (a instanceof Number na && b instanceof Number nb) {
                return Double.compare(na.doubleValue(), nb.doubleValue());
            }
            if (a instanceof Number && !(b instanceof Number)) {
                try {
                    return Double.compare(((Number) a).doubleValue(), Double.parseDouble(b.toString()));
                } catch (NumberFormatException ignored) {}
            }
            if (b instanceof Number && !(a instanceof Number)) {
                try {
                    return Double.compare(Double.parseDouble(a.toString()), ((Number) b).doubleValue());
                } catch (NumberFormatException ignored) {}
            }
            return a.toString().compareToIgnoreCase(b.toString());
        }

        @Override
        public String toString() {
            return "(" + left + " " + operator + " " + right + ")";
        }
    }

    class InExpr implements Expression {
        private final Expression expression;
        private final List<Expression> values;
        private final boolean notIn;

        public InExpr(Expression expression, List<Expression> values, boolean notIn) {
            this.expression = expression;
            this.values = values;
            this.notIn = notIn;
        }

        @Override
        public Object evaluate(Map<String, Object> context, List<Object> params) {
            Object target = expression.evaluate(context, params);
            if (target == null) return notIn;
            boolean match = false;
            for (Expression valExpr : values) {
                Object v = valExpr.evaluate(context, params);
                if (target.toString().equalsIgnoreCase(String.valueOf(v))) {
                    match = true;
                    break;
                }
            }
            return notIn != match;
        }
    }

    class IsNullExpr implements Expression {
        private final Expression expression;
        private final boolean notNull;

        public IsNullExpr(Expression expression, boolean notNull) {
            this.expression = expression;
            this.notNull = notNull;
        }

        @Override
        public Object evaluate(Map<String, Object> context, List<Object> params) {
            Object val = expression.evaluate(context, params);
            boolean isNull = (val == null);
            return notNull != isNull;
        }
    }

    class FunctionExpr implements Expression {
        private final String functionName;
        private final List<Expression> arguments;
        private final boolean distinct;

        public FunctionExpr(String functionName, List<Expression> arguments, boolean distinct) {
            this.functionName = functionName.toUpperCase();
            this.arguments = arguments;
            this.distinct = distinct;
        }

        public String getFunctionName() { return functionName; }
        public List<Expression> getArguments() { return arguments; }
        public boolean isDistinct() { return distinct; }

        @Override
        public Object evaluate(Map<String, Object> context, List<Object> params) {
            if (arguments.isEmpty()) return null;
            Object argVal = arguments.get(0).evaluate(context, params);
            if (argVal == null) return null;

            switch (functionName) {
                case "UPPER":
                    return argVal.toString().toUpperCase();
                case "LOWER":
                    return argVal.toString().toLowerCase();
                case "LENGTH":
                case "LEN":
                    return argVal.toString().length();
                case "TRIM":
                    return argVal.toString().trim();
                default:
                    return argVal;
            }
        }

        public boolean isAggregate() {
            return functionName.equals("COUNT") || functionName.equals("SUM")
                    || functionName.equals("AVG") || functionName.equals("MIN") || functionName.equals("MAX");
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(functionName).append("(");
            if (distinct) sb.append("DISTINCT ");
            for (int i = 0; i < arguments.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(arguments.get(i));
            }
            sb.append(")");
            return sb.toString();
        }
    }

    class BetweenExpr implements Expression {
        private final Expression target;
        private final Expression lower;
        private final Expression upper;
        private final boolean notBetween;

        public BetweenExpr(Expression target, Expression lower, Expression upper, boolean notBetween) {
            this.target = target;
            this.lower = lower;
            this.upper = upper;
            this.notBetween = notBetween;
        }

        public Expression getTarget() { return target; }
        public Expression getLower() { return lower; }
        public Expression getUpper() { return upper; }
        public boolean isNotBetween() { return notBetween; }

        @Override
        public Object evaluate(Map<String, Object> context, List<Object> params) {
            Object val = target != null ? target.evaluate(context, params) : null;
            Object low = lower != null ? lower.evaluate(context, params) : null;
            Object up = upper != null ? upper.evaluate(context, params) : null;
            if (val == null || low == null || up == null) return false;
            boolean between = BinaryExpr.compare(val, low) >= 0 && BinaryExpr.compare(val, up) <= 0;
            return notBetween ? !between : between;
        }

        @Override
        public String toString() {
            return target + (notBetween ? " NOT BETWEEN " : " BETWEEN ") + lower + " AND " + upper;
        }
    }
}

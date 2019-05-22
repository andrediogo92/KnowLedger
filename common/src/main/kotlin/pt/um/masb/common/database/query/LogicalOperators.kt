package pt.um.masb.common.database.query

enum class SimpleBinaryOperator(val s: String) {
    AND("AND"),
    OR("OR")
}

enum class LogicalOperator(val s: String) {
    AND("AND"),
    OR("OR"),
    NOT("NOT"),
    CONTAINS("CONTAINS"),
    IN("IN"),
    CONTAINSKEY("CONTAINSKEY"),
    CONTAINSVALUE("CONTAINSVALUE"),
    LIKE("LIKE"),
    ISDEFINED("IS DEFINED"),
    ISNOTDEFINE("IS NOT DEFINED"),
    MATCHES("MATCHES"),
    INSTANCEOF("INSTANCEOF")
}
package org.jetbrains.research.ml.tasktracker.ui.util

private val factorsList = listOf(
    "self-management to time",
    "self-organization/problem solving",
    "self-restraint",
    "self-motivation",
    "self-regulation of emotion"
)

private const val CONTROL_SUM = 8

fun getSurveyFactors(answers: List<Int>): String {
    val factorsSumList = answers.chunked(4).map { it.sum() } zip factorsList
    val factors = factorsSumList.filter { it.first > CONTROL_SUM }.map { it.second }
    if (factors.isEmpty()) {
        return "You have no attention difficulties that affect your everyday life."
    }
    return "We found out that you may have some difficulties with" + factors.joinToString(", ") +
            ". If you feel that these issues affect your everyday life or you would like to know more about attention management, it might be helpful to discuss this with a psychologist of your choice."
}

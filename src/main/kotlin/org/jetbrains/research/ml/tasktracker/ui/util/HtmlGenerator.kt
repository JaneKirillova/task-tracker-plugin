package org.jetbrains.research.ml.tasktracker.ui.util

import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.document
import kotlinx.html.dom.serialize

class HtmlGenerator {
    fun getSurveyPage(title: String, questions: List<String>): String {
        return document {
            append.html {
                generateHead()
                generateTableInfo(title)
                generateQuestionsTable(questions)
                body {
                    p {
                        attributes["style"] = "text-align: center;padding-bottom: 10px;"
                        attributes["font"] = "normal 18px/1.6 system-ui, sans-serif;"
                        button {
                            attributes
                            id = "next-button"
                            text("Next")
                        }
                    }
                }
            }
        }.serialize()
    }

    private fun HTML.generateHead(): HTML {
        return apply {
            head {
                script {
                    unsafe {
                        raw(
                            """
                                function checkSurvey() {
                                var elements = document.querySelectorAll('.question:checked');
                                if (elements.length !== 10) {
                                alert("Please complete all questions");
                                return false;
                                }
                                return true
                                }
                                """.trimIndent()
                        )
                    }
                }
                style {
                    unsafe {
                        raw(
                            javaClass.getResource("/org/jetbrains/research/ml/tasktracker/ui/css/survey.css")
                                ?.readText().toString()

                        )
                    }

                }
            }
        }
    }

    private fun HTML.generateTableInfo(title: String): HTML {
        return apply {
            body {
                h2 { text(title) }
                p {
                    attributes["style"] = "text-align: left"
                    br {
                        text("How often do you experience each of these problems?")
                    }
                    br {
                        text("1- Never or rarely; 2 - Sometimes; 3 - Often; 4 - Very often. ")
                        text("Please mark the number next to each item that best describes your behavior")
                        b { text(" during the past 6 months.") }
                    }
                }
            }
        }
    }

    private fun HTML.generateQuestionsTable(questions: List<String>): HTML {
        return apply {
            body {
                form {
                    table {
                        attributes["cellspacing"] = "10"
                        attributes["width"] = "105%"
                        tr {
                            th {
                                attributes["style"] = "width:10;"
                                text("Question")
                            }
                            th { text("Never or rarely") }
                            th { text("Sometimes") }
                            th { text("Often") }
                            th { text("Very often") }
                        }
                        questions.forEachIndexed { index, question ->
                            tr {
                                td {
                                    attributes["style"] = "text-align: left;display: inline-block;"
                                    text(question)
                                }
                                for (i in 1..4) {
                                    td {
                                        attributes["style"] = "text-align:center;"
                                        input {
                                            type = InputType.radio
                                            classes = setOf("question")
                                            name = "choose-button-$index"
                                            value = i.toString()
                                        }
                                        text(i)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
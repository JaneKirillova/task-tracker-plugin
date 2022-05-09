package org.jetbrains.research.ml.tasktracker.ui.util

import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.document
import kotlinx.html.dom.serialize

class HtmlGenerator {
    companion object {
        fun getSurveyPage(title: String, questions: List<String>): String {
            return document {
                append.html {
                    generateHead(title)
                    generateTableInfo(title)
                    generateQuestionsTable(questions)
                    body {
                        p {
                            button {
                                id = "next-button"
                                text("Next")
                            }
                        }
                    }
                }
            }.serialize()
        }

        private fun HTML.generateHead(title: String): HTML {
            return apply {
                head {
                    title(title)
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
                                """
                                        body {
                                            width: 60%;
                                            float: left;
                                            display: inline-block;
                                            text-align: center;
                                        }
                                    """.trimIndent()
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
                        attributes["style"] = "padding: 15px;text-align: left"
                        br { text("Please, proceed with the survey.") }
                        br { text("How often do you experience each of these problems?") }
                        br { text("1- Never or rarely; 2 - Sometimes; 3 - Often; 4 - Very often.") }
                        br { text("   Please mark the number next to each item that best describes your behavior <b>during the past 6 months") }
                    }
                }
            }
        }

        private fun HTML.generateQuestionsTable(questions: List<String>): HTML {
            return apply {
                body {
                    form {
                        table {
                            attributes["cellspacing"] = "15"
                            tr {
                                th { text("Question") }
                                th { text("Never or rarely") }
                                th { text("Sometimes") }
                                th { text("Often") }
                                th { text("Very often") }
                            }
                            questions.forEachIndexed { index, question ->
                                tr {
                                    td {
                                        attributes["style"] = "text-align: left;"
                                        text(question)
                                    }
                                    for (i in 1..4) {
                                        td {
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
}

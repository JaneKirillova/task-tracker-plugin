package org.jetbrains.research.ml.tasktracker.models

enum class Language(val extension: Extension, override val key: String) : Keyed {
    PYTHON(Extension.PY, "python"),
    JUPYTER(Extension.IPYNB,"jupyter"),
    JAVA(Extension.JAVA, "java"),
    KOTLIN(Extension.KT, "kotlin"),
    CPP(Extension.CPP, "c++");
}

enum class Extension(val ext: String) {
    PY(".py"),
    IPYNB(".ipynb"),
    JAVA(".java"),
    KT(".kt"),
    CPP(".cpp"),
    CSV(".csv");
}
package org.jetbrains.research.ml.codetracker.ui.panes

import javafx.collections.FXCollections
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import org.jetbrains.research.ml.codetracker.models.Task
import org.jetbrains.research.ml.codetracker.models.TaskInfo
import org.jetbrains.research.ml.codetracker.ui.MainController
import org.jetbrains.research.ml.codetracker.ui.makeTranslatable
import kotlin.reflect.KClass

enum class TaskChooserNotifyEvent : IPaneNotifyEvent {
    CHOSEN_TASK_NOTIFY,
    LANGUAGE_NOTIFY
}

object TaskChooserControllerManager : PaneControllerManager<TaskChooserNotifyEvent, TaskChooserController>() {
    override val paneControllerClass: KClass<TaskChooserController> = TaskChooserController::class
    override var paneControllers: MutableList<TaskChooserController> = arrayListOf()
    override val paneUiData: PaneUiData<TaskChooserNotifyEvent> =
        TaskChooserUiData
    override val fxmlFilename: String = "taskChooser-ui-form-2.fxml"

    override fun notify(notifyEvent: TaskChooserNotifyEvent, new: Any?) {
        val isDataUnfilled = paneUiData.anyDataDefault()
        paneControllers.forEach { it.setStartSolvingButtonDisability(isDataUnfilled) }
        when (notifyEvent) {
            TaskChooserNotifyEvent.CHOSEN_TASK_NOTIFY -> paneControllers.forEach { it.selectTask(new as Int) }
            TaskChooserNotifyEvent.LANGUAGE_NOTIFY -> switchLanguage(new as Int)
        }
    }
}

object TaskChooserUiData : PaneUiData<TaskChooserNotifyEvent>(
    TaskChooserControllerManager
) {
//    Todo: get from server
    private val tasks: List<Task> = arrayListOf(
        Task("key1", 0, hashMapOf("en" to TaskInfo("name1", "description1", "input1", "output1"))),
        Task("key2", 1, hashMapOf("en" to TaskInfo("name2", "description2", "input2", "output2"))))
    val chosenTask = ListedUiField(
        tasks,
        TaskChooserNotifyEvent.CHOSEN_TASK_NOTIFY, -1,"chosenTask")
    override val currentLanguage: LanguageUiField = LanguageUiField(
        TaskChooserNotifyEvent.LANGUAGE_NOTIFY
    )
    override fun getData(): List<UiField<*>> = listOf(
        chosenTask
    )
}



class TaskChooserController(override val uiData: TaskChooserUiData, scale: Double, fxPanel: JFXPanel, id: Int) : PaneController<TaskChooserNotifyEvent>(uiData, scale, fxPanel, id) {
    @FXML private lateinit var taskChooserPane: Pane

    @FXML private lateinit var orangePolygon: Polygon
    @FXML private lateinit var yellowRectangle: Rectangle
    @FXML private lateinit var bluePolygon: Polygon

    @FXML private lateinit var choseTaskComboBox: ComboBox<String>
    @FXML private lateinit var choseTaskLabel: Label

//    Todo: maybe we need a text under this button because when user comes back from TaskPane it becomes unclear
    @FXML private lateinit var backToProfileButton: Button
    @FXML private lateinit var startSolvingButton: Button
    @FXML private lateinit var startSolvingText: Text
    @FXML private lateinit var finishWorkButton: Button
    @FXML private lateinit var finishWorkText: Text

    override fun initialize() {
        initChoseTaskComboBox()
        initButtons()
        super.initialize()
    }

    fun selectTask(newTaskIndex: Int) {
        choseTaskComboBox.selectionModel.select(newTaskIndex)
    }

    fun setStartSolvingButtonDisability(isDisable: Boolean) {
        startSolvingButton.isDisable = isDisable
    }

    override fun makeTranslatable() {
        choseTaskLabel.makeTranslatable(::choseTaskLabel.name)
        startSolvingText.makeTranslatable(::startSolvingText.name)
        finishWorkText.makeTranslatable(::finishWorkText.name)

    }

    private fun initChoseTaskComboBox() {
//        Todo: change language to current language
        choseTaskComboBox.items = FXCollections.observableList(TaskChooserUiData.chosenTask.dataList.map { it.info?.get("en")?.name })
        choseTaskComboBox.selectionModel.selectedItemProperty().addListener { _, old, new ->
            TaskChooserUiData.chosenTask.uiValue = choseTaskComboBox.selectionModel.selectedIndex
        }
    }

    private fun initButtons() {
//        Todo: move out the same event handlers?
        backToProfileButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            MainController.visiblePaneControllerManager =
                ProfileControllerManager
        }
        startSolvingButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            MainController.visiblePaneControllerManager =
                TaskControllerManager
        }
        finishWorkButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            MainController.visiblePaneControllerManager =
                FinishControllerManager
        }
    }
}
import dto.TodoDto
import io.qameta.allure.Description
import io.qameta.allure.Story
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TodoRestTest {
    companion object {
        const val TODO_PORT = 4242
        private const val APP_NAME = "todo-app"
        val steps = Steps()

        @Container
        var env: GenericContainer<*> = GenericContainer(DockerImageName.parse(APP_NAME)).withExposedPorts(TODO_PORT)
    }

    @Story("Customer Scenario")
    @Description(
        "Create 1 TODO " +
                "Get and check first TODO from list " +
                "Update an existing TODO with the provided one " +
                "Check that TODO has been updated" +
                "Clear database after test"
    )
    @Tag("api")
    @Test
    fun todoEndToEnd() {
        val postTodo = steps.createRandomTodoDTO()
        steps.createTodo(postTodo)
        val getPostedTodo = steps.getListCurrentTodos()[0]
        steps.assertEquals(postTodo, getPostedTodo)

        val updatedTodo = steps.createRandomTodoDTO()
        steps.updateExistingTodo(getPostedTodo, updatedTodo)

        val getUpdatedTodo = steps.getListCurrentTodos()[0]
        steps.assertEquals(updatedTodo, getUpdatedTodo)

        deleteTodosAndCheck(updatedTodo)
    }

    @Story("Check GET with limit and offset")
    @Description(
        "Create 2 TODO's " +
                "Check that /GET with limit 1 and offset 1 returned SECOND Todo (not first) " +
                "Clear database after test"
    )
    @Tag("api")
    @Test
    fun getTodoWithLimitOffset() {
        val postFirstTodo = steps.createRandomTodoDTO()
        steps.createTodo(postFirstTodo)
        val postSecondTodo = steps.createRandomTodoDTO()
        steps.createTodo(postSecondTodo)

        val getSecondTodo = steps.getTodoWithLimitOffset(1, 1)
        steps.assertEquals(postSecondTodo, getSecondTodo)

        deleteTodosAndCheck(postFirstTodo, postSecondTodo)
    }

    private fun deleteTodosAndCheck(vararg listTodos: TodoDto) {
        for (todo in listTodos) steps.deleteTodoById(todo.id)
        steps.checkEmptyDatabase()
    }
    
    @AfterEach
    fun release() {
        steps.softAssertion.assertAll()
    }
}

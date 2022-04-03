import dto.TodoDto
import io.qameta.allure.Description
import io.qameta.allure.Story
import org.apache.http.HttpStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.stream.Stream

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TodoRestTest {
    companion object {
        const val TODO_PORT = 4242
        private const val APP_NAME = "todo-app"
        val steps = Steps()

        @Container
        var env: GenericContainer<*> = GenericContainer(DockerImageName.parse(APP_NAME)).withExposedPorts(TODO_PORT)

        @JvmStatic
        fun todoDtoProvider(): Stream<TodoDto> = Stream.of(TodoDto(-1), TodoDto(1, "1", null))
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
        steps.createTodoAndCheckStatus(postTodo)
        val getPostedTodo = steps.getListCurrentTodos()[0]
        steps.assertEquals(postTodo, getPostedTodo)
        val updatedTodo = steps.createRandomTodoDTO()
        steps.updateExistingTodoAndCheckStatus(getPostedTodo, updatedTodo)
        val getUpdatedTodo = steps.getListCurrentTodos()[0]
        steps.assertEquals(updatedTodo, getUpdatedTodo)
        deleteTodosAndCheck(updatedTodo)
    }

    @Story("Check GET with limit and offset")
    @Description(
        "Create 2 TODOs " +
                "Check that /GET with limit 1 and offset 1 returned SECOND Todo (not first) " +
                "Clear database after test"
    )
    @Tag("api")
    @Test
    fun getTodoWithLimitOffset() {
        val postFirstTodo = steps.createRandomTodoDTO()
        steps.createTodoAndCheckStatus(postFirstTodo)
        val postSecondTodo = steps.createRandomTodoDTO()
        steps.createTodoAndCheckStatus(postSecondTodo)
        val getSecondTodo = steps.getTodoWithLimitOffset(1, 1)
        steps.assertEquals(postSecondTodo, getSecondTodo)
        deleteTodosAndCheck(postFirstTodo, postSecondTodo)
    }

    @Story("Check zero limit")
    @Description(
        "Create TODO " +
                "Check that /GET with limit 0 return zero TODOs " +
                "Clear database after test"
    )
    @Tag("api")
    @Test
    fun getTodoWithZeroLimit() {
        val postTodo = steps.createRandomTodoDTO()
        steps.createTodoAndCheckStatus(postTodo)
        steps.checkRequestedTodoIsEmpty(steps.getListCurrentTodos(0))
        deleteTodosAndCheck(postTodo)
    }

    @Story("Check impossibility to change a non-existent TODO")
    @Description(
        "Create TODO " +
                "Trying to change a non-existent TODO " +
                "Catch exception with 404 HTTP code " +
                "Clear database after test"
    )
    @Tag("api")
    @Test
    fun impossibleToChangeTodo() {
        val postTodo = steps.createRandomTodoDTO()
        steps.createTodoAndCheckStatus(postTodo)
        val response = steps.updateExistingTodo(steps.createRandomTodoDTO(), postTodo)
        steps.assertResponseStatus(response!!, HttpStatus.SC_NOT_FOUND)
        deleteTodosAndCheck(postTodo)
    }

    @Story("Check app catches unrealized methods")
    @Description("Trying to request a non-existent method then app catching exception with 405 HTTP code")
    @Tag("api")
    @Test
    fun catchUnrealizedMethod() {
        steps.assertResponseStatus(steps.requestNonExistentMethod()!!, HttpStatus.SC_METHOD_NOT_ALLOWED)
    }

    @Story("Add invalid values when creating TODO")
    @Description("Trying to add invalid values to POST method and check bad request exception")
    @Tag("api")
    @ParameterizedTest
    @MethodSource("todoDtoProvider")
    fun parameterizedInvalidValues(todoDto: TodoDto) {
        steps.assertResponseStatus(steps.createTodo(todoDto)!!, HttpStatus.SC_BAD_REQUEST)
    }

    @Story("App works only with JSON media type")
    @Description("Trying to request method with text content type then app catching exception with 415 HTTP code")
    @Tag("api")
    @Test
    fun catchUnsupportedMediaType() {
        steps.assertResponseStatus(steps.brokenPostWithTextContentType(), HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE)
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

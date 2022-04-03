import TodoRestTest.Companion.TODO_PORT
import TodoRestTest.Companion.env
import dto.TodoDto
import io.qameta.allure.Step
import io.restassured.RestAssured.given
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.apache.http.HttpStatus
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class Steps {
    val softAssertion = SoftAssertions()

    private fun requestSpec(): RequestSpecification? {
        return RequestSpecBuilder().setBaseUri(EndPoints.BASE_URI).setPort(env.getMappedPort(TODO_PORT))
            .setContentType(ContentType.JSON).build().log().all()
    }

    @Step("Compare posted TODO and received one")
    fun assertEquals(firstTodo: TodoDto, secondTodo: TodoDto) {
        softAssertion.assertThat(firstTodo).isEqualTo(secondTodo)
    }

    @Step("Check that database is cleared")
    fun checkEmptyDatabase() {
        softAssertion.assertThat(getListCurrentTodos()).isEmpty()
    }

    @Step("Check that requested TODO list is empty")
    fun checkRequestedTodoIsEmpty(requestedTodo: MutableList<TodoDto>) {
        softAssertion.assertThat(requestedTodo).isEmpty()
    }

    @Step("POST /todos create one TODO task and check 201 status")
    fun createTodoAndCheckStatus(todoDto: TodoDto) {
        privateCreateTodo(todoDto).then().statusCode(HttpStatus.SC_CREATED)
    }

    @Step("POST /todos create one TODO task")
    fun createTodo(todoDto: TodoDto): Response? {
        return privateCreateTodo(todoDto)
    }

    private fun privateCreateTodo(todoDto: TodoDto) = given(requestSpec()).body(todoDto).post()

    @Step("DELETE /todos by id")
    fun deleteTodoById(id: Long) {
        val username = "admin"
        val password = "admin"
        given(requestSpec()).auth().preemptive().basic(username, password).basePath(id.toString()).delete().then()
            .statusCode(HttpStatus.SC_NO_CONTENT)
    }

    @Step("GET /todos list")
    fun getListCurrentTodos(limit: Int = Integer.MAX_VALUE): MutableList<TodoDto> {
        return given(requestSpec())
            .queryParam("limit", limit)
            .get().then().extract().jsonPath().getList("", TodoDto::class.java)
    }

    @Step("GET one from /todos with limit&offset")
    fun getTodoWithLimitOffset(limit: Int = 1, offset: Int = 0): TodoDto {
        return given(requestSpec())
            .queryParam("limit", limit)
            .queryParam("offset", offset)
            .get().then().extract().jsonPath().getList("", TodoDto::class.java)[0]
    }

    @Step("Generate TODO DTO")
    fun createRandomTodoDTO(completed: Boolean = false): TodoDto {
        val until = 100L
        return TodoDto(
            Random.nextLong(until), UUID.randomUUID().toString(), completed
        )
    }

    @Step("PUT /todos update an existing TODO and check 200 status")
    fun updateExistingTodoAndCheckStatus(previousTodo: TodoDto, updatedTodo: TodoDto) {
        updateTodo(previousTodo, updatedTodo).then().statusCode(HttpStatus.SC_OK)
    }

    @Step("PUT /todos update an existing TODO")
    fun updateExistingTodo(previousTodo: TodoDto, updatedTodo: TodoDto): Response? {
        return updateTodo(previousTodo, updatedTodo)
    }

    @Step("DELETE with non-existent path")
    fun requestNonExistentMethod(): Response? {
        return given(requestSpec()).delete()
    }

    @Step("POST with text content type")
    fun brokenPostWithTextContentType(): Response {
        val randomString = "1"
        return given(requestSpec()!!.contentType(ContentType.TEXT)).body(randomString).post()
    }

    @Step("Check response status")
    fun assertResponseStatus(response: Response, httpStatus: Int) {
        softAssertion.assertThat(response.statusCode).isEqualTo(httpStatus)
    }

    private fun updateTodo(previousTodo: TodoDto, updatedTodo: TodoDto) =
        given(requestSpec()).basePath(previousTodo.id.toString()).body(updatedTodo).put()
}
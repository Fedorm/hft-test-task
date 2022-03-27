import TodoRestTest.Companion.TODO_PORT
import TodoRestTest.Companion.env
import dto.TodoDto
import io.qameta.allure.Step
import io.restassured.RestAssured.given
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
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

    @Step("POST /todos and create one TODO task")
    fun createTodo(todoDto: TodoDto) {
        given(requestSpec()).body(todoDto).post()
            .then().statusCode(HttpStatus.SC_CREATED)
    }

    @Step("DELETE /todos by id")
    fun deleteTodoById(id: Long) {
        val username = "admin"
        val password = "admin"
        given(requestSpec()).auth().preemptive().basic(username, password).basePath(id.toString()).delete().then()
            .statusCode(HttpStatus.SC_NO_CONTENT)
    }

    @Step("GET /todos list without limit&offset")
    fun getListCurrentTodos(): MutableList<TodoDto> {
        return given(requestSpec())
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

    @Step("PUT /todos update an existing TODO")
    fun updateExistingTodo(previousTodo: TodoDto, updatedTodo: TodoDto) {
        given(requestSpec()).basePath(previousTodo.id.toString()).body(updatedTodo).put().then()
            .statusCode(HttpStatus.SC_OK)
    }
}
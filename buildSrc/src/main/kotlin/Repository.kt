import org.gradle.api.Project
import java.net.URI

object Repository {
    fun publishUrl(project: Project): URI = project.uri("repository_url")
}
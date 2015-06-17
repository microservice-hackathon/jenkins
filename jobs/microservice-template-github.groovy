import com.ofg.pipeline.domain.GitProject
import com.ofg.pipeline.domain.GitProjectFetcher
import com.ofg.pipeline.template.MicroserviceTemplateBuilder

def organization = binding.variables['ORGANIZATION_NAME'] ?: 'microhackathon-test'
def reposApi = new URL("https://api.github.com/orgs/${organization}/repos")
def repos = new GitProjectFetcher(binding.variables['TEST_MODE'] ?: false, reposApi).fetchRepos()
def projectsToExclude =  ((binding.variables['PROJECTS_TO_EXCLUDE'] as String)?.split(',') as List) ?: ["${organization}.github.io", 'properties']

List projectToCode = repos.findAll {!projectsToExclude.contains(it.name)}

MicroserviceTemplateBuilder.pipeline(this) {
    forProjects projectToCode.collect { new GitProject(it.name, it.clone_url) }
    buildGithubPrs {
        organizationUrl "https://github.com/$organization"
        cronToPollScm '*/2 * * * *'
        organizationName organization
        whitelistedUsers(['microservice-hackathon-bot'])
    }
    buildJobs()
    buildViews()
}


import pl.wybcz.pipeline.domain.GitProject
import pl.wybcz.pipeline.domain.GitProjectFetcher
import pl.wybcz.pipeline.template.MicroserviceTemplateBuilder

def stashServerHost = '52.17.120.44:7990'
def stashProjectCode = 'PRs'
def reposApi = new URL("http://$stashServerHost/rest/api/1.0/projects/$stashProjectCode/repos")
def repos = new GitProjectFetcher(binding.variables['TEST_MODE'] ?: false, reposApi).fetchRepos()

MicroserviceTemplateBuilder.pipeline(this) {
    forProjects repos.collect { new GitProject('test-repo', "http://${STASH_USERNAME}@$stashServerHost/scm/prs/test-repo.git") }
    buildStashPrs {
        stashHost stashServerHost
        cronToPollScm '*/2 * * * *'
        repoName 'test-repo'
        projectCode stashProjectCode
        username "${STASH_USERNAME}"
        password "${STASH_PASSWORD}"
    }
    buildJobs()
    buildViews { List<GitProject> project ->
         return [stash : [project.name]]
    }
}



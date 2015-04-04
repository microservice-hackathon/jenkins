import pl.wybcz.pipeline.domain.GitProject
import pl.wybcz.pipeline.template.MicroserviceTemplateBuilder

def stashServerHost = '52.17.120.44:7990'
def stashProjectCode = 'PRs'

MicroserviceTemplateBuilder.pipeline(this) {
    forProjects([new GitProject('test-repo', "http://${STASH_USERNAME}@$stashServerHost/scm/prs/test-repo.git")])
    buildStashPrs {
        stashHost stashServerHost
        cronToPollScm '*/2 * * * *'
        repoName 'test-repo'
        projectCode stashProjectCode
        username "${STASH_USERNAME}"
        password "${STASH_PASSWORD}"
    }
    buildJobs()
    buildViews { List<GitProject> projectToCode ->
        return [stash: projectToCode.collect {it.name}]
    }
}



import pl.wybcz.pipeline.domain.GitProject
import pl.wybcz.pipeline.template.MicroserviceTemplateBuilder
import pl.wybcz.pipeline.template.RealmConverter

def stashServerHost = 'http://52.17.120.44:7990'
def stashProjectCode = 'PRs'
def repoToBuild = 'test-repo'

MicroserviceTemplateBuilder.pipeline(this) {
    forProjects([new GitProject(repoToBuild, "http://${STASH_USERNAME}@$stashServerHost/scm/${stashProjectCode.toLowerCase()}/${repoToBuild}.git")])
    buildStashPrs {
        stashHost stashServerHost
        cronToPollScm '*/2 * * * *'
        repoName repoToBuild
        projectCode stashProjectCode
        username "${STASH_USERNAME}"
        password "${STASH_PASSWORD}"
    }
    buildJobs()
    buildViews new RealmConverter() {
        @Override
        Map<String, List<String>> convertToRealmMultimap(List<GitProject> projectToCode) {
            return [stash: projectToCode.collect {it.name}]
        }
    }
}



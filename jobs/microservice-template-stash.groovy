import pl.wybcz.pipeline.domain.GitProject
import pl.wybcz.pipeline.template.MicroserviceTemplateBuilder
import pl.wybcz.pipeline.template.RealmConverter

def stashServerHost = "${STASH_HOST}"
def stashProjectCode = "${STASH_PROJECT}"
def repoToBuild = "${STASH_REPO}"

MicroserviceTemplateBuilder.pipeline(this) {
    forProjects([new GitProject(repoToBuild, "http://${STASH_USERNAME}@$stashServerHost/scm/${stashProjectCode.toLowerCase()}/${repoToBuild}.git")])
    buildStashPrs {
        stashHost stashServerHost
        cronToPollScm '*/2 * * * *'
        repoName repoToBuild
        projectCode stashProjectCode
        buildSteps {
            gradle('clean build')
        }
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



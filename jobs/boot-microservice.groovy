import pl.wybcz.GitProject
import pl.wybcz.MicroserviceTemplateBuilder

def organization = 'microhackathon-test'
def reposApi = new URL("https://api.github.com/orgs/${organization}/repos")
def repos = new groovy.json.JsonSlurper().parse(reposApi.newReader())

List projectToCode = repos.findAll {!(it.name == "${organization}.github.io" || it.name == "properties")}

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



import pl.wybcz.HackathonRealmParser
import pl.wybcz.MicroserviceTemplateBuilder

def organization = 'microhackathon-2015-03-juglodz'
def reposApi = new URL("https://api.github.com/orgs/${organization}/repos")
def repos = new groovy.json.JsonSlurper().parse(reposApi.newReader())

List projectToCode = repos.findAll {!(it.name == "${organization}.github.io" || it.name == "properties")}

MicroserviceTemplateBuilder microserviceTemplateBuilder = new MicroserviceTemplateBuilder(this,
        'https://github.com/microhackathon-2015-03-juglodz',
        '*/2 * * * *',
        organization,
        ['microservice-hackathon-bot']
)

projectToCode.each {
    microserviceTemplateBuilder.buildJobs(it.name, it.clone_url)
}

Map<String, List<String>> realmMultimap = new HackathonRealmParser().convertToRealmMultimap(projectToCode)
realmMultimap.each { String realm, List<String> projects ->
    microserviceTemplateBuilder.buildViews(realm, projects)
}


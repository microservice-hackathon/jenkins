import pl.wybcz.HackathonRealmParser
import pl.wybcz.MicroserviceTemplateBuilder

def organization = 'microhackathon-2015-03-juglodz'
def reposApi = new URL("https://api.github.com/orgs/${organization}/repos")
def repos = new groovy.json.JsonSlurper().parse(reposApi.newReader())

List projectToCode = repos.findAll {!(it.name == "${organization}.github.io" || it.name == "properties")}

projectToCode.each {
    new MicroserviceTemplateBuilder(this).buildJobs(it.name, it.clone_url)
}

Map<String, List<String>> realmMultimap = new HackathonRealmParser().convertToRealmMultimap(projectToCode)
realmMultimap.each { String realm, List<String> projects ->
    new MicroserviceTemplateBuilder(this).buildViews(realm, projects)
}


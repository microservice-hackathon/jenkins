import pl.wybcz.MicroserviceTemplateBuilder

def organization = 'microhackathon-2015-03-juglodz'
def reposApi = new URL("https://api.github.com/orgs/${organization}/repos")
def repos = new groovy.json.JsonSlurper().parse(reposApi.newReader())

List projectToCode = repos.findAll {!(it.name == "${organization}.github.io" || it.name == "properties")}
projectToCode.each {
    String projectName = it.name
    def projectGitRepo = it.clone_url

    new MicroserviceTemplateBuilder(
            projectName: projectName,
            projectGitRepo: projectGitRepo
    ).build(this)
}

Map<String, List<String>> realmMultimap = projectToCode.inject([:]) { acc, entry ->
    String realm = entry.name.split('-').last()
    if(acc[realm] == null) {
        acc[realm] = [entry.name]
    } else {
        acc[realm] << entry.name
    }
    return acc
}

realmMultimap.each { String realm, List<String> projects ->
    nestedView(realm) {
        views {
            projects.each { String project ->
                view("${project}-pipeline", type: BuildPipelineView) {
                    filterBuildQueue()
                    filterExecutors()
                    title("${project} Pipeline")
                    displayedBuilds(5)
                    selectedJob("${project}-build")
                    alwaysAllowManualTrigger()
                    showPipelineParameters()
                    refreshFrequency(5)
                }
            }
        }
    }

    deliveryPipelineView("${realm}-delivery") {
        pipelineInstances(0)
        showAggregatedPipeline()
        columns(1)
        sorting(Sorting.TITLE)
        updateInterval(5)
        enableManualTriggers()
        showAvatars()
        showChangeLog()
        pipelines {
            projects.each {
                component("Deploy $it to production", "${it}-build")
            }
        }
    }

}


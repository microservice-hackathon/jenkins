
def organization = 'microhackathon-2015-03-juglodz'
def reposApi = new URL("https://api.github.com/orgs/${organization}/repos")
def repos = new groovy.json.JsonSlurper().parse(reposApi.newReader())

List projectToCode = repos.findAll {!(it.name == "${organization}.github.io" || it.name == "properties")}
projectToCode.each {
    String projectName = it.name
    def projectGitRepo = it.clone_url

    job("${projectName}-build") {
        deliveryPipelineConfiguration('Build', 'Build')
        wrappers {
            deliveryPipelineVersion('CD-${BUILD_NUMBER}', true)
        }
        scm {
            git(projectGitRepo, 'master')
        }
        steps {
            gradle('build -x test')
        }
        publishers {
            downstreamParameterized {
                trigger("${projectName}-publish", 'SUCCESS', true) {
                    currentBuild()
                    sameNode()
                    gitRevision()
                }
            }
        }
    }

    job("${projectName}-publish") {
        deliveryPipelineConfiguration('Build', 'Publish')
        wrappers {
            deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
        }
        scm {
            git(projectGitRepo, 'master')
        }
        steps {
            gradle('build -x test')
        }
        publishers {
            downstreamParameterized {
                trigger("${projectName}-deploy-stub-runner", 'SUCCESS', true) {
                    currentBuild()
                    sameNode()
                    gitRevision()
                }
            }
        }
    }

    job("${projectName}-deploy-stub-runner") {
        deliveryPipelineConfiguration('Smoke tests', 'Deploy stub-runner')
        wrappers {
            deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
        }
        scm {
            git(projectGitRepo, 'master')
        }
        steps {
            gradle('build -x test')
        }
        publishers {
            downstreamParameterized {
                trigger("${projectName}-deploy-app", 'SUCCESS', true) {
                    currentBuild()
                    sameNode()
                    gitRevision()
                }
            }
        }
    }

    job("${projectName}-deploy-app") {
        deliveryPipelineConfiguration('Smoke tests', 'Deploy app')
        wrappers {
            deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
        }
        scm {
            git(projectGitRepo, 'master')
        }
        steps {
            gradle('build -x test')
        }
        publishers {
            downstreamParameterized {
                trigger("${projectName}-run-smoke-tests", 'SUCCESS', true) {
                    currentBuild()
                    sameNode()
                    gitRevision()
                }
            }
        }
    }

    job("${projectName}-run-smoke-tests") {
        deliveryPipelineConfiguration('Smoke tests', 'Run smoke tests')
        wrappers {
            deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
        }
        scm {
            git(projectGitRepo, 'master')
        }
        steps {
            gradle('build -x test')
        }
        publishers {
            downstreamParameterized {
                trigger("${projectName}-deploy-previous-version", 'SUCCESS', true) {
                    currentBuild()
                    sameNode()
                    gitRevision()
                }
            }
        }
    }

    job("${projectName}-deploy-previous-version") {
        deliveryPipelineConfiguration('Smoke tests', 'Deploy previous version')
        wrappers {
            deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
        }
        scm {
            git(projectGitRepo, 'master')
        }
        steps {
            gradle('build -x test')
        }
        publishers {
            downstreamParameterized {
                trigger("${projectName}-run-smoke-tests-on-old-jar", 'SUCCESS', true) {
                    currentBuild()
                    sameNode()
                    gitRevision()
                }
            }
        }
    }

    job("${projectName}-run-smoke-tests-on-old-jar") {
        deliveryPipelineConfiguration('Smoke tests', 'Run smoke tests on old jar')
        wrappers {
            deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
        }
        scm {
            git(projectGitRepo, 'master')
        }
        steps {
            gradle('build -x test')
        }
        publishers {
            downstreamParameterized {
                trigger("${projectName}-deploy-to-prod", 'SUCCESS', true) {
                    currentBuild()
                    sameNode()
                    gitRevision()
                }
            }
        }
    }

    job("${projectName}-deploy-to-prod") {
        deliveryPipelineConfiguration('Deploy to prod', 'Deploy to prod')
        wrappers {
            deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
        }
        scm {
            git(projectGitRepo, 'master')
        }
        steps {
            gradle('build -x test')
        }
    }
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


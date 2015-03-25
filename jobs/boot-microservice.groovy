
def organization = 'microhackathon-2015-03-juglodz'
def reposApi = new URL("https://api.github.com/orgs/${organization}/repos")
def repos = new groovy.json.JsonSlurper().parse(reposApi.newReader())

List projectToCode = repos.collect {
    String projectName = it.name
    return !(projectName == "${organization}.github.io" || projectName == "properties")
}
projectToCode.each {
    String projectName = it.name
    def projectGitRepo = it.clone_url

    job("${projectName}-build") {
        deliveryPipelineConfiguration('Build')
        wrappers {
            deliveryPipelineVersion("""1.0.0.\${GROOVY,script = "return new Date().format('yyyyMMddHHmmss')"}""", true)
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
        deliveryPipelineConfiguration('Build')
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
        deliveryPipelineConfiguration('Smoke tests')
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
        deliveryPipelineConfiguration('Smoke tests')
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
        deliveryPipelineConfiguration('Smoke tests')
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
        deliveryPipelineConfiguration('Smoke tests')
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
        deliveryPipelineConfiguration('Smoke tests')
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
        deliveryPipelineConfiguration('Deploy to prod')
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
    String realm = entry.split('-').last()
    String firstPart = entry - '-' - realm
    if(acc[realm] == null) {
        acc[realm] = [firstPart]
    } else {
        acc[realm] << firstPart
    }
    return acc
}

realmMultimap.each { String realm, List<String> projects ->
    nestedView(realm) {
        views {
            view('overview') {
                jobs {
                    regex("^.*-$realm-.*\$")
                }
                columns {
                    status()
                    name()
                }
            }
            projects.each {
                buildPipelineView("${it}-pipeline") {
                    filterBuildQueue()
                    filterExecutors()
                    title("${it} Pipeline")
                    displayedBuilds(5)
                    selectedJob("${it}-build")
                    alwaysAllowManualTrigger()
                    showPipelineParameters()
                    refreshFrequency(5)
                }
            }
        }
    }

    deliveryPipelineView("${realm}-delivery") {
        pipelineInstances(1)
        showAggregatedPipeline()
        columns(1)
        sorting(Sorting.TITLE)
        updateInterval(5)
        enableManualTriggers()
        showAvatars()
        showChangeLog()
        pipelines {
            projects.each {
                component('Deploy to production', "${it}-build")
            }
        }
    }

}


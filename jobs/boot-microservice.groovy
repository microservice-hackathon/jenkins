
def organization = 'microhackathon-2015-03-juglodz'
def reposApi = new URL("https://api.github.com/orgs/${organization}/repos")
def repos = new groovy.json.JsonSlurper().parse(reposApi.newReader())

repos.each {
    def projectName = it.name
    def projectGitRepo = it.clone_url

    if (projectName == "${organization}.github.io" || projectName == "properties") {
        return
    }
    
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
    
    buildPipelineView("${projectName}-pipeline") {
        filterBuildQueue()
        filterExecutors()
        title('Boot-microservice Pipeline')
        displayedBuilds(5)
        selectedJob("${projectName}-build")
        alwaysAllowManualTrigger()
        showPipelineParameters()
        refreshFrequency(5)
    }
    
    deliveryPipelineView("${projectName}-delivery") {
        pipelineInstances(1)
        showAggregatedPipeline()
        columns(1)
        sorting(Sorting.TITLE)
        updateInterval(5)
        enableManualTriggers()
        showAvatars()
        showChangeLog()
        pipelines {
            component('Deploy microservice to production', "${projectName}-build")
        }
    }
}

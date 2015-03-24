def project = '4finance/boot-microservice'
def branchApi = new URL("https://api.github.com/repos/${project}/branches")
def branches = new groovy.json.JsonSlurper().parse(branchApi.newReader())

job('build') {
    deliveryPipelineConfiguration('Build')
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger('publish', 'SUCCESS', true) {
                currentBuild()
                sameNode()
            }
        }
    }
}

job('publish') {
    deliveryPipelineConfiguration('Build')
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger('deploy-stub-runner', 'SUCCESS', true) {
                currentBuild()
                sameNode()
            }
        }
    }
}

job('deploy-stub-runner') {
    deliveryPipelineConfiguration('Smoke tests')
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger('deploy-app', 'SUCCESS', true) {
                currentBuild()
                sameNode()
            }
        }
    }
}

job('deploy-app') {
    deliveryPipelineConfiguration('Smoke tests')
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger('run-smoke-tests', 'SUCCESS', true) {
                currentBuild()
                sameNode()
            }
        }
    }
}

job('run-smoke-tests') {
    deliveryPipelineConfiguration('Smoke tests')
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger('deploy-previous-version', 'SUCCESS', true) {
                currentBuild()
                sameNode()
            }
        }
    }
}

job('deploy-previous-version') {
    deliveryPipelineConfiguration('Smoke tests')
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger('run-smoke-tests-on-old-jar', 'SUCCESS', true) {
                currentBuild()
                sameNode()
            }
        }
    }
}

job('run-smoke-tests-on-old-jar') {
    deliveryPipelineConfiguration('Smoke tests')
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger('deploy-to-prod', 'SUCCESS', true) {
                currentBuild()
                sameNode()
            }
        }
    }
}

job('deploy-to-prod') {
    deliveryPipelineConfiguration('Deploy to prod')
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('build -x test')
    }
}

buildPipelineView('boot-microservice-build') {
    filterBuildQueue()
    filterExecutors()
    title('Boot-microservice Pipeline')
    displayedBuilds(5)
    selectedJob('build')
    alwaysAllowManualTrigger()
    showPipelineParameters()
    refreshFrequency(60)
}

deliveryPipelineView('boot-microservice-delivery') {
    pipelineInstances(10)
    showAggregatedPipeline()
    columns(2)
    sorting(Sorting.TITLE)
    updateInterval(60)
    enableManualTriggers()
    showAvatars()
    showChangeLog()
    pipelines {
        component('Deploy microservice to production', 'build')
    }
}
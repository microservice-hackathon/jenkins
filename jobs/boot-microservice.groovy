def project = '4finance/boot-microservice'
def branchApi = new URL("https://api.github.com/repos/${project}/branches")
def branches = new groovy.json.JsonSlurper().parse(branchApi.newReader())

job('publish') {
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('clean build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger('deploy-stub-runner', 'SUCCESS', true) {
                currentBuild()
            }
        }
    }
}

job('deploy-stub-runner') {
    steps {
        gradle('clean build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger('deploy-app', 'SUCCESS', true) {
                currentBuild()
            }
        }
    }
}

job('deploy-app') {
    steps {
        gradle('clean build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger('run-smoke-test', 'SUCCESS', true) {
                currentBuild()
            }
        }
    }
}

job('run-smoke-tests') {
    steps {
        gradle('clean build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger('deploy-previous-version', 'SUCCESS', true) {
                currentBuild()
            }
        }
    }
}

job('deploy-previous-version') {
    steps {
        gradle('clean build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger('run-smoke-tests-on-old-jar', 'SUCCESS', true) {
                currentBuild()
            }
        }
    }
}

job('run-smoke-tests-on-old-jar') {
    steps {
        gradle('clean build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger('deploy-to-prod', 'SUCCESS', true) {
                currentBuild()
            }
        }
    }
}

job('deploy-to-prod') {
    steps {
        gradle('clean build -x test')
    }
}

deliveryPipelineView('boot-microservice') {
    pipelineInstances(10)
    showAggregatedPipeline()
    columns(2)
    sorting(Sorting.TITLE)
    updateInterval(60)
    enableManualTriggers()
    showAvatars()
    showChangeLog()
    pipelines {
        component('Build and publish the app', 'publish')
        component('Deploy stub-runner to stg', 'deploy-stub-runner')
        component('Deploy app to stg', 'deploy-app')
        component('Run smoke tests', 'run-smoke-tests')
        component('Deploy previous version of the app', 'deploy-previous-version')
        component('Run smoke tests', 'run-smoke-tests')
        component('Run smoke tests on old jar', 'run-smoke-tests-on-old-jar')
        component('Deploy to prod', 'deploy-to-prod')
    }
}
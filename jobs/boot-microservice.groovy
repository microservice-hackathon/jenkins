def project = '4finance/boot-microservice'
def branchApi = new URL("https://api.github.com/repos/${project}/branches")
def branches = new groovy.json.JsonSlurper().parse(branchApi.newReader())

job('build') {
    deliveryPipelineConfiguration('Build')
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

job('publish') {
    deliveryPipelineConfiguration('Build')
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
    deliveryPipelineConfiguration('Smoke tests')
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
    deliveryPipelineConfiguration('Smoke tests')
    steps {
        gradle('clean build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger('run-smoke-tests', 'SUCCESS', true) {
                currentBuild()
            }
        }
    }
}

job('run-smoke-tests') {
    deliveryPipelineConfiguration('Smoke tests')
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
    deliveryPipelineConfiguration('Smoke tests')
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
    deliveryPipelineConfiguration('Smoke tests')
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
    deliveryPipelineConfiguration('Deploy to prod')
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
        component('Deploy microservice to production', 'publish')
    }
}
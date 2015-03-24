def project = '4finance/boot-microservice'
def branchApi = new URL("https://api.github.com/repos/${project}/branches")
def branches = new groovy.json.JsonSlurper().parse(branchApi.newReader())

job('publish') {
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('clean build')
    }
}

job('deploy-stub-runner') {
    steps {
        gradle('clean build')
    }
}

job('deploy-app') {
    steps {
        gradle('clean build')
    }
}

job('run-smoke-tests') {
    steps {
        gradle('clean build')
    }
}

job('deploy-previous-version') {
    steps {
        gradle('clean build')
    }
}

job('deploy-to-prod') {
    steps {
        gradle('clean build')
    }
}

view(type: DeliveryPipelineView) {
    name('boot-microservice')
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
        component('Deploys previous version of the app', 'deploy-previous-version')
        component('Run smoke tests', 'run-smoke-tests')
        component('Deploy to prod', 'deploy-to-prod')
    }
}
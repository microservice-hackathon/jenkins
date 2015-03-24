
def project = '4finance/boot-microservice'
def project_name = 'boot-microservice'
def branchApi = new URL("https://api.github.com/repos/${project}/branches")
def branches = new groovy.json.JsonSlurper().parse(branchApi.newReader())

job("${project_name}-build") {
    deliveryPipelineConfiguration('Build')
	displayName("${project_name} - build")
    wrappers {
        deliveryPipelineVersion("""1.0.0.\${GROOVY,script = "return new Date().format('yyyyMMddHHmmss')"}""", true)
    }
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger("${project_name}-publish", 'SUCCESS', true) {
                currentBuild()
                sameNode()
                gitRevision()
            }
        }
    }
}

job("${project_name}-publish") {
    deliveryPipelineConfiguration('Build')
	displayName("${project_name} - publish artifacts")
    wrappers {
        deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
    }
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger("${project_name}-deploy-stub-runner", 'SUCCESS', true) {
                currentBuild()
                sameNode()
                gitRevision()
            }
        }
    }
}

job("${project_name}-deploy-stub-runner") {
	displayName("${project_name} - deploy stub-runner")
    deliveryPipelineConfiguration('Smoke tests')
    wrappers {
        deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
    }
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger("${project_name}-deploy-app", 'SUCCESS', true) {
                currentBuild()
                sameNode()
                gitRevision()
            }
        }
    }
}

job("${project_name}-deploy-app") {
	displayName("${project_name} - deploy application")
    deliveryPipelineConfiguration('Smoke tests')
    wrappers {
        deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
    }
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger("${project_name}-run-smoke-tests", 'SUCCESS', true) {
                currentBuild()
                sameNode()
                gitRevision()
            }
        }
    }
}

job("${project_name}-run-smoke-tests") {
    deliveryPipelineConfiguration('Smoke tests')
	displayName("${project_name} - run smoke tests")
    wrappers {
        deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
    }
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger("${project_name}-deploy-previous-version", 'SUCCESS', true) {
                currentBuild()
                sameNode()
                gitRevision()
            }
        }
    }
}

job("${project_name}-deploy-previous-version") {
    deliveryPipelineConfiguration('Smoke tests')
	displayName("${project_name} - deploy prev version")
    wrappers {
        deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
    }
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger("${project_name}-run-smoke-tests-on-old-jar", 'SUCCESS', true) {
                currentBuild()
                sameNode()
                gitRevision()
            }
        }
    }
}

job("${project_name}-run-smoke-tests-on-old-jar") {
    deliveryPipelineConfiguration('Smoke tests')
	displayName("${project_name} - run smoke tests on prev version")
    wrappers {
        deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
    }
    scm {
        git("git://github.com/${project}.git", 'master')
    }
    steps {
        gradle('build -x test')
    }
    publishers {
        downstreamParameterized {
            trigger("${project_name}-deploy-to-prod", 'SUCCESS', true) {
                currentBuild()
                sameNode()
                gitRevision()
            }
        }
    }
}

job("${project_name}-deploy-to-prod") {
    deliveryPipelineConfiguration('Deploy to prod')
	displayName("${project_name} - deploy to prod")
    wrappers {
        deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
    }
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
    selectedJob("${project_name}-build")
    alwaysAllowManualTrigger()
    showPipelineParameters()
    refreshFrequency(5)
}

deliveryPipelineView('boot-microservice-delivery') {
    pipelineInstances(1)
    showAggregatedPipeline()
    columns(1)
    sorting(Sorting.TITLE)
    updateInterval(5)
    enableManualTriggers()
    showAvatars()
    showChangeLog()
    pipelines {
        component('Deploy microservice to production', "${project_name}-build")
    }
}

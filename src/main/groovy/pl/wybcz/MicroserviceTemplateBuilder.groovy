package pl.wybcz
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.View
import javaposse.jobdsl.dsl.ViewType

class MicroserviceTemplateBuilder {
    String projectName
    String projectGitRepo
    String realm
    List<String> projects

    List<Job> buildJobs(DslFactory dslFactory) {
        return [
                dslFactory.job("${projectName}-build") {
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
                ,
                dslFactory.job("${projectName}-publish") {
                    deliveryPipelineConfiguration('Build', 'Publish')
                    wrappers {
                        deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
                    }
                    scm {
                        git(projectGitRepo, 'master')
                    }
                    steps {
                        gradle('build -x test --info')
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
                ,
                dslFactory.job("${projectName}-deploy-stub-runner") {
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
                ,
                dslFactory.job("${projectName}-deploy-app") {
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
                ,
                dslFactory.job("${projectName}-run-smoke-tests") {
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
                ,
                dslFactory.job("${projectName}-deploy-previous-version") {
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
                ,
                dslFactory.job("${projectName}-run-smoke-tests-on-old-jar") {
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
                ,
                dslFactory.job("${projectName}-deploy-to-prod") {
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
            ]
    }

    List<View> buildViews(DslFactory dslFactory) {
        return [
                    dslFactory.nestedView(realm) {
                        views {
                            projects.each { String projectName ->
                                view("${projectName}-pipeline", type: ViewType.BuildPipelineView) {
                                    filterBuildQueue()
                                    filterExecutors()
                                    title("${projectName} Pipeline")
                                    displayedBuilds(5)
                                    selectedJob("${projectName}-build")
                                    alwaysAllowManualTrigger()
                                    showPipelineParameters()
                                    refreshFrequency(5)
                                }
                            }
                        }
                    }
                    ,
                    dslFactory.deliveryPipelineView("${realm}-delivery") {
                        pipelineInstances(0)
                        showAggregatedPipeline()
                        columns(1)
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
                ]
    }
}

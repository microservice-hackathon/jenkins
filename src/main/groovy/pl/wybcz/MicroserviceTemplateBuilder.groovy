package pl.wybcz

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.View
import javaposse.jobdsl.dsl.ViewType

class MicroserviceTemplateBuilder {

    private final DslFactory dslFactory
    private final GithubPrBuilder githubPrBuilder
    private final String organizationUrl
    private final String cronToPollScm
    private final String organizationName
    private final List<String> whitelistedUsers

    MicroserviceTemplateBuilder(DslFactory dslFactory,
    String organizationUrl ,
    String cronToPollScm,
    String organizationName,
    List<String> whitelistedUsers) {
        this.dslFactory = dslFactory
        this.githubPrBuilder = new GithubPrBuilder(dslFactory)
        this.organizationUrl = organizationUrl
        this.cronToPollScm = cronToPollScm
        this.organizationName = organizationName
        this.whitelistedUsers = whitelistedUsers
    }

    List<Job> buildJobs(String projectName, String projectGitRepo) {
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
                ,
                githubPrBuilder.buildPrJob(organizationUrl, projectName, cronToPollScm, organizationName, whitelistedUsers)
            ]
    }

    List<View> buildViews(String realm, List<String> projects) {
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
                                view("${projectName}-pr", type: ViewType.ListView) {
                                    jobs{
                                        name("${projectName}-pr-build")
                                    }
                                    columns {
                                        status()
                                        weather()
                                        name()
                                        lastSuccess()
                                        lastFailure()
                                        lastDuration()
                                        buildButton()
                                    }
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

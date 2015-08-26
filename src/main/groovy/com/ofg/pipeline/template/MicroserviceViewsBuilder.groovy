package com.ofg.pipeline.template

import javaposse.jobdsl.dsl.*
import javaposse.jobdsl.dsl.views.NestedView
import javaposse.jobdsl.dsl.views.NestedViewsContext

class MicroserviceViewsBuilder {

    private final DslFactory dslFactory

    MicroserviceViewsBuilder(DslFactory dslFactory) {
        this.dslFactory = dslFactory
    }

    List<View> buildViews(String realm, List<String> projects) {
        return [
                dslFactory.nestedView("${realm}") {
                    views {
                        nestedView("${realm}-pipelines") {
                            NestedView nestedView = delegate
                            projects.each { String projectName ->
                                nestedView.views {
                                NestedViewsContext context = delegate

                                    /*context.buildPipelineView("${projectName}-pipeline") {
                                        filterBuildQueue()
                                        filterExecutors()
                                        title("${projectName} Pipeline")
                                        displayedBuilds(5)
                                        selectedJob("${projectName}-build")
                                        alwaysAllowManualTrigger()
                                        showPipelineParameters()
                                        refreshFrequency(5)
                                    }
                                    context.listView("${projectName}-pr") {
                                        jobs {
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
                                    }*/
                                    context.deliveryPipelineView("${projectName}-delivery") {
                                        pipelineInstances(10)
                                        columns(1)
                                        updateInterval(5)
                                        enableManualTriggers()
                                        showChangeLog()
                                        showAvatars()
                                        pipelines {
                                            component("Deploy $projectName to production", "${projectName}-build")
                                        }
                                        configure {
                                            it / 'allowRebuild'(true)
                                            it / 'allowPipelineStart'(true)
                                        }
                                    }
                                }
                            }
                        }
                        nestedView("${realm}-overview") {
                            views {
                                deliveryPipelineView("${realm}-delivery") {
                                    pipelineInstances(10)
                                    columns(1)
                                    updateInterval(5)
                                    enableManualTriggers()
                                    showChangeLog()
                                    showAvatars()
                                    pipelines {
                                        projects.each {
                                            component("Deploy $it to production", "${it}-build")
                                        }
                                    }
                                    configure {
                                        it / 'allowRebuild'(true)
                                        it / 'allowPipelineStart'(true)
                                    }
                                }
                                buildMonitorView("${realm}-deploy-to-prod-monitor") {
                                    jobs {
                                        regex("^.*${realm}-deploy-to-prod\$")
                                    }
                                }
                            }
                        }
                    }
                },
                dslFactory.nestedView('prod') {
                    views {
                        buildMonitorView("deploy-to-prod-monitor") {
                            jobs {
                                regex("^.*-deploy-to-prod\$")
                            }
                        }
                    }
                }

        ]
    }
}

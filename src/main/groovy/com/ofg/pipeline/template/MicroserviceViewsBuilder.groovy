package com.ofg.pipeline.template

import javaposse.jobdsl.dsl.*
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
                            views {
                                NestedViewsContext context = delegate
                                projects.each { String projectName ->
                                    context.buildPipelineView("${projectName}-pipeline") {
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
                                    }
                                }

                            }
                        }
                        nestedView("${realm}-overview") {
                            views {
                                deliveryPipelineView("${realm}-delivery") {
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
                                buildMonitorView("${realm}-deploy-to-prod-monitor") {
                                    jobs {
                                        regex("^.*${realm}-deploy-to-prod\$")
                                    }
                                }
                            }
                        }
                    }
                }

        ]
    }
}

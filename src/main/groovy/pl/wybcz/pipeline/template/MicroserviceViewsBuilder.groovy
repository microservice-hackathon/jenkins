package pl.wybcz.pipeline.template

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.View
import javaposse.jobdsl.dsl.ViewType

class MicroserviceViewsBuilder {

    private final DslFactory dslFactory

    MicroserviceViewsBuilder(DslFactory dslFactory) {
        this.dslFactory = dslFactory
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
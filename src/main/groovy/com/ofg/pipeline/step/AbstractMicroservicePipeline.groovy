package com.ofg.pipeline.step

abstract class AbstractMicroservicePipeline {
    Closure downstreamParametrized(String projectName) {
        return {
            downstreamParameterized {
                trigger("${projectName}", 'SUCCESS', true) {
                    currentBuild()
                    sameNode()
                    gitRevision()
                }
            }
        }
    }
}
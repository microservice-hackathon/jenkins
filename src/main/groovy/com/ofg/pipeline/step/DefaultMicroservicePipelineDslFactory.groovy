package com.ofg.pipeline.step

import com.ofg.pipeline.domain.NexusBuilder
import groovy.transform.CompileStatic
import javaposse.jobdsl.dsl.DslFactory

@CompileStatic
class DefaultMicroservicePipelineDslFactory {
    
    private final DslFactory dslFactory
    private final @Delegate MicroservicePipelineBuildDslFactory pipelineBuildDslFactory
    private final @Delegate MicroservicePipelineSmokeTestsDslFactory smokeTestsDslFactory
    private final @Delegate MicroservicePipelineDeployToProdDslFactory deployToProdDslFactory

    DefaultMicroservicePipelineDslFactory(DslFactory dslFactory, NexusBuilder nexusBuilder) {
        this.dslFactory = dslFactory
        this.pipelineBuildDslFactory = new MicroservicePipelineBuildDslFactory(dslFactory, nexusBuilder)
        this.smokeTestsDslFactory = new MicroservicePipelineSmokeTestsDslFactory(dslFactory)
        this.deployToProdDslFactory = new MicroservicePipelineDeployToProdDslFactory(dslFactory, nexusBuilder)
    }

}

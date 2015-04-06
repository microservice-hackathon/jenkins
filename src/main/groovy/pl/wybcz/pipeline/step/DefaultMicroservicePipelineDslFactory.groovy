package pl.wybcz.pipeline.step

import groovy.transform.CompileStatic
import javaposse.jobdsl.dsl.DslFactory

@CompileStatic
class DefaultMicroservicePipelineDslFactory {
    
    private final DslFactory dslFactory
    private final @Delegate MicroservicePipelineBuildDslFactory pipelineBuildDslFactory
    private final @Delegate MicroservicePipelineSmokeTestsDslFactory smokeTestsDslFactory
    private final @Delegate MicroservicePipelineDeployToProdDslFactory deployToProdDslFactory

    DefaultMicroservicePipelineDslFactory(DslFactory dslFactory) {
        this.dslFactory = dslFactory
        this.pipelineBuildDslFactory = new MicroservicePipelineBuildDslFactory(dslFactory)
        this.smokeTestsDslFactory = new MicroservicePipelineSmokeTestsDslFactory(dslFactory)
        this.deployToProdDslFactory = new MicroservicePipelineDeployToProdDslFactory(dslFactory)
    }

}

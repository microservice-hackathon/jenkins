package com.ofg.pipeline.template

import com.ofg.pipeline.domain.NexusBuilder
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import com.ofg.pipeline.pr.PrBuilder
import com.ofg.pipeline.step.DefaultMicroservicePipelineDslFactory

class MicroserviceJobsBuilder {

    private final DefaultMicroservicePipelineDslFactory pipeline
    private final PrBuilder prBuilder

    MicroserviceJobsBuilder(DslFactory dslFactory, PrBuilder prBuilder, NexusBuilder nexusBuilder) {
        this.pipeline = new DefaultMicroservicePipelineDslFactory(dslFactory, nexusBuilder)
        this.prBuilder = prBuilder
    }

    List<Job> buildJobs(String projectName, String projectGitRepo) {
        List<Job> jobs = [
                pipeline.build(projectName, projectGitRepo),
                pipeline.publish(projectName, projectGitRepo),
                pipeline.deployToProd(projectName, projectGitRepo)
        ]
        return appendPrBuilderIfPresent(jobs, projectName)
    }

    private List<Job> appendPrBuilderIfPresent(List<Job> jobs, String projectName) {
        if (!prBuilder) {
            return jobs
        }
        return jobs << prBuilder.buildPrJob(projectName)
    }
}

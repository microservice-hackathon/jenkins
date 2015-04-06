package pl.wybcz.pipeline.template

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import pl.wybcz.pipeline.pr.PrBuilder
import pl.wybcz.pipeline.step.DefaultMicroservicePipelineDslFactory

class MicroserviceJobsBuilder {

    private final DefaultMicroservicePipelineDslFactory pipeline
    private final PrBuilder prBuilder

    MicroserviceJobsBuilder(DslFactory dslFactory, PrBuilder prBuilder) {
        this.pipeline = new DefaultMicroservicePipelineDslFactory(dslFactory)
        this.prBuilder = prBuilder
    }

    List<Job> buildJobs(String projectName, String projectGitRepo) {
        List<Job> jobs = [
                pipeline.build(projectName, projectGitRepo),
                pipeline.publish(projectName, projectGitRepo),
                pipeline.deployStubRunner(projectName, projectGitRepo),
                pipeline.deployApp(projectName, projectGitRepo),
                pipeline.runSmokeTests(projectName, projectGitRepo),
                pipeline.deployPreviousVersion(projectName, projectGitRepo),
                pipeline.runSmokeTestsOnOldJar(projectName, projectGitRepo),
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

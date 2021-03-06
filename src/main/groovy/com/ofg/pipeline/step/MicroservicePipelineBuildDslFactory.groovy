package com.ofg.pipeline.step

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class MicroservicePipelineBuildDslFactory extends AbstractMicroservicePipeline  {

    private final DslFactory dslFactory

    MicroservicePipelineBuildDslFactory(DslFactory dslFactory) {
        this.dslFactory = dslFactory
    }

    Job build(String projectName, String projectGitRepo) {
        return dslFactory.job("${projectName}-build") {
            deliveryPipelineConfiguration('Build', 'Build')
            wrappers {
                deliveryPipelineVersion('CD-${BUILD_NUMBER}', true)
            }
            scm {
                git(projectGitRepo, 'master')
            }
            steps {
                gradle('clean build -x test -x generateWiremockClientStubs')
            }
            publishers downstreamParametrized("${projectName}-publish")
        }
    }

    Job publish(String projectName, String projectGitRepo) {
        return dslFactory.job("${projectName}-publish") {
            deliveryPipelineConfiguration('Build', 'Publish')
            wrappers {
                deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
            }
            scm {
                git(projectGitRepo, 'master')
            }
            steps {
                gradle('build -x test -x generateWiremockClientStubs')
            }
            publishers downstreamParametrized("${projectName}-deploy-stub-runner")
        }
    }

}

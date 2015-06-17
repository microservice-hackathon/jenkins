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
            deliveryPipelineConfiguration('Build', 'Build and deploy')
            wrappers {
                deliveryPipelineVersion('CD-${BUILD_NUMBER}', true)
            }
            scm {
                git(projectGitRepo, 'master')
            }
            steps {
                gradle('clean build publish -PbuildNr=$PIPELINE_VERSION --stacktrace')
            }
            publishers downstreamParametrized("${projectName}-deploy-to-prod")
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
                gradle('publish')
            }
            publishers downstreamParametrized("${projectName}-deploy-to-prod")
        }
    }

}

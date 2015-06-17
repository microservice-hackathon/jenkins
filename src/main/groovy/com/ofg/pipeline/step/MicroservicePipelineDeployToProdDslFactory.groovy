package com.ofg.pipeline.step

import com.ofg.pipeline.domain.NexusBuilder
import groovy.transform.CompileStatic
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

@CompileStatic
class MicroservicePipelineDeployToProdDslFactory extends AbstractMicroservicePipeline  {

    private final DslFactory dslFactory
    private final NexusBuilder nexusBuilder

    MicroservicePipelineDeployToProdDslFactory(DslFactory dslFactory, NexusBuilder nexusBuilder) {
        this.dslFactory = dslFactory
        this.nexusBuilder = nexusBuilder
    }

    Job deployToProd(String projectName, String projectGitRepo) {
        return dslFactory.job("${projectName}-deploy-to-prod") {
            deliveryPipelineConfiguration('Deploy to prod', 'Deploy to prod')
            wrappers {
                deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
            }
            scm {
                git(projectGitRepo, 'master')
            }
            steps {
                environmentVariables {
                    propertiesFile('gradle.properties')
                }
            }
            publishers {
                rundeck('deploy:deploy') {
                    options([
                            artifactId: projectName,
                            groupId: '$groupId',
                            nexusUrl: nexusBuilder.repoUrl,
                            version: '$PIPELINE_VERSION'

                    ])
                    shouldFailTheBuild()
                    shouldWaitForRundeckJob()
                }
            }
        }
    }


}

package com.ofg.pipeline.step

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class MicroservicePipelineDeployToProdDslFactory extends AbstractMicroservicePipeline  {

    private final DslFactory dslFactory

    MicroservicePipelineDeployToProdDslFactory(DslFactory dslFactory) {
        this.dslFactory = dslFactory
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
                rundeck('deploy') {
                    options([
                            artifactId: projectName,
                            groupId: 'pl.devoxx',
                            nexusUrl: System.getenv().getOrDefault('mavenRepoUrl', 'http://nexus.com'),
                            version: System.getenv().getOrDefault('PIPELINE_VERSION', '1.0.0')

                    ])
                    shouldFailTheBuild()
                    shouldWaitForRundeckJob()
                }
            }
        }
    }


}

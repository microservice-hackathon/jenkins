package com.ofg.pipeline.step

import com.ofg.pipeline.domain.NexusBuilder
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class MicroservicePipelineBuildDslFactory extends AbstractMicroservicePipeline  {

    private final DslFactory dslFactory
    private final NexusBuilder nexusBuilder

    MicroservicePipelineBuildDslFactory(DslFactory dslFactory, NexusBuilder nexusBuilder) {
        this.dslFactory = dslFactory
        this.nexusBuilder = nexusBuilder
    }

    Job build(String projectName, String projectGitRepo) {
        return dslFactory.job("${projectName}-build") {
            deliveryPipelineConfiguration('Build', 'Build and deploy')
            wrappers {
                deliveryPipelineVersion('CD-${BUILD_NUMBER}', true)
                environmentVariables {
                    injectPasswords()
                    maskPasswords()
                }
            }
            triggers {
                githubPush()
                scm('*/1 * * * *')
            }
            configure {
                Node propertiesNode = it / 'properties'
                def slack = propertiesNode / 'jenkins.plugins.slack.SlackNotifier_-SlackJobProperty'
                (slack / 'startNotification').setValue(true)
                (slack / 'notifySuccess').setValue(true)
                (slack / 'notifyAborted').setValue(true)
                (slack / 'notifyNotBuilt').setValue(true)
                (slack / 'notifyUnstable').setValue(true)
                (slack / 'notifyFailure').setValue(true)
                (slack / 'notifyBackToNormal').setValue(true)
                (slack / 'notifyRepeatedFailure').setValue(true)
                (slack / 'includeTestSummary').setValue(true)
                (slack / 'showCommitList').setValue(true)
            }
            scm {
                git(projectGitRepo, 'master')
            }
            steps {
                gradle("clean build publish -PbuildNr=\$PIPELINE_VERSION --stacktrace -PmavenUser=$nexusBuilder.mavenUsername -PmavenRepoUrl=$nexusBuilder.repoUrl")
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

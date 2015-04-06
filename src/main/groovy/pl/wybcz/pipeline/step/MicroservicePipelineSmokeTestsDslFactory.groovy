package pl.wybcz.pipeline.step

import groovy.transform.CompileStatic
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

@CompileStatic
class MicroservicePipelineSmokeTestsDslFactory extends AbstractMicroservicePipeline {

    private final DslFactory dslFactory

    MicroservicePipelineSmokeTestsDslFactory(DslFactory dslFactory) {
        this.dslFactory = dslFactory
    }

    Job deployStubRunner(String projectName, String projectGitRepo) {
        return dslFactory.job("${projectName}-deploy-stub-runner") {
            deliveryPipelineConfiguration('Smoke tests', 'Deploy stub-runner')
            wrappers {
                deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
            }
            scm {
                git(projectGitRepo, 'master')
            }
            steps {
                gradle('build -x test')
            }
            publishers downstreamParametrized("${projectName}-deploy-app")
        }
    }

    Job deployApp(String projectName, String projectGitRepo) {
        return dslFactory.job("${projectName}-deploy-app") {
            deliveryPipelineConfiguration('Smoke tests', 'Deploy app')
            wrappers {
                deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
            }
            scm {
                git(projectGitRepo, 'master')
            }
            steps {
                gradle('build -x test')
            }
            publishers downstreamParametrized("${projectName}-run-smoke-tests")
        }
    }

    Job runSmokeTests(String projectName, String projectGitRepo) {
        return dslFactory.job("${projectName}-run-smoke-tests") {
            deliveryPipelineConfiguration('Smoke tests', 'Run smoke tests')
            wrappers {
                deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
            }
            scm {
                git(projectGitRepo, 'master')
            }
            steps {
                gradle('build -x test')
            }
            publishers downstreamParametrized("${projectName}-deploy-previous-version")
        }
    }

    Job deployPreviousVersion(String projectName, String projectGitRepo) {
        return dslFactory.job("${projectName}-deploy-previous-version") {
            deliveryPipelineConfiguration('Smoke tests', 'Deploy previous version')
            wrappers {
                deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
            }
            scm {
                git(projectGitRepo, 'master')
            }
            steps {
                gradle('build -x test')
            }
            publishers downstreamParametrized("${projectName}-run-smoke-tests-on-old-jar")
        }
    }

    Job runSmokeTestsOnOldJar(String projectName, String projectGitRepo) {
        return dslFactory.job("${projectName}-run-smoke-tests-on-old-jar") {
            deliveryPipelineConfiguration('Smoke tests', 'Run smoke tests on old jar')
            wrappers {
                deliveryPipelineVersion('${ENV,var="PIPELINE_VERSION"}', true)
            }
            scm {
                git(projectGitRepo, 'master')
            }
            steps {
                gradle('build -x test')
            }
            publishers downstreamParametrized("${projectName}-deploy-to-prod")
        }
    }

}
